/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.vm.x86;

import java.io.PrintStream;

import org.jnode.system.BootLog;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.util.NumberUtils;
import org.jnode.util.TimeUtils;
import org.jnode.vm.CpuID;
import org.jnode.vm.Unsafe;
import org.jnode.vm.Vm;
import org.jnode.vm.VmProcessor;
import org.jnode.vm.VmScheduler;
import org.jnode.vm.VmThread;
import org.jnode.vm.annotation.LoadStatics;
import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.annotation.PrivilegedActionPragma;
import org.jnode.vm.annotation.Uninterruptible;
import org.jnode.vm.classmgr.VmIsolatedStatics;
import org.jnode.vm.classmgr.VmSharedStatics;
import org.jnode.vm.performance.PerformanceCounters;
import org.jnode.vm.x86.performance.X86PerformanceCounters;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Word;

/**
 * Processor implementation for the X86 architecture.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@MagicPermission
public abstract class VmX86Processor extends VmProcessor {

    /** The IRQ counters */
    private final int[] irqCount = new int[16];

    /** The local API */
    private LocalAPIC apic;

    /** GDT used in this processor */
    private GDT gdt;

    /** Is this a logical processor? */
    private boolean logical = false;

    /** The resource manager */
    private ResourceManager rm;

    /** Kernel variable (ints.asm) */
    volatile Word resume_int;

    /** Kernel variable (ints.asm) */
    volatile Word resume_intno;

    /** Kernel variable (ints.asm) */
    volatile Word resume_error;

    /** Kernel variable (ints.asm) */
    volatile Address resume_handler;

    /** Kernel variable (vm-ints.asm) */
    volatile int deadLockCounter;

    /** Kernel variable (vm-inst.asm) */
    volatile int fxSaveCounter;

    /** Kernel variable (vm-inst.asm) */
    volatile int fxRestoreCounter;

    /** Kernel variable (vm-inst.asm) */
    volatile int deviceNaCounter;
    
    /** My performance counter accessor */
    transient X86PerformanceCounters perfCounters;

    /**
     * @param id
     */
    public VmX86Processor(int id, VmX86Architecture arch, VmSharedStatics statics,
            VmIsolatedStatics isolatedStatics, VmScheduler scheduler, X86CpuID cpuId) {
        super(id, arch, statics, isolatedStatics, scheduler);
        if (cpuId != null) {
            setCPUID(cpuId);
        }
    }

    /**
     * Gets the IRQ counters array.
     * 
     * @return int[]
     */
    protected final int[] getIrqCounters() {
        return irqCount;
    }

    /**
     * Load the CPU id.
     * 
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
     * @param apic
     *            The apic to set.
     */
    final void setApic(LocalAPIC apic) {
        this.apic = apic;
    }

    /**
     * Load the APIC id of the currently executing processor and set it into the
     * Id field.
     */
    final void loadAndSetApicID() {
        if (this.apic != null) {
            setId(apic.getId());
        }
    }

    /**
     * Send a startup signal to this processor.
     */
    @PrivilegedActionPragma
    final void startup(ResourceManager rm) throws ResourceNotFreeException {
        // Save resource manager, so when this processor starts, it can be
        // used right away.
        this.rm = rm;
        final VmProcessor me = current();
        Unsafe.debug("Startup of 0x" + NumberUtils.hex(getId(), 2) + " from "
                + NumberUtils.hex(me.getId(), 2) + "\n");

        // Setup kernel structures
        setupStructures();

        // Setup the boot code
        final Address bootCode = setupBootCode(rm, gdt);
        
        // Make sure APIC is enabled (the apic of the current CPU!)
        Unsafe.debug("Enabling APIC: current state " + apic.isEnabled() + "\n");
        apic.setEnabled(true);
        apic.clearErrors();
        // TimeUtils.loop(5000);

        // Send INIT IPI
        Unsafe.debug("Sending INIT IPI\n");
        apic.sendInitIPI(getId(), true);
        apic.loopUntilNotBusy();
        TimeUtils.loop(10);

        // Send INIT-DeAssert IPI
        Unsafe.debug("Sending INIT-DeAssert IPI\n");
        apic.sendInitIPI(getId(), false);
        apic.loopUntilNotBusy();
        TimeUtils.loop(10);

        final int numStarts = 2;
        for (int i = 0; i < numStarts; i++) {
            // Send STARTUP IPI
            Unsafe.debug("Sending STARTUP IPI to " + getId() + "\n");
            apic.clearErrors();
            apic.sendStartupIPI(getId(), bootCode);
            apic.loopUntilNotBusy();
            // Unsafe.debug("Not busy");
            TimeUtils.loop(100);
            apic.clearErrors();
        }

        Unsafe.debug("X86 CPU Start finished\n");
        // BootLog.info("loop 5000");
        // TimeUtils.loop(5000);
    }

    /**
     * Create a new thread
     * 
     * @param stack
     * @return The new thread
     */
    protected abstract VmX86Thread createThread(VmIsolatedStatics isolatedStatics, byte[] stack);

    /**
     * Setup the required CPU structures. GDT, TSS, kernel stack, user stack,
     * initial thread.
     */
    protected final void setupStructures() {
        // Clone GDT
        this.gdt = new GDT();
        setupGDT(gdt);

        // Create user stack
        final byte[] userStack = new byte[VmThread.DEFAULT_STACK_SLOTS * getArchitecture().getReferenceSize()];
        setupUserStack(userStack);
        this.currentThread = createThread(getIsolatedStatics(), userStack);
        this.stackEnd = ((VmX86Thread)currentThread).getStackEnd().toAddress();

        // gdt.dump(System.out);
    }

    /**
     * Setup the given GDT for use by this processor.
     * 
     * @param gdt
     */
    protected abstract void setupGDT(GDT gdt);

    /**
     * Setup the initial user stack
     */
    protected abstract void setupUserStack(byte[] userStack);

    /**
     * Setup a memory region with bootcode for this processor.
     * 
     * @param rm
     * @return The address of the bootcode.
     */
    protected abstract Address setupBootCode(ResourceManager rm, GDT gdt)
            throws ResourceNotFreeException;

    /**
     * Entry point for starting Application processors.
     */
    @LoadStatics
    @Uninterruptible
    static final void applicationProcessorMain() {
        final VmX86Processor cpu = (VmX86Processor) current();
        Unsafe.debug("Starting Application Processor " + cpu.getIdString());
              
        // First force a load of CPUID
        cpu.getCPUID();
        
        // Prepare for threading
        cpu.systemReadyForThreadSwitch();

        // Detect and start logical CPU's
        try {
            detectAndstartLogicalProcessors(cpu.rm);
        } catch (ResourceNotFreeException ex) {
            BootLog.error("Cannot detect logical processors", ex);
        }

        // Run idle thread. 
        // The scheduler will fetch other work
        Unsafe.debug("Running normal threads on " + cpu.getIdString());
        cpu.getIdleThread().run();
    }

    /**
     * Detect and start any logical processors found in the currently running
     * CPU.
     */
    static final void detectAndstartLogicalProcessors(ResourceManager rm)
            throws ResourceNotFreeException {
        final VmX86Processor cpu = (VmX86Processor) current();
        if (cpu.logical) {
            return;
        }
        final X86CpuID cpuid = (X86CpuID) cpu.getCPUID();
        if (!cpuid.hasFeature(X86CpuID.FEAT_HTT)) {
            // No HTT
            return;
        }
        
        // Prepare for threading first
        cpu.systemReadyForThreadSwitch();

        final VmX86Architecture arch = (VmX86Architecture) cpu
                .getArchitecture();
        final int logCpuCnt = cpuid.getLogicalProcessors();
        // Now create and start all logical processors
        for (int i = 1; i < logCpuCnt; i++) {
            final int logId = cpu.getId() | i;
            Unsafe.debug("Adding logical CPU 0x" + NumberUtils.hex(logId, 2));
            final VmX86Processor logCpu = (VmX86Processor) arch
                    .createProcessor(logId, Vm.getVm().getSharedStatics(), cpu.getIsolatedStatics(), cpu.getScheduler());
            logCpu.logical = true;
            arch.initX86Processor(logCpu);
            logCpu.startup(rm);
        }
    }

    
    /**
     * Gets the performance counter accessor of this processor.
     * @return
     */
    public final PerformanceCounters getPerformanceCounters() {
        if (perfCounters == null) {
            synchronized (this) {
                if (perfCounters == null) {                
                    perfCounters = X86PerformanceCounters.create(this, (X86CpuID)getCPUID());
                }
            }
        }
        return perfCounters;
    }
    
    public void dumpStatistics(PrintStream out) {
        out.println(getCPUID());
        out.println("fxSave/Restore " + fxSaveCounter + "/" + fxRestoreCounter
                + "/" + deviceNaCounter);
    }
}
