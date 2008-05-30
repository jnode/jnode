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

package org.jnode.vm;

import java.nio.ByteOrder;
import org.jnode.security.JNodePermission;
import org.jnode.system.ResourceManager;
import org.jnode.vm.annotation.Internal;
import org.jnode.vm.annotation.KernelSpace;
import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.classmgr.TypeSizeInfo;
import org.jnode.vm.classmgr.VmIsolatedStatics;
import org.jnode.vm.classmgr.VmSharedStatics;
import org.jnode.vm.compiler.IMTCompiler;
import org.jnode.vm.compiler.NativeCodeCompiler;
import org.jnode.vm.scheduler.IRQManager;
import org.jnode.vm.scheduler.VmProcessor;
import org.jnode.vm.scheduler.VmScheduler;
import org.vmmagic.pragma.UninterruptiblePragma;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;
import org.vmmagic.unboxed.Word;

/**
 * Class describing a specific system architecture.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@MagicPermission
public abstract class VmArchitecture extends VmSystemObject {

    private final JNodePermission MMAP_PERM = new JNodePermission("getMemoryMap");
    private transient MemoryMapEntry[] memoryMap;
    private transient VmMultiMediaSupport multiMediaSupport;
    private final int referenceSize;
    private final VmStackReader stackReader;

    protected VmArchitecture(int referenceSize, VmStackReader stackReader) {
        this.referenceSize = referenceSize;
        this.stackReader = stackReader;
    }

    /**
     * Gets the name of this architecture.
     * This name is the programmers name used to identify packages,
     * class name extensions etc.
     *
     * @return Name
     */
    public abstract String getName();

    /**
     * Gets the full name of this architecture, including operating mode.
     *
     * @return Name
     */
    public abstract String getFullName();

    /**
     * Gets the byte ordering of this architecture.
     *
     * @return ByteOrder
     */
    public abstract ByteOrder getByteOrder();

    /**
     * Gets the size in bytes of an object reference.
     *
     * @return Reference size
     */
    @KernelSpace
    public final int getReferenceSize() {
        return referenceSize;
    }

    /**
     * Gets the log base two of the size of an OS page
     *
     * @return
     */
    public abstract byte getLogPageSize(int region)
        throws UninterruptiblePragma;

    /**
     * Gets the log base two of the size of an OS page
     *
     * @return
     */
    public final Extent getPageSize(int region)
        throws UninterruptiblePragma {
        return Extent.fromIntZeroExtend(1 << getLogPageSize(region));
    }

    /**
     * Gets the type size information of this architecture.
     *
     * @return
     */
    public abstract TypeSizeInfo getTypeSizeInfo();

    /**
     * Gets the stackreader for this architecture.
     *
     * @return Stack reader
     */
    @KernelSpace
    public final VmStackReader getStackReader() {
        return stackReader;
    }

    /**
     * Gets all compilers for this architecture.
     *
     * @return The compilers, sorted by optimization level, from least optimizations to most
     *         optimizations.
     */
    public abstract NativeCodeCompiler[] getCompilers();

    /**
     * Gets all test compilers for this architecture.
     * This can be used to test new compilers in a running system.
     *
     * @return The compilers, sorted by optimization level, from least optimizations to most
     *         optimizations, or null for no test compilers.
     */
    public abstract NativeCodeCompiler[] getTestCompilers();

    /**
     * Gets the compiler of IMT's.
     *
     * @return
     */
    public abstract IMTCompiler getIMTCompiler();

    /**
     * Called early on in the boot process (before the initializating of
     * the memory manager) to initialize any architecture specific variables.
     * Do not allocate memory here.
     *
     * @param emptyMmap If true, all page mappings in the AVAILABLE region
     *                  are removed.
     */
    protected abstract void boot(boolean emptyMmap);

    /**
     * Find and start all processors in the system.
     * All all discovered processors to the given list.
     * The bootstrap processor is already on the given list.
     */
    protected abstract void initializeProcessors(ResourceManager rm);

    /**
     * Call this method to register a processor found in {@link #initializeProcessors(ResourceManager)}.
     *
     * @param cpu
     */
    protected final void addProcessor(VmProcessor cpu) {
        Vm.getVm().addProcessor(cpu);
    }

    /**
     * Create a processor instance for this architecture.
     *
     * @return The processor
     */
    protected abstract VmProcessor createProcessor(int id, VmSharedStatics sharedStatics,
                                                   VmIsolatedStatics isolatedStatics, VmScheduler scheduler);

    /**
     * Create the IRQ manager for this architecture.
     *
     * @return
     */
    @Internal
    public abstract IRQManager createIRQManager(VmProcessor processor);

    /**
     * Gets the start address of the given space.
     *
     * @return
     */
    public Address getStart(int space) {
        switch (space) {
            case VirtualMemoryRegion.BOOTIMAGE:
                return Unsafe.getKernelStart();
            case VirtualMemoryRegion.INITJAR:
                return Unsafe.getInitJarStart();
            default:
                throw new IllegalArgumentException("Unknown space " + space);
        }
    }

    /**
     * Gets the start address of the given space.
     *
     * @return
     */
    public Address getEnd(int space) {
        switch (space) {
            case VirtualMemoryRegion.BOOTIMAGE:
                return Unsafe.getBootHeapEnd();
            case VirtualMemoryRegion.INITJAR:
                return Unsafe.getInitJarEnd();
            default:
                throw new IllegalArgumentException("Unknown space " + space);
        }
    }

    /**
     * Gets the physical address of the first page available
     * for mmap.
     *
     * @return
     */
    protected final Word getFirstAvailableHeapPage() {
        return pageAlign(VirtualMemoryRegion.HEAP, Unsafe.getMemoryStart().toWord(), true);
    }

    /**
     * Page align a given value.
     *
     * @param v
     * @param up If true, the value will be rounded up, otherwise rounded down.
     * @return
     */
    public final Word pageAlign(int region, Word v, boolean up) {
        final int logPageSize = getLogPageSize(region);
        if (up) {
            v = v.add((1 << logPageSize) - 1);
        }
        return v.rshl(logPageSize).lsh(logPageSize);
    }

    /**
     * Page align a given value.
     *
     * @param v
     * @param up If true, the value will be rounded up, otherwise rounded down.
     * @return
     */
    public final Address pageAlign(int region, Address v, boolean up) {
        return pageAlign(region, v.toWord(), up).toAddress();
    }

    /**
     * Map a region of the virtual memory space. Note that you cannot allocate
     * memory in this memory, because it is used very early in the boot process.
     *
     * @param region   Memory region
     * @param start    The start of the virtual memory region to map
     * @param size     The size of the virtual memory region to map
     * @param physAddr The physical address to map the virtual address to. If this is
     *                 Address.max(), free pages are used instead.
     * @return true for success, false otherwise.
     */
    public abstract boolean mmap(int region, Address start, Extent size, Address physAddr)
        throws UninterruptiblePragma;

    /**
     * Unmap a region of the virtual memory space. Note that you cannot allocate
     * memory in this memory, because it is used very early in the boot process.
     *
     * @param region Memory region
     * @param start  The start of the virtual memory region to unmap. This value is
     *               aligned down on pagesize.
     * @param size   The size of the virtual memory region to unmap. This value is
     *               aligned up on pagesize.
     * @return true for success, false otherwise.
     */
    public abstract boolean munmap(int region, Address start, Extent size)
        throws UninterruptiblePragma;

    /**
     * Gets the memory map of the current system.
     *
     * @return
     */
    public final MemoryMapEntry[] getMemoryMap() {
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(MMAP_PERM);
        }
        if (memoryMap == null) {
            memoryMap = createMemoryMap();
        }
        return memoryMap;
    }

    /**
     * Create the memory map of the current system.
     *
     * @return
     */
    protected abstract MemoryMapEntry[] createMemoryMap();

    /**
     * Creates a multi media memory resource wrapping the given memory resource.
     *
     * @param res
     * @return The created instance, which is never null.
     */
    final MultiMediaMemoryResourceImpl createMultiMediaMemoryResource(MemoryResourceImpl res) {
        if (multiMediaSupport == null) {
            multiMediaSupport = createMultiMediaSupport();
        }
        return new MultiMediaMemoryResourceImpl((MemoryResourceImpl) res, multiMediaSupport);
    }

    /**
     * Creates a multi media support instance optimized for this architecture.
     * Override this to allow architecture optimized implementations.
     */
    protected VmMultiMediaSupport createMultiMediaSupport() {
        return new VmJavaMultiMediaSupport();
    }
}
