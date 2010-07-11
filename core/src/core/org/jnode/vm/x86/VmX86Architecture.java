/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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

import java.nio.ByteOrder;
import java.util.HashMap;

import org.jnode.annotation.Internal;
import org.jnode.annotation.MagicPermission;
import org.jnode.assembler.x86.X86Constants;
import org.jnode.bootlog.BootLogInstance;
import org.jnode.system.resource.ResourceManager;
import org.jnode.system.resource.ResourceNotFreeException;
import org.jnode.system.resource.ResourceOwner;
import org.jnode.vm.Unsafe;
import org.jnode.vm.BaseVmArchitecture;
import org.jnode.vm.VmMagic;
import org.jnode.vm.VmMultiMediaSupport;
import org.jnode.vm.VmSystem;
import org.jnode.vm.classmgr.VmIsolatedStatics;
import org.jnode.vm.classmgr.VmSharedStatics;
import org.jnode.vm.compiler.NativeCodeCompiler;
import org.jnode.vm.facade.MemoryMapEntry;
import org.jnode.vm.facade.VmUtils;
import org.jnode.vm.scheduler.IRQManager;
import org.jnode.vm.scheduler.VmProcessor;
import org.jnode.vm.scheduler.VmScheduler;
import org.jnode.vm.x86.compiler.l1a.X86Level1ACompiler;
import org.jnode.vm.x86.compiler.l1b.X86Level1BCompiler;
import org.jnode.vm.x86.compiler.stub.X86StubCompiler;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;
import org.vmmagic.unboxed.Offset;

