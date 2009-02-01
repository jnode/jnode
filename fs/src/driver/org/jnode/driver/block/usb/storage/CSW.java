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
 
package org.jnode.driver.block.usb.storage;

import org.jnode.driver.bus.usb.USBPacket;
import org.jnode.util.NumberUtils;

public class CSW extends USBPacket {

    public CSW() {
        super(13);
    }

    public void setSignature(int signature) {
        setInt(0, signature);
    }

    public void setFlag(int flag) {
        setInt(4, flag);
    }

    public void setResidue(int residue) {
        setInt(8, residue);
    }

    public void setStatus(byte status) {
        setByte(12, status);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("--- CSW ---").append("\n");
        sb.append("Signature : 0x").append(NumberUtils.hex(getInt(0), 8)).append("\n");
        sb.append("Flag      : 0x").append(NumberUtils.hex(getByte(4), 8)).append("\n");
        sb.append("Residue   : 0x").append(NumberUtils.hex(getByte(8), 8)).append("\n");
        sb.append("Status    : 0x").append(NumberUtils.hex(getByte(12), 2)).append("\n");
        sb.append("Packet    : ").append(super.toString());
        return sb.toString();
    }

}
