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
 
package org.jnode.driver.net.via_rhine;

import org.apache.log4j.Logger;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.PKT_BUF_SZ;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;
import org.vmmagic.unboxed.Address;

/**
 * @author Levente S\u00e1ntha
 */
class ViaRhineDesc {
    private static final int OWN_BIT_MASK = 0x80000000;
    protected final Logger log = Logger.getLogger(getClass());
    byte[] desc;
    int descOffs;
    MemoryResource descMr;
    byte[] data;
    int dataOffs;
    MemoryResource dataMr;
    int descAddr;

    public ViaRhineDesc(ResourceManager rm) {
        desc = new byte[16 + 32];
        descMr = rm.asMemoryResource(desc);
        Address descma = descMr.getAddress();
        descOffs = align32(descma);
        descAddr = descma.add(descOffs).toInt();
        data = new byte[PKT_BUF_SZ + 32];
        dataMr = rm.asMemoryResource(data);
        Address datma = dataMr.getAddress();
        dataOffs = align32(datma);
        descMr.setInt(descOffs + 8, datma.add(dataOffs).toInt());
    }

    void setOwnBit() {
        descMr.setInt(descOffs, descMr.getInt(descOffs) | OWN_BIT_MASK);
    }

    boolean isOwnBit() {
        return (descMr.getInt(descOffs) & OWN_BIT_MASK) != 0;
    }

    void setNextDescAddr(int addr) {
        descMr.setInt(descOffs + 12, addr);
    }

    /**
     * Align an addres on 32-byte boundary.
     *
     * @param addr the address
     * @return the the aligned address offset relative to addr
     */
    private int align32(Address addr) {
        int i_addr = addr.toInt();
        int offs = 0;

        while ((i_addr & 31) != 0) {
            i_addr++;
            offs++;
        }

        return offs;
    }
}