/**
 * Architecture descriptor for the Intel X86 architecture.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@MagicPermission
public abstract class VmX86Architecture extends BaseVmArchitecture {

    /**
     * Start address of the boot image (1Mb)
     */
    public static final int BOOT_IMAGE_START = 0x00100000;

    // Page entry flags
    protected static final int PF_PRESENT = 0x00000001;

    protected static final int PF_WRITE = 0x00000002;

    protected static final int PF_USER = 0x00000004;

    protected static final int PF_PWT = 0x00000008;

    protected static final int PF_PCD = 0x00000010;

    protected static final int PF_ACCESSED = 0x00000020;

    protected static final int PF_DIRTY = 0x00000040;

    protected static final int PF_PSE = 0x00000080;

    protected static final int MBMMAP_BASEADDR = 0; // 64-bit base address

    protected static final int MBMMAP_LENGTH = 8; // 64-bit length

    protected static final int MBMMAP_TYPE = 16; // 32-bit type

    protected static final int MBMMAP_ESIZE = 20;

    // Values for MBMMAP_TYPE field
    protected static final int MMAP_TYPE_MEMORY = 1; // Available memory

    protected static final int MMAP_TYPE_RESERVED = 2; // Reserved memory

    protected static final int MMAP_TYPE_ACPI = 3; // ACPI reclaim memory

    protected static final int MMAP_TYPE_NVS = 4; // ACPI NVS memory

    protected static final int MMAP_TYPE_UNUSABLE = 5; // Memory with errors

    // found in it

    /**
     * The compilers
     */
    private final NativeCodeCompiler[] compilers;

    /**
     * The compilers under test
     */
    private final NativeCodeCompiler[] testCompilers;

    /**
     * The local APIC accessor, if any
     */
    private LocalAPIC localAPIC;

    /**
     * The MP configuration table
     */
    private MPConfigTable mpConfigTable;

    /**
     * Programmable interrupt controller
     */
    private PIC8259A pic8259a;

    /**
     * The boot processor
     */
    private transient VmX86Processor bootProcessor;

    /**
     * The centralized irq manager
     */
    private transient X86IRQManager irqManager;

    /**
     * Initialize this instance using the default compiler.
     */
    public VmX86Architecture(int referenceSize) {
        this(referenceSize, "L1A");
    }

    /**
     * Initialize this instance.
     *
     * @param compiler the name of the compiler to use as standard.  If 
     * the supplied name is {@code null} or doesn't match (case insensitively) 
     * one of the known names, the default compiler will be used.  
     */
    public VmX86Architecture(int referenceSize, String compiler) {
        super(referenceSize, new VmX86StackReader(referenceSize));
        this.compilers = new NativeCodeCompiler[2];
        this.compilers[0] = new X86StubCompiler();
        // Compare insensitively, producing a warning if the user selects
        // an unknown compiler, and using a default where appropriate.
        if (compiler != null && compiler.length() > 0 && 
                !compiler.equalsIgnoreCase("default")) {
            if ("L1B".equalsIgnoreCase(compiler)) {
                this.compilers[1] = new X86Level1BCompiler();
            } else if ("L1A".equalsIgnoreCase(compiler)) {
                this.compilers[1] = new X86Level1ACompiler();
            } else { 
                BootLogInstance.get().warn("JNode native compiler '" + compiler + "' is unknown.");
            }
        } 
        if (this.compilers[1] == null) {
            BootLogInstance.get().warn("JNode native compiler defaulting to 'L1A'");
            this.compilers[1] = new X86Level1ACompiler();
        }
        this.testCompilers = null;
    }

    /**
     * Gets the name of this architecture.
     *
     * @return name
     */
    public final String getName() {
        return "x86";
    }

    /**
     * Gets the full name of this architecture, including operating mode.
     *
     * @return Name
     */
    public String getFullName() {
        if (getReferenceSize() == 4) {
            return getName() + "-32";
        } else {
            return getName() + "-64";
        }
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
     * Gets the current operating mode; i.e. 32 or 64 bit mode.
     *
     * @return mode
     */
    public final X86Constants.Mode getMode() {
        if (getReferenceSize() == 4) {
            return X86Constants.Mode.CODE32;
        } else {
            return X86Constants.Mode.CODE64;
        }
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
     * Gets all test compilers for this architecture.
     *
     * @return The compilers, sorted by optimization level, from least
     *         optimizations to most optimizations.
     */
    public final NativeCodeCompiler[] getTestCompilers() {
        return testCompilers;
    }

    /**
     * @see org.jnode.vm.BaseVmArchitecture#initializeProcessors(ResourceManager)
     */
    protected final void initializeProcessors(ResourceManager rm) {
        // Mark current cpu as bootprocessor
        final VmX86Processor bootCpu = (VmX86Processor) VmMagic.currentProcessor();
        this.bootProcessor = bootCpu;
        bootCpu.setBootProcessor(true);

        final String cmdLine = VmSystem.getCmdLine();
        if (cmdLine.indexOf("mp=no") >= 0) {
            return;
        }
        //

        final MPFloatingPointerStructure mp = MPFloatingPointerStructure.find(
            rm, ResourceOwner.SYSTEM);
        if (mp == null) {
            BootLogInstance.get().info("No MP table found");
            // No MP table found.
            return;
        }
        try {
            BootLogInstance.get().info("Found " + mp);
            this.mpConfigTable = mp.getMPConfigTable();
        } finally {
            mp.release();
        }

        if (mpConfigTable == null) {
            return;
        }

        mpConfigTable.dump(System.out);
        final ResourceOwner owner = ResourceOwner.SYSTEM;
        try {
            // Create the local APIC accessor
            localAPIC = new LocalAPIC(rm, owner, mpConfigTable
                .getLocalApicAddress());
        } catch (ResourceNotFreeException ex) {
            BootLogInstance.get().error("Cannot claim APIC region");
            return;
        }

        // Set the APIC reference of the current (bootstrap) processor
        bootCpu.setApic(localAPIC);
        bootCpu.loadAndSetApicID();

        // Find & initialize this I/O APIC.
        for (MPEntry entry : mpConfigTable.entries()) {
            if (entry instanceof MPIOAPICEntry) {
                final MPIOAPICEntry apicEntry = (MPIOAPICEntry) entry;
                if (apicEntry.getFlags() != 0) {
                    try {
                        // We found an enabled I/O APIC.
                        final IOAPIC ioAPIC = new IOAPIC(rm, owner, apicEntry
                            .getAddress());
                        ioAPIC.dump(System.out);
                        break;
                    } catch (ResourceNotFreeException ex) {
                        BootLogInstance.get().error("Cannot claim I/O APIC region ", ex);
                    }
                }
            }
        }

        try {
            // Detect Hyper threading on current (bootstrap) processor
            VmX86Processor.detectAndstartLogicalProcessors(rm);
        } catch (ResourceNotFreeException ex) {
            BootLogInstance.get().error("Cannot claim region for logical processor startup",
                ex);
        }

        // Find all physical AP processors
        final X86CpuID cpuId = (X86CpuID) bootCpu.getCPUID();
        final HashMap<Integer, MPProcessorEntry> physCpus = new HashMap<Integer, MPProcessorEntry>();
        for (MPEntry e : mpConfigTable.entries()) {
            if (e.getEntryType() == 0) {
                final MPProcessorEntry cpuEntry = (MPProcessorEntry) e;
                if (cpuEntry.isEnabled() && !cpuEntry.isBootstrap()) {
                    // Check if it is a physical CPU
                    final int apicId = cpuEntry.getApicID();
                    int physId = cpuId.getPhysicalPackageId(apicId);
                    // This algorithme is based on the specification
                    // that physical processors are listed before logical
                    // processors.
                    if (!physCpus.containsKey(physId)) {
                        // New physical CPU found
                        physCpus.put(physId, cpuEntry);
                    }
                }
            }
        }

        // Start all physical AP processors
        for (MPProcessorEntry cpuEntry : physCpus.values()) {
            final int apicId = cpuEntry.getApicID();
            // New CPU
            final VmX86Processor newCpu = (VmX86Processor) createProcessor(
                apicId, VmUtils.getVm().getSharedStatics(), bootCpu
                .getIsolatedStatics(), bootCpu.getScheduler());
            initX86Processor(newCpu);
            try {
                newCpu.startup(rm);
            } catch (ResourceNotFreeException ex) {
                BootLogInstance.get().error("Cannot claim region for processor startup", ex);
            }
        }

        // If there is more then one CPU, start sending timeslice interrupts now
        BootLogInstance.get().info("Activating timeslice interrupts");
        bootCpu.activateTimeSliceInterrupts();
    }

    /**
     * Create a processor instance for this architecture.
     *
     * @return The processor
     */
    public abstract VmProcessor createProcessor(int id,
                                                VmSharedStatics sharedStatics, VmIsolatedStatics isolatedStatics,
                                                VmScheduler scheduler);

    @Override
    @Internal
    public final IRQManager createIRQManager(VmProcessor processor) {
        synchronized (this) {
            // Create PIC if not available
            if (pic8259a == null) {
                pic8259a = new PIC8259A();
            }
            if (irqManager == null) {
                irqManager = new X86IRQManager(bootProcessor, pic8259a);
            }
        }
        return irqManager;
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

    /**
     * Print the multiboot memory map to Unsafe.debug.
     */
    protected final void dumpMultibootMMap() {
        final int cnt = UnsafeX86.getMultibootMMapLength();
        Address mmap = UnsafeX86.getMultibootMMap();

        Unsafe.debug("Memory map\n");
        for (int i = 0; i < cnt; i++) {
            long base = mmap
                .loadLong(Offset.fromIntZeroExtend(MBMMAP_BASEADDR));
            long length = mmap
                .loadLong(Offset.fromIntZeroExtend(MBMMAP_LENGTH));
            int type = mmap.loadInt(Offset.fromIntZeroExtend(MBMMAP_TYPE));
            mmap = mmap.add(MBMMAP_ESIZE);

            Unsafe.debug(mmapTypeToString(type));
            Unsafe.debug(base);
            Unsafe.debug(" - ");
            Unsafe.debug(base + length - 1);
            Unsafe.debug('\n');
        }
    }

    /**
     * Convert an mmap type into a human readable string.
     *
     * @param type
     * @return
     */
    private final String mmapTypeToString(int type) {
        switch (type) {
            case MMAP_TYPE_MEMORY:
                return "Available    ";
            case MMAP_TYPE_RESERVED:
                return "Reserved     ";
            case MMAP_TYPE_ACPI:
                return "ACPI reclaim ";
            case MMAP_TYPE_NVS:
                return "ACPI NVS     ";
            case MMAP_TYPE_UNUSABLE:
                return "Unusable     ";
            default:
                return "Undefined    ";
        }
    }

    /**
     * @see org.jnode.vm.BaseVmArchitecture#createMemoryMap()
     */
    protected MemoryMapEntry[] createMemoryMap() {
        final int cnt = UnsafeX86.getMultibootMMapLength();
        final MemoryMapEntry[] map = new MemoryMapEntry[cnt];
        Address mmap = UnsafeX86.getMultibootMMap();

        for (int i = 0; i < cnt; i++) {
            long base = mmap
                .loadLong(Offset.fromIntZeroExtend(MBMMAP_BASEADDR));
            long length = mmap
                .loadLong(Offset.fromIntZeroExtend(MBMMAP_LENGTH));
            int type = mmap.loadInt(Offset.fromIntZeroExtend(MBMMAP_TYPE));
            mmap = mmap.add(MBMMAP_ESIZE);

            map[i] = new X86MemoryMapEntry(Address.fromLong(base), Extent
                .fromLong(length), type);
        }

        return map;
    }

    /**
     * @see org.jnode.vm.BaseVmArchitecture#createMultiMediaSupport()
     */
    protected VmMultiMediaSupport createMultiMediaSupport() {
        final X86CpuID id = (X86CpuID) VmProcessor.current().getCPUID();
        if (id.hasMMX()) {
            return new MMXMultiMediaSupport();
        } else {
            return super.createMultiMediaSupport();
        }
    }
}
