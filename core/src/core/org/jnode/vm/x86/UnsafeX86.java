/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.vm.x86;

import org.vmmagic.unboxed.Address;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class UnsafeX86 {

    /**
     * Copies the bootstrap GDT.
     * @param gdt 
     * @return The array length of the required array.
     */
    static final native int getGDT(int[] gdt);
    
    /**
     * Copies the bootstrap TSS.
     * @param tss
     * @return The array length of the required array.
     */
    static final native int getTSS(int[] tss);

    /**
     * Gets the size required for the bootcode for an application processor.
     * @return The required size in bytes.
     */
    static final native int getAPBootCodeSize();
    
    /**
     * Prepare the bootcode.
     * This will copy the bootcode into the memory region and patches the
     * appropriate pointers.
     * @param memory
     * @param gdt
     */
    static final native void setupBootCode(Address memory, int[] gdt, int[] tss);
    
    /**
     * Gets the CR3 register.
     * @return
     */
    static final native Address getCR3();
    
    /**
     * Gets the address of first entry in the multiboot mmap table.
     * @return
     */
    static final native Address getMultibootMMap();
    
    /**
     * Gets the number of entries in the multiboot mmap table.
     * @return
     */
    static final native int getMultibootMMapLength();
    
    /**
     * Merge 32-bit ARGB values at the given memory address.
     * @param src The source address (points to 32-bit ARGB int's)
     * @param dst The destination address (points to 32-bit RGB int's)
     * @param length The number of 32-bit int's to merge.
     */
    static final native void setARGB32bppMMX(Address src, Address dst, int length);
}
