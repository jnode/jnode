/*
 * $Id$
 */
package org.jnode.vm.x86;

import java.nio.ByteOrder;
import java.util.Iterator;

import org.jnode.system.BootLog;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.vm.Unsafe;
import org.jnode.vm.Vm;
import org.jnode.vm.VmArchitecture;
import org.jnode.vm.VmProcessor;
import org.jnode.vm.VmStackReader;
import org.jnode.vm.classmgr.VmStatics;
import org.jnode.vm.compiler.NativeCodeCompiler;
import org.jnode.vm.x86.compiler.l1.X86Level1Compiler;
import org.jnode.vm.x86.compiler.stub.X86StubCompiler;

/**
 * Architecture descriptor for the Intel X86 architecture.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VmX86Architecture extends VmArchitecture {

    public static final int SLOT_SIZE = 4;

    /** The stackreader of this architecture */
    private final VmX86StackReader stackReader = new VmX86StackReader();

    /** The compilers */
    private final NativeCodeCompiler[] compilers = { /* new X86Level0Compiler(), */
            new X86StubCompiler(), new X86Level1Compiler()};

    /** The local APIC accessor, if any */
    private LocalAPIC localAPIC;

    /** The MP configuration table */
    private MPConfigTable mpConfigTable;

    /**
     * Gets the name of this architecture.
     * 
     * @return name
     */
    public final String getName() {
        return "x86";
    }

    /**
     * Gets the byte ordering of this architecture.
     * 
     * @return ByteOrder
     */
    public final ByteOrder getByteOrder() {
        return ByteOrder.LITTLE_ENDIAN;
    }

    /**
     * Gets the size in bytes of an object reference.
     * 
     * @return Size of reference, always 4 here
     */
    public final int getReferenceSize() {
        return SLOT_SIZE;
    }

    /**
     * Gets the stackreader for this architecture.
     * 
     * @return Stack reader
     */
    public final VmStackReader getStackReader() {
        return stackReader;
    }

    /**
     * Gets all compilers for this architecture.
     * 
     * @return The compilers, sorted by optimization level, from least
     *         optimizations to most optimizations.
     */
    public final NativeCodeCompiler[] getCompilers() {
        return compilers;
    }

    /**
     * Create a processor instance for this architecture.
     * 
     * @return The processor
     */
    public VmProcessor createProcessor(int id, VmStatics statics) {
        return new VmX86Processor(id, this, statics, null);
    }

    /**
     * @see org.jnode.vm.VmArchitecture#initializeProcessors(ResourceManager)
     */
    protected void initializeProcessors(ResourceManager rm) {
        final MPFloatingPointerStructure mp = MPFloatingPointerStructure.find(
                rm, ResourceOwner.SYSTEM);
        if (mp == null) {
            BootLog.info("No MP table found");
            // No MP table found.
            return;
        }
        try {
            BootLog.info("Found " + mp);
            this.mpConfigTable = mp.getMPConfigTable();
        } finally {
            mp.release();
        }

        if (mpConfigTable == null) { return; }

        mpConfigTable.dump(System.out);
        try {
            // Create the local APIC accessor
            localAPIC = new LocalAPIC(rm, ResourceOwner.SYSTEM, mpConfigTable
                    .getLocalApicAddress());
        } catch (ResourceNotFreeException ex) {
            BootLog.error("Cannot claim APIC region");
            return;
        }

        // Set the APIC reference of the current (bootstrap) processor
        final VmX86Processor cpu = (VmX86Processor) Unsafe
                .getCurrentProcessor();
        cpu.setApic(localAPIC);
        cpu.loadAndSetApicID();

        try {
            // Detect Hyper threading on current (bootstrap) processor
            VmX86Processor.detectAndstartLogicalProcessors(rm);
        } catch (ResourceNotFreeException ex) {
            BootLog.error("Cannot claim region for logical processor startup", ex);
        }

        // Find all CPU's
        for (Iterator i = mpConfigTable.entries().iterator(); i.hasNext();) {
            final MPEntry e = (MPEntry) i.next();
            if (e.getEntryType() == 0) {
                final MPProcessorEntry cpuEntry = (MPProcessorEntry) e;
                if (cpuEntry.isEnabled() && !cpuEntry.isBootstrap()) {
                    // New CPU
                    final VmX86Processor newCpu = (VmX86Processor) createProcessor(
                            cpuEntry.getApicID(), Vm.getVm().getStatics());
                    initX86Processor(newCpu);
                    try {
                        newCpu.startup(rm);
                    } catch (ResourceNotFreeException ex) {
                        BootLog.error("Cannot claim region for processor startup", ex);
                    }
                }
            }
        }
    }

    /**
     * Initialize a processor wrt. APIC and add it to the list of processors.
     * 
     * @param cpu
     */
    final void initX86Processor(VmX86Processor cpu) {
        cpu.setApic(localAPIC);
        super.addProcessor(cpu);
    }
}