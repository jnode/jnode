/*
 * $Id$
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
