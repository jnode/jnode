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
 
package org.jnode.vm.facade;

import java.nio.ByteOrder;

import org.jnode.vm.VirtualMemoryRegion;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;

/**
 * Interface with the system architecture.
 * 
 * @author Fabien DUMINY (fduminy at jnode.org)
 *
 */
public interface VmArchitecture {

    /**
     * Gets the byte ordering of this architecture.
     *
     * @return the architecture's ByteOrder
     */
	ByteOrder getByteOrder();

    /**
     * Gets the size in bytes of an object reference.
     *
     * @return the architecture's reference size in bytes; i.e. 4 or 8.
     */
	int getReferenceSize();

    /**
     * Gets the start address of the given space.
     * 
     * @param space a {@link VirtualMemoryRegion}.
     * @return the start address of the region
     */
	Address getStart(int space);

    /**
     * Gets the end address of the given space.
     *
     * @param space a {@link VirtualMemoryRegion}.
     * @return the end address of the region
     */
	Address getEnd(int space);

    /**
     * Gets the memory map of the current system.  If no map has yet been created,
     * it will be created by calling {@link #createMemoryMap()}.
     *
     * @return the architecture's memory map.
     */
	MemoryMapEntry[] getMemoryMap();

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
	boolean mmap(int region, Address start, Extent size, Address physAddr);

    /**
     * Gets the log base two of the size in bytes of an OS page in a given region
     *
     * @param region a {@link VirtualMemoryRegion} value
     * @return the log base two page size
     */
	byte getLogPageSize(int region);

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
	boolean munmap(int region, Address start, Extent size);

    /**
     * Page align a given address (represented as an Address) in a given region.
     *
     * @param region a {@link VirtualMemoryRegion}.
     * @param start an address value
     * @param up If true, the value will be rounded up, otherwise rounded down.
     * @return the corresponding page aligned address represented as a Address. 
     */
	Address pageAlign(int region, Address start, boolean up);

    /**
     * Gets the name of this architecture.
     * This name is the programmers name used to identify packages,
     * class name extensions etc.
     *
     * @return the architecture's name
     */
	String getName();

	/**
	 * Gets the type size information of this architecture.
	 * 
	 * @return the architecture's type size information descriptor
	 */
	TypeSizeInfo getTypeSizeInfo();
}
