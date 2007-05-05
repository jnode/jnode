/*
 * $Id$
 */
package org.jnode.driver.net.via_rhine;

import static org.jnode.driver.net.via_rhine.ViaRhineConstants.*;
import org.jnode.system.ResourceManager;
import org.jnode.net.SocketBuffer;

/**
 * @author Levente Sántha
 */
class ViaRhineRxRing extends ViaRhineRing<ViaRhineRxRing.RxDesc> {
    ViaRhineRxRing(ResourceManager rm){
        super(rm, RX_RING_SIZE);
    }

    RxDesc createDescr(ResourceManager rm) {
        return new RxDesc(rm);
    }

    static class RxDesc extends ViaRhineDesc{
        RxDesc(ResourceManager rm){
            super(rm);
            setOwnBit();
            setDataBufferSize(PKT_BUF_SZ);
        }

        private void setDataBufferSize(int size) {
            descMr.setInt(descOffs + 4, size);
        }

        int getFrameLength(){
            return descMr.getChar(descOffs + 2) & 0x000007FF;
        }

        SocketBuffer getPacket(){
            int ln = getFrameLength();
            log.debug("packetlength: " + ln);
            byte[] buf = new byte[ln];
            System.arraycopy(data, dataOffs, buf, 0, ln);
            return new SocketBuffer(buf, 0, ln);
        }
    }
}
