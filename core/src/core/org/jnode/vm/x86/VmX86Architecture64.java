/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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

import org.jnode.vm.VirtualMemoryRegion;
import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.classmgr.TypeSizeInfo;
import org.jnode.vm.classmgr.VmIsolatedStatics;
import org.jnode.vm.classmgr.VmSharedStatics;
import org.jnode.vm.compiler.IMTCompiler;
import org.jnode.vm.scheduler.VmProcessor;
import org.jnode.vm.scheduler.VmScheduler;
import org.jnode.vm.x86.compiler.X86IMTCompiler64;
import org.vmmagic.pragma.UninterruptiblePragma;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@MagicPermission
public final class VmX86Architecture64 extends VmX86Architecture {

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
     * Start address of the virtual memory region available to the memory manager (4Gb).
     * This address must be 4Mb aligned.
     */
    public static final long AVAILABLE_START = 0x0000000100000000L;

    /**
     * End address of the virtual memory region  available to the memory manager (8Gb)
     * This address must be 4Mb aligned.
     */
    public static final long AVAILABLE_END = 0x0000000200000000L;

    /**
     * Size of an object reference
     */
    public static final int SLOT_SIZE = 8;

    /**
     * The IMT compiler
     */
    private final X86IMTCompiler64 imtCompiler;

    /**
     * The type size information
     */
    private final TypeSizeInfo typeSizeInfo;

    /**
     * Initialize this instance.
     */
    public VmX86Architecture64() {
        this("L1A");
    }

    /**
     * Initialize this instance.
     *
     * @param compiler
     */
    public VmX86Architecture64(String compiler) {
        super(SLOT_SIZE, compiler);
        this.imtCompiler = new X86IMTCompiler64();
        this.typeSizeInfo = new TypeSizeInfo(1, 1, 2, 2, 1);
    }

    /**
     * @see org.jnode.vm.VmArchitecture#createProcessor(int,
     *      org.jnode.vm.classmgr.VmStatics)
     */
    public final VmProcessor createProcessor(int id, VmSharedStatics sharedStatics, VmIsolatedStatics isolatedStatics,
                                             VmScheduler scheduler) {
        return new VmX86Processor64(id, this, sharedStatics, isolatedStatics, scheduler, null);
    }

    /**
     * @see org.jnode.vm.VmArchitecture#getIMTCompiler()
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
        return 22; // 4Mb
    }

    /**
     * @see org.jnode.vm.VmArchitecture#getEnd(org.jnode.vm.VmArchitecture.VirtualMemoryRegion)
     */
    public Address getEnd(int space) {
        switch (space) {
            case VirtualMemoryRegion.HEAP:
                return Address.fromLong(AVAILABLE_END);
            case VirtualMemoryRegion.AVAILABLE:
                return Address.fromLong(AVAILABLE_END);
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
                return Address.fromLong(AVAILABLE_START);
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
    public final boolean mmap(int space, Address start, Extent size, Address physAddr)
        throws UninterruptiblePragma {
        if (space != VirtualMemoryRegion.HEAP) {
            return false;
        }

        return false;
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
        return false;
    }

    /**
     * @see org.jnode.vm.VmArchitecture#boot()
     */
    protected void boot(boolean emptyMMap) {
        dumpMultibootMMap();
    }
}
