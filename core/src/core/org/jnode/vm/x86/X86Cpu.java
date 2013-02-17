/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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

import static org.jnode.vm.x86.X86Vendor.AMD;
import static org.jnode.vm.x86.X86Vendor.INTEL;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public enum X86Cpu {

    // Intel CPU's
    PENTIUM("Pentium", INTEL, 0x05, new int[]{0x01, 0x02}),
    PENTIUM_MMX("Pentium with MMX", INTEL, 0x05, 0x04),
    PENTIUMPRO("Pentium Pro", INTEL, 0x06, 0x01),
    PENTIUM2("Pentium II", INTEL, 0x06, new int[]{0x03, 0x05}),
    CELERON("Celeron", INTEL, 0x06, 0x06),
    PENTIUM3("Pentium III", INTEL, 0x06, new int[]{0x07, 0x08, 0x0A, 0x0B}),
    PENTIUMM("Pentium M", INTEL, 0x06, new int[]{0x09, 0x0D}),
    PENTIUM4("Pentium 4", INTEL, 0x0F, new int[]{0x00, 0x01, 0x02, 0x03, 0x04}),

    // AMD CPU's
    ATHLON("Athlon", AMD, 0x06, new int[]{0x01, 0x02, 0x04, 0x06, 0x07, 0x08, 0x0A}),
    ATHLON64("Athlon64", AMD, 0x0F, new int[]{0x04, 0x08, 0x0B, 0x0C, 0x0E, 0x0F}),
    OPTERON("Opteron", AMD, 0x0F, new int[]{0x05}),

    // Other CPU's
    UNKNOWN("?", X86Vendor.UNKNOWN, 0, 0);    

    private final String name;
    private final X86Vendor vendor;
    private final int family;
    private final int[] model;

    private X86Cpu(String name, X86Vendor vendor, int family, int model) {
        this(name, vendor, family, new int[]{model});
    }

    private X86Cpu(String name, X86Vendor vendor, int family, int[] model) {
        this.name = name;
        this.vendor = vendor;
        this.family = family;
        this.model = model;
    }

    /**
     * Get the CPU type for the CPU with the given 'id'.
     *
     * @param id
     * @return the CPU, or {@link #UNKNOWN} the 'id' is not known.
     */
    public static final X86Cpu find(X86CpuID id) {
        final String vendor = id.getVendor();
        for (X86Cpu c : values()) {
            if (c.vendor.getId().equals(vendor)) {
                if (c.family == id.getFamily()) {
                    for (int m : c.model) {
                        if (id.getModel() == m) {
                            return c;
                        }
                    }
                }
            }
        }
        return UNKNOWN;
    }

    /**
     * @return Returns the name.
     */
    public final String getName() {
        return name;
    }
}
