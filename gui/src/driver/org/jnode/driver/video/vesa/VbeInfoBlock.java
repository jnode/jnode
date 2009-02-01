/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
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
 
package org.jnode.driver.video.vesa;

import java.util.ArrayList;
import java.util.List;

import org.jnode.vm.Unsafe;
import org.vmmagic.unboxed.Address;

public class VbeInfoBlock {
    private final Address address;

    VbeInfoBlock(Address address) {
        this.address = address;
    }

    public int getCapabilities() {
        return address.add(7).loadShort();
    }

    public boolean isEmpty() {
        return address.isZero() || VesaUtils.isEmpty(address, 8);
    }

    public List<Short> getVideoModeList() {
        List<Short> modes = new ArrayList<Short>();
        Address addr = address.add(14).loadAddress();
        Unsafe.debug("\nvideo mode list at address " + Integer.toHexString(addr.toInt()) + "\n");
        if (!addr.isZero()) {
            short mode = addr.loadShort();
            int counter = 0;
            while ((mode != 0xFFFF) && (counter++ < 100)) {
                modes.add(mode);

                addr = addr.add(2);
                mode = addr.loadShort();
            }
        }

        return modes;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("VESA version ").append(address.add(4).loadShort()).append("\n");

        sb.append("video modes : ");
        for (short mode : getVideoModeList()) {
            sb.append(Integer.toHexString(mode)).append(", ");
        }

        Unsafe.debug("\nsearching video mode 0x140 (800x600x32)...");
        Address addr = address.add(0); // clone
        int offset = -1;
        for (int i = 0; i < 4096; i++) {
            byte b1 = addr.loadByte();
            addr = addr.add(1);

            byte b2 = addr.loadByte();
            addr = addr.add(1);

            if ((b1 == 0x01) && (b2 == 0x40)) {
                offset = i * 2;
                break;
            }
            if ((b2 == 0x01) && (b1 == 0x40)) {
                offset = i * 2;
                break;
            }
        }
        Unsafe.debug("\nend of search");

        sb.append("\nfound 0x140 at offset " +
                ((offset < 0) ? "NOT FOUND" : Integer.toHexString(offset)) + "\n");

        return sb.toString();
    }
}
