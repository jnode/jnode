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

import static org.jnode.driver.net.via_rhine.ViaRhineConstants.PKT_BUF_SZ;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.RX_RING_SIZE;
import org.jnode.net.SocketBuffer;
import org.jnode.system.ResourceManager;

/**
 * @author Levente S\u00e1ntha
 */
class ViaRhineRxRing extends ViaRhineRing<ViaRhineRxRing.RxDesc> {
    ViaRhineRxRing(ResourceManager rm) {
        super(rm, RX_RING_SIZE);
    }

    RxDesc createDescr(ResourceManager rm) {
        return new RxDesc(rm);
    }

    static class RxDesc extends ViaRhineDesc {
        RxDesc(ResourceManager rm) {
            super(rm);
            setOwnBit();
            setDataBufferSize(PKT_BUF_SZ);
        }

        private void setDataBufferSize(int size) {
            descMr.setInt(descOffs + 4, size);
        }

        int getFrameLength() {
            return descMr.getChar(descOffs + 2) & 0x000007FF;
        }

        SocketBuffer getPacket() {
            int ln = getFrameLength();
            log.debug("packetlength: " + ln);
            byte[] buf = new byte[ln];
            System.arraycopy(data, dataOffs, buf, 0, ln);
            return new SocketBuffer(buf, 0, ln);
        }
    }
}
