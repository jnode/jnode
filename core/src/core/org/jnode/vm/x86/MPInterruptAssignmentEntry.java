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
public abstract class MPInterruptAssignmentEntry extends MPEntry {

    /**
     * @param mem
     */
    MPInterruptAssignmentEntry(MemoryResource mem) {
        super(mem);
    }

    public int getInterruptType() {
        return mem.getByte(1) & 0xFF;
    }

    public int getFlags() {
        return mem.getChar(2);
    }

    public int getSourceBusID() {
        return mem.getByte(4) & 0xFF;
    }

    public int getSourceBusIRQ() {
        return mem.getByte(5) & 0xFF;
    }

    public int getDestinationApicID() {
        return mem.getByte(6) & 0xFF;
    }

    public int getDestinationApicINTN() {
        return mem.getByte(7) & 0xFF;
    }


    /**
     * @see org.jnode.vm.x86.MPEntry#toString()
     */
    public String toString() {
        return super.toString() + " type " + getInterruptType() +
            ", flags 0x" + NumberUtils.hex(getFlags(), 4) +
            ", src bus:0x" + NumberUtils.hex(getSourceBusID(), 2) +
            ",irq:" + getSourceBusIRQ() +
            "), dst ID:0x" + NumberUtils.hex(getDestinationApicID(), 2) +
            ",INTN:" + getDestinationApicINTN() + ')';
    }
}
