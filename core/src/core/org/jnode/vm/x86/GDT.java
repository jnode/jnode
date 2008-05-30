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
import org.jnode.util.NumberUtils;
import org.jnode.vm.annotation.MagicPermission;
import org.vmmagic.unboxed.Address;

/**
 * Global descriptor table wrapper.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@MagicPermission
final class GDT {

    public static final int NULL_ENTRY = 0;

    /**
     * Entry for kernel code (cpl=0)
     */
    public static final int KERNEL_CODE_ENTRY = 1;

    /**
     * Entry for kernel data (cpl=0)
     */
    public static final int KERNEL_DATA_ENTRY = 2;

    /**
     * Entry for user code (cpl=3)
     */
    public static final int USER_CODE_ENTRY = 3;

    /**
     * Entry for user data (cpl=3)
     */
    public static final int USER_DATA_ENTRY = 4;

    /**
     * Entry for tss
     */
    public static final int TSS_ENTRY = 5;

    /**
     * Entry for current processor. Only valid in 32-bit mode
     */
    public static final int PROCESSOR_ENTRY = 6;

    private final int[] gdt;

    /**
     * Initialize this instance.
     */
    public GDT() {
        final int len = UnsafeX86.getGDT(null);
        this.gdt = new int[len];
        UnsafeX86.getGDT(gdt);
    }

    /**
     * Gets the GDT
     */
    public int[] getGdt() {
        return gdt;
    }

    /**
     * Sets the base address of an entry at a given index.
     *
     * @param index
     * @param base
     */
    public final void setBase(int index, Address base) {
        final int intBase = base.toInt();
        final int idx = index * 2;
        gdt[idx + 0] &= 0x0000FFFF; // Remove base bits
        gdt[idx + 1] &= 0x00FFFF00; // Remove base bits
        gdt[idx + 0] |= (intBase & 0x0000FFFF) << 16; // Base 0-15
        gdt[idx + 1] |= (intBase >> 16) & 0x000000FF; // Base 16-23
        gdt[idx + 1] |= intBase & 0xFF000000; // Base 23-31
    }

    public final void dump(PrintStream out) {
        for (int i = 0; i < gdt.length; i += 2) {
            out.println("GDT[" + i + "] " + NumberUtils.hex(gdt[i + 1]) + " " + NumberUtils.hex(gdt[i]));
        }
    }
}
