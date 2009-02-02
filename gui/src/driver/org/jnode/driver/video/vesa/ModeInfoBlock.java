/*
 * $Id$
 *
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
 
package org.jnode.driver.video.vesa;

import org.vmmagic.unboxed.Address;

/**
 * 
 * @author Fabien DUMINY (fduminy at jnode.org)
 * 
 */
class ModeInfoBlock {
    private final Address address;

    ModeInfoBlock(Address address) {
        this.address = address;
    }

    short getXResolution() {
        return address.add(18).loadShort();
    }

    short getYResolution() {
        return address.add(20).loadShort();
    }

    byte getBitsPerPixel() {
        return address.add(25).loadByte();
    }

    public int getRamBase() {
        return address.add(40).loadInt();
    }

    public byte getBankSize() {
        return address.add(28).loadByte();
    }

    public byte getNumberOfBanks() {
        return address.add(26).loadByte();
    }

    public short getBytesPerScanLine() {
        return address.add(16).loadByte();
    }

    public boolean isEmpty() {
        return VesaUtils.isEmpty(address, 44);
    }

    @Override
    public String toString() {
        return getXResolution() + "x" + getYResolution() + "x" + getBitsPerPixel();
    }

}
