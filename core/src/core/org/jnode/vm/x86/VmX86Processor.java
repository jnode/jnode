/*
 * $Id$
 */
package org.jnode.vm.x86;

import org.jnode.system.BootLog;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.util.NumberUtils;
import org.jnode.util.TimeUtils;
import org.jnode.vm.CpuID;
import org.jnode.vm.Unsafe;
import org.jnode.vm.Vm;
import org.jnode.vm.VmProcessor;
import org.jnode.vm.VmThread;
import org.jnode.vm.classmgr.VmStatics;
import org.vmmagic.pragma.LoadStaticsPragma;
import org.vmmagic.unboxed.ObjectReference;

/**
 * Processor implementation for the X86 architecture.
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VmX86Processor extends VmProcessor {
	
	/** The IRQ counters */
	private final int[] irqCount = new int[16];
	/** The local API */
	private LocalAPIC apic;
	/** GDT used in this processor */
	private GDT gdt;
	/** TSS used in this processor */
	private TSS tss;
	/** Is this a logical processor? */
	private boolean logical = false;
	/** Space of boot code */
	private MemoryResource bootCode;
	/** The resource manager */
	private ResourceManager rm;
	/** Kernel variable (ints.asm) */
	volatile int resume_int;
	/** Kernel variable (ints.asm) */
	volatile int resume_intno;
	/** Kernel variable (ints.asm) */
	volatile int resume_error;
	/** Kernel variable (ints.asm) */
	volatile int resume_handler;
	/** Kernel variable (vm-ints.asm) */
	volatile int deadLockCounter;

	/**
	 * @param id
	 */
	public VmX86Processor(int id, VmX86Architecture arch, VmStatics statics, X86CpuID cpuId) {
		super(id, arch, statics);
		if (cpuId != null) {
			setCPUID(cpuId);
		}
	}

	/**
	 * Gets the IRQ counters array. 
	 * @return int[]
	 */
	protected final int[] getIrqCounters() {
		return irqCount;
	}
	
	/**
	 * Create a new thread
	 * @return The new thread
	 */
	protected VmThread createThread() {
		return new VmX86Thread();
	}
	
	/**
	 * Create a new thread
	 * @param javaThread
	 * @return The new thread
	 */
	public VmThread createThread(Thread javaThread) {
		return new VmX86Thread(javaThread);
	}
	
	/**
	 * Load the CPU id.
	 * @return CpuID
	 */
	protected CpuID loadCPUID(int[] id) {
		return new X86CpuID(id);
	}
	
    /**
     * @return Returns the apic.
     */
    final LocalAPIC getApic() {
        return this.apic;
    }
    /**
     * @param apic The apic to set.
     */
    final void setApic(LocalAPIC apic) {
        this.apic = apic;
    }
    
    /**
     * Load the APIC id of the currently executing processor and set it
     * into the Id field.
     */
    final void loadAndSetApicID() {
        if (this.apic != null) {
            setId(apic.getId());
        }
    }
    
    /**
     * Send a startup signal to this processor.
     */
    final void startup(ResourceManager rm) throws ResourceNotFreeException {
        // Save resource manager, so when this processor starts, it can be
        // used right away.
        this.rm = rm;
        final VmProcessor me = Unsafe.getCurrentProcessor();
        BootLog.info("Startup of 0x" + NumberUtils.hex(getId(), 2) + " from " + NumberUtils.hex(me.getId(), 2));
        
        // Setup kernel structures
        setupStructures();
        
        // Setup the boot code
        setupBootCode(rm);
        
        // Make sure APIC is enabled (the apic of the current CPU!)
        BootLog.info("Enabling APIC current state " + apic.isEnabled());
        apic.setEnabled(true);
        apic.clearErrors();
        //TimeUtils.loop(5000);
        
        // Send INIT IPI
        BootLog.info("Sending INIT IPI");
        apic.sendInitIPI(getId(), true);
        apic.loopUntilNotBusy();
        TimeUtils.loop(10);
        
        // Send INIT-DeAssert IPI
        BootLog.info("Sending INIT-DeAssert IPI");
        apic.sendInitIPI(getId(), false);
        apic.loopUntilNotBusy();
        TimeUtils.loop(10);

        final int numStarts = 2;
        for (int i = 0; i < numStarts; i++) {
            // Send STARTUP IPI
            BootLog.info("Sending STARTUP IPI");
            apic.clearErrors();
            apic.sendStartupIPI(getId(), bootCode.getAddress());
            apic.loopUntilNotBusy();
            //BootLog.info("Not busy");
            TimeUtils.loop(100);
            apic.clearErrors();
        }
        
        //BootLog.info("loop 5000");
        //TimeUtils.loop(5000);
    }
    
    /**
     * Setup the required CPU structures.
     * GDT, TSS, kernel stack, user stack, initial thread.
     */
    private final void setupStructures() {
        // Clone GDT
        this.gdt = new GDT();
        gdt.setBase(GDT.PROCESSOR_ENTRY, ObjectReference.fromObject(this).toAddress());
        
        // Clone TSS
        this.tss = new TSS();
        gdt.setBase(GDT.TSS_ENTRY, tss.getAddress());
        
        // Create kernel stack
        tss.setKernelStack(new byte[VmThread.DEFAULT_STACK_SIZE]);
        
        // Create user stack
        final byte[] userStack = new byte[VmThread.DEFAULT_STACK_SIZE];
        tss.setUserStack(userStack);
        this.currentThread = new VmX86Thread(userStack);
        
        //gdt.dump(System.out);
        
    }
    
    /**
     * Setup a memory region with bootcode for this processor.
     * @param rm
     */
    private final void setupBootCode(ResourceManager rm) throws ResourceNotFreeException {
        // Setup the AP bootcode 
        final int size = UnsafeX86.getAPBootCodeSize();
        
        // Claim the memory
        this.bootCode = rm.claimMemoryResource(ResourceOwner.SYSTEM, null, size, ResourceManager.MEMMODE_ALLOC_DMA);

        // Initialize the memory
        UnsafeX86.setupBootCode(bootCode.getAddress(), gdt.getGdt(), tss.getTSS());
        
    }
    
    /**
     * Entry point for starting Application processors.
     */
    static final void applicationProcessorMain() throws LoadStaticsPragma {
        final VmX86Processor cpu = (VmX86Processor)Unsafe.getCurrentProcessor();
        BootLog.info("Starting Application Processor " + cpu.getId());

        // First force a load of CPUID
        cpu.getCPUID();
        
        // Detect and start logical CPU's
        try {
            detectAndstartLogicalProcessors(cpu.rm);
        } catch (ResourceNotFreeException ex) {
            BootLog.error("Cannot detect logical processors", ex);
        }
        
        // TODO do something useful.        
    }
    
    /**
     * Detect and start any logical processors found in the currently
     * running CPU. 
     */
    static final void detectAndstartLogicalProcessors(ResourceManager rm) throws ResourceNotFreeException {
        final VmX86Processor cpu = (VmX86Processor)Unsafe.getCurrentProcessor();
        if (cpu.logical) {
            return;
        }
        final X86CpuID cpuid = (X86CpuID)cpu.getCPUID();
        if (!cpuid.hasFeature(X86CpuID.FEAT_HTT)) {
            // No HTT
            return;
        }
        
        final VmX86Architecture arch = (VmX86Architecture)cpu.getArchitecture();
        final int logCpuCnt = cpuid.getLogicalProcessors();
        // Now create and start all logical processors
        for (int i = 1; i < logCpuCnt; i++) {
            final int logId = cpu.getId() | i;
            BootLog.info("Adding logical CPU 0x" + NumberUtils.hex(logId, 2));
            final VmX86Processor logCpu = (VmX86Processor)arch.createProcessor(logId, Vm.getVm().getStatics());
            logCpu.logical = true;
            arch.initX86Processor(logCpu);
            logCpu.startup(rm);
        }        
    }
}
