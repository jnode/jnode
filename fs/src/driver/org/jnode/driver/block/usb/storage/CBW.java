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

public class CBW extends USBPacket {

    public CBW() {
        super(31);
    }

    public void setSignature(int signature) {
        setInt(0, signature);
    }

    public void setTag(int tag) {
        setInt(4, tag);
    }

    public void setDataTransferLength(int dataTransferLength) {
        setInt(8, dataTransferLength);
    }

    public void setFlags(byte flags) {
        setByte(12, flags);
    }

    public void setLun(byte lun) {
        setByte(13, lun);
    }

    public void setLength(byte length) {
        setByte(14, (length & 0x07));
    }

    public void setCdb(byte[] cdb) {
        for (int offset = 0; offset < cdb.length; offset++) {
            setByte((offset + 15), cdb[offset]);
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("--- CBW ---").append("\n");
        sb.append("Signature : 0x").append(NumberUtils.hex(getInt(0), 8)).append("\n");
        sb.append("Tag       : 0x").append(NumberUtils.hex(getInt(4), 8)).append("\n");
        sb.append("DTL       : 0x").append(NumberUtils.hex(getInt(8), 8)).append("\n");
        sb.append("Flag      : 0x").append(NumberUtils.hex(getByte(12), 2)).append("\n");
        sb.append("Lun       : 0x").append(NumberUtils.hex(getByte(13), 2)).append("\n");
        sb.append("Length    : 0x").append(NumberUtils.hex(getByte(14), 2)).append("\n");
        sb.append("Packet    : ").append(super.toString());
        return sb.toString();
    }
}
