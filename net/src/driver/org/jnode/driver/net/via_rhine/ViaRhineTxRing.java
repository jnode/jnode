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
 
package org.jnode.driver.net.via_rhine;

import java.util.Arrays;
import static org.jnode.driver.net.via_rhine.ViaRhineConstants.TX_RING_SIZE;
import org.jnode.net.SocketBuffer;
import static org.jnode.net.ethernet.EthernetConstants.ETH_ZLEN;
import org.jnode.system.ResourceManager;

/**
 * @author Levente S\u00e1ntha
 */
class ViaRhineTxRing extends ViaRhineRing<ViaRhineTxRing.TxDesc> {
    ViaRhineTxRing(ResourceManager rm) {
        super(rm, TX_RING_SIZE);
    }

    TxDesc createDescr(ResourceManager rm) {
        return new TxDesc(rm);
    }

    static class TxDesc extends ViaRhineDesc {
        TxDesc(ResourceManager rm) {
            super(rm);
            setControlStatus();
        }

        private void setControlStatus() {
            descMr.setInt(descOffs + 4, descMr.getInt(descOffs + 4) | 0x00e08000);
        }

        void setFrameLength(int size) {
            descMr.setInt(descOffs + 4, ((descMr.getInt(descOffs + 4) & ~0x000007FF) | (size & 0x000007FF)));
        }

        void setPacket(SocketBuffer sb) {
            int size = sb.getSize();
            log.debug("packetlength: " + size);
            sb.get(data, dataOffs, 0, size);

            if (size < ETH_ZLEN) {
                Arrays.fill(data, dataOffs + size, dataOffs + ETH_ZLEN, (byte) 0);
                setFrameLength(ETH_ZLEN);
            } else {
                setFrameLength(size);
            }
        }
    }
}
