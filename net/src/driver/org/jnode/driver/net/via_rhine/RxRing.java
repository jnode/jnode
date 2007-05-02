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
public class RxRing {
    RxDesc[] ring;
    int ringAddr;
    int current;
    RxRing(ResourceManager rm){
        ring = new RxDesc[RX_RING_SIZE];
        for(int i = 0; i < RX_RING_SIZE; i++){
            ring[i] = new RxDesc(rm);
            if(i == 0)
                ringAddr = ring[0].descAddr;
            else
                ring[i - 1].setNextDescAddr(ring[i].descAddr);


        }
        ring[RX_RING_SIZE - 1].setNextDescAddr(ringAddr);        
    }


    SocketBuffer getPacket(){
        return ring[current].getPacket();
    }

    RxDesc currentDesc(){
        return ring[current];
    }

    void next(){
        current = (current + 1) % RX_RING_SIZE;
    }
}
