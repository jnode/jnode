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

import org.jnode.vm.Unsafe;
import org.jnode.vm.VirtualMemoryRegion;
import static org.jnode.vm.VirtualMemoryRegion.ACPI;
import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.classmgr.TypeSizeInfo;
import org.jnode.vm.classmgr.VmIsolatedStatics;
import org.jnode.vm.classmgr.VmSharedStatics;
import org.jnode.vm.compiler.IMTCompiler;
import org.jnode.vm.scheduler.VmProcessor;
import org.jnode.vm.scheduler.VmScheduler;
import org.jnode.vm.x86.compiler.X86IMTCompiler32;
import org.vmmagic.pragma.UninterruptiblePragma;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;
import org.vmmagic.unboxed.Word;

/**
 * Architecture description for the x86 (32-bit) architecture.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@MagicPermission
public final class VmX86Architecture32 extends VmX86Architecture {

    /**
     * Start address of the virtual memory region  available to devices (3Gb)
     */
    public static final int DEVICE_START = 0xC0000000;

    /**
     * End address of the virtual memory region  available to devices (4Gb-4Mb)
     */
    public static final int DEVICE_END = 0xFFC00000;

    /**
     * Start address of the virtual memory region available to ACPI (3Gb - 4Mb)
     */
    public static final int ACPI_START = DEVICE_START - 0x400000;

    /**
     * Start address of the virtual memory region available to ACPI (3Gb)
     */
    public static final int ACPI_END = DEVICE_START;

    /**
     * Start address of the virtual memory region available to the memory manager (256Mb).
     * This address must be 4Mb aligned.
     */
    public static final int AVAILABLE_START = 0x10000000;

    /**
     * End address of the virtual memory region  available to the memory manager.
     * This address must be 4Mb aligned.
     */
    public static final int AVAILABLE_END = ACPI_START;

    // Log of page size per region
    private static final byte LOG_DEFAULT_PAGE_SIZE = 22;
    private static final byte LOG_AVAILABLE_PAGE_SIZE = LOG_DEFAULT_PAGE_SIZE;
    private static final byte LOG_HEAP_PAGE_SIZE = LOG_AVAILABLE_PAGE_SIZE;
    private static final byte LOG_ACPI_PAGE_SIZE = 22;
    private static final byte LOG_DEVICE_PAGE_SIZE = LOG_DEFAULT_PAGE_SIZE;

    // Page sizes per region
    private static final int AVAILABLE_PAGE_SIZE = 1 << LOG_AVAILABLE_PAGE_SIZE;
    private static final int HEAP_PAGE_SIZE = 1 << LOG_HEAP_PAGE_SIZE;
    private static final int ACPI_PAGE_SIZE = 1 << LOG_ACPI_PAGE_SIZE;
    private static final int DEVICE_PAGE_SIZE = 1 << LOG_DEVICE_PAGE_SIZE;

    /**
     * Size of an object reference
     */
    public static final int SLOT_SIZE = 4;

    /**
     * The IMT compiler
     */
    private final X86IMTCompiler32 imtCompiler;

    /**
     * The type size information
     */
    private final TypeSizeInfo typeSizeInfo;

    /**
     * The next physical page address to be mmaped
     */
    private Word pageCursor;

    /**
     * Default page entry flags
     */
    private static final int PF_DEFAULT = PF_PRESENT | PF_WRITE | PF_USER | PF_PSE;

    /**
     * Initialize this instance.
     */
    public VmX86Architecture32() {
        this("L1A");
    }

    /**
     * Initialize this instance.
     *
     * @param compiler
     */
    public VmX86Architecture32(String compiler) {
        super(SLOT_SIZE, compiler);
        this.imtCompiler = new X86IMTCompiler32();
        this.typeSizeInfo = new TypeSizeInfo(1, 1, 2, 2, 1);
    }

    /**
     * Create a processor instance for this architecture.
     *
     * @return The processor
     */
    public VmProcessor createProcessor(int id, VmSharedStatics sharedStatics, VmIsolatedStatics isolatedStatics,
                                       VmScheduler scheduler) {
        return new VmX86Processor32(id, this, sharedStatics, isolatedStatics, scheduler, null);
    }

    /**
     * Gets the compiler of IMT's.
     *
     * @return The IMT compiler
     */
    public final IMTCompiler getIMTCompiler() {
        return imtCompiler;
    }

    /**
     * Gets the type size information of this architecture.
     *
     * @return
     */
    public final TypeSizeInfo getTypeSizeInfo() {
        return typeSizeInfo;
    }

    /**
     * @see org.jnode.vm.VmArchitecture#getLogPageSize()
     */
    public final byte getLogPageSize(int region) {
        switch (region) {
            case VirtualMemoryRegion.AVAILABLE:
                return LOG_AVAILABLE_PAGE_SIZE;
            case VirtualMemoryRegion.HEAP:
                return LOG_HEAP_PAGE_SIZE;
            case VirtualMemoryRegion.ACPI:
                return LOG_ACPI_PAGE_SIZE;
            case VirtualMemoryRegion.DEVICE:
                return LOG_DEVICE_PAGE_SIZE;
            default:
                return LOG_DEFAULT_PAGE_SIZE;
        }
    }

    /**
     * @see org.jnode.vm.VmArchitecture#getEnd(org.jnode.vm.VmArchitecture.VirtualMemoryRegion)
     */
    public Address getEnd(int space) {
        switch (space) {
            case VirtualMemoryRegion.HEAP:
                return Address.fromIntZeroExtend(AVAILABLE_END);
            case VirtualMemoryRegion.AVAILABLE:
                return Address.fromIntZeroExtend(AVAILABLE_END);
            case VirtualMemoryRegion.DEVICE:
                return Address.fromIntZeroExtend(DEVICE_END);
            case VirtualMemoryRegion.ACPI:
                return Address.fromIntZeroExtend(ACPI_END);
            default:
                return super.getEnd(space);
        }
    }

    /**
     * @see org.jnode.vm.VmArchitecture#getStart(org.jnode.vm.VmArchitecture.VirtualMemoryRegion)
     */
    public Address getStart(int space) {
        switch (space) {
            case VirtualMemoryRegion.HEAP:
                return Address.fromIntZeroExtend(BOOT_IMAGE_START);
            case VirtualMemoryRegion.AVAILABLE:
                return Address.fromIntZeroExtend(AVAILABLE_START);
            case VirtualMemoryRegion.DEVICE:
                return Address.fromIntZeroExtend(DEVICE_START);
            case VirtualMemoryRegion.ACPI:
                return Address.fromIntZeroExtend(ACPI_START);
            default:
                return super.getStart(space);
        }
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
    public final boolean mmap(int region, Address start,
                              Extent size, Address physAddr) throws UninterruptiblePragma {
        switch (region) {
            case VirtualMemoryRegion.HEAP:
                if (!physAddr.isMax()) {
                    return false;
                }
                break;
            case VirtualMemoryRegion.ACPI:
                if (physAddr.isMax()) {
                    return false;
                }
                break;
            default:
                return false;
        }

        final Word alignedStart = pageAlign(region, start.toWord(), false);
        if (!alignedStart.EQ(start.toWord())) {
            // Make adjustments on size & physAddr
            final Word diff = start.sub(alignedStart).toWord();
            start = alignedStart.toAddress();
            size = size.add(diff);
            if (!physAddr.isMax()) {
                physAddr = physAddr.sub(diff);
            }
        }
        size = pageAlign(region, size.toWord(), true).toExtent();

        if (pageCursor.isZero()) {
            Unsafe.debug("pageCursor is zero");
        }

        final Extent pageSize = getPageSize(region);
        while (!size.isZero()) {
            mapPage(start, physAddr, pageSize, (region == ACPI));
            start = start.add(pageSize);
            size = size.sub(pageSize);
            if (!physAddr.isMax()) {
                physAddr = physAddr.add(pageSize);
            }
        }

        return true;
    }

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
    public boolean munmap(int region, Address start, Extent size)
        throws UninterruptiblePragma {
        switch (region) {
            case VirtualMemoryRegion.HEAP:
            case VirtualMemoryRegion.ACPI:
                break;
            default:
                return false;
        }

        final Word alignedStart = pageAlign(region, start.toWord(), false);
        if (!alignedStart.EQ(start.toWord())) {
            // Make adjustments on size & physAddr
            final Word diff = start.sub(alignedStart).toWord();
            start = alignedStart.toAddress();
            size = size.add(diff);
        }
        size = pageAlign(region, size.toWord(), true).toExtent();
        final Extent pageSize = getPageSize(region);

        removeVirtualMMap(start.toWord(), start.add(size).toWord(), pageSize);
        return true;
    }

    /**
     * Map a page at the given virtual address.
     *
     * @param vmAddress
     */
    private final void mapPage(Address vmAddress, Address physAddr, Extent pageSize, boolean debug) {
        // Setup the pdir structures
        final Word pdirIdx = vmAddress.toWord().rshl(22);
        final Address pdirEntryPtr = UnsafeX86.getCR3().add(pdirIdx.lsh(2));
        Word entry = pdirEntryPtr.loadWord();
        if (entry.and(Word.fromIntZeroExtend(PF_PRESENT)).isZero()) {
            final Word pagePtr;
            if (physAddr.isMax()) {
                // Get a free page
                pagePtr = pageCursor;
                pageCursor = pageCursor.add(pageSize);
            } else {
                pagePtr = physAddr.toWord();
            }

            // There is currently no present page, so do the mapping
            entry = pagePtr.or(Word.fromIntZeroExtend(PF_DEFAULT));
            pdirEntryPtr.store(entry);

            if (debug) {
                Unsafe.debug("mapPage ");
                Unsafe.debug(entry);
                Unsafe.debug('\n');
            }
        } else {
            if (debug) {
                Unsafe.debug("mapPage: page present\n");
            }
        }

    }

    /**
     * @see org.jnode.vm.VmArchitecture#boot()
     */
    protected void boot(boolean emptyMMap) {
        Unsafe.debug("VmArchitecture32#boot\n");
        dumpMultibootMMap();
        pageCursor = getFirstAvailableHeapPage();

        if (emptyMMap) {
            // Remove all page mappings between AVAILABLE_START-END
            final Word start = Word.fromIntZeroExtend(AVAILABLE_START);
            final Word end = Word.fromIntZeroExtend(AVAILABLE_END);
            final Extent pageSize = Extent.fromIntZeroExtend(AVAILABLE_PAGE_SIZE);
            removeVirtualMMap(start, end, pageSize);
        }
    }

    /**
     * Remove all virtual memory mappings in a given address range.
     *
     * @param start
     * @param end
     */
    private final void removeVirtualMMap(Word start, Word end, Extent pageSize) {
        final Address pdir = UnsafeX86.getCR3();

        for (Word ptr = start; ptr.LT(end); ptr = ptr.add(pageSize)) {
            final Word pdirIdx = ptr.rshl(22);
            pdir.add(pdirIdx.lsh(2)).store(Word.zero());
        }
    }
}
