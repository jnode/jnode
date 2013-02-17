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

import org.jnode.system.resource.MemoryResource;
import org.jnode.util.NumberUtils;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class MPProcessorEntry extends MPEntry {

    private static final int F_ENABLED = 0x01;
    private static final int F_BOOTSTRAP = 0x02;

    public MPProcessorEntry(MemoryResource mem) {
        super(mem);
    }

    public int getApicID() {
        return mem.getByte(1) & 0xFF;
    }

    /**
     * Gets the flags.
     *
     * @return The flags
     */
    public int getFlags() {
        return mem.getByte(3) & 0xFF;
    }

    /**
     * Is this the bootstrap processor?
     *
     * @return True/false
     */
    public final boolean isBootstrap() {
        return ((getFlags() & F_BOOTSTRAP) != 0);
    }

    /**
     * Is this the bootstrap enabled?
     *
     * @return True/false
     */
    public final boolean isEnabled() {
        return ((getFlags() & F_ENABLED) != 0);
    }

    public int getCpuSignature() {
        return mem.getInt(4);
    }

    public int getFeatures() {
        return mem.getInt(8);
    }

    public final boolean hasHyperThreading() {
        return ((getFeatures() & X86CpuID.FEAT_HTT) != 0);
    }

    public String toString() {
        return super.toString() +
            " ID 0x" + NumberUtils.hex(getApicID(), 2) +
            ", flags 0x" + NumberUtils.hex(getFlags(), 2) +
            ", cpusig 0x" + NumberUtils.hex(getCpuSignature()) +
            ", feat 0x" + NumberUtils.hex(getFeatures());
    }

    /**
     * @see org.jnode.vm.x86.MPEntry#getEntryTypeName()
     */
    public String getEntryTypeName() {
        return "Processor";
    }
}
