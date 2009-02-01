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

import org.jnode.system.MemoryResource;
import org.jnode.util.NumberUtils;
import org.jnode.vm.annotation.MagicPermission;
import org.vmmagic.unboxed.Address;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@MagicPermission
class MPIOAPICEntry extends MPEntry {

    /**
     * @param mem
     */
    MPIOAPICEntry(MemoryResource mem) {
        super(mem);
    }

    public int getApicID() {
        return mem.getByte(1) & 0xFF;
    }

    public int getApicVersion() {
        return mem.getByte(2) & 0xFF;
    }

    public int getFlags() {
        return mem.getByte(3) & 0xFF;
    }

    public Address getAddress() {
        return Address.fromIntZeroExtend(mem.getInt(4));
    }


    /**
     * @see org.jnode.vm.x86.MPEntry#toString()
     */
    public String toString() {
        return super.toString() + " ID 0x" + NumberUtils.hex(getApicID(), 2) +
            ", version " + getApicVersion() +
            ", flags 0x" + NumberUtils.hex(getFlags(), 2) +
            ", addr 0x" + NumberUtils.hex(getAddress().toInt());
    }

    /**
     * @see org.jnode.vm.x86.MPEntry#getEntryTypeName()
     */
    public String getEntryTypeName() {
        return "I/O APIC";
    }
}
