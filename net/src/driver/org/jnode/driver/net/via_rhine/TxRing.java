/*
 * $Id$
 */
package org.jnode.driver.net.via_rhine;

import org.jnode.system.ResourceManager;
import org.jnode.net.SocketBuffer;

/**
 * @author Levente Sántha
 */
public class TxRing {
    TxDesc[] ring;
    int ringAddr;
    int current;
    TxRing(ResourceManager rm){
        ring = new TxDesc[ViaRhineConstants.TX_RING_SIZE];
        for(int i = 0; i < ViaRhineConstants.TX_RING_SIZE; i++){
            ring[i] = new TxDesc(rm);
            if(i == 0)
                ringAddr = ring[0].descAddr;
            else
                ring[i - 1].setNextDescAddr(ring[i].descAddr);


        }
        ring[ViaRhineConstants.TX_RING_SIZE - 1].setNextDescAddr(ringAddr);
    }


    TxDesc currentDesc(){
        return ring[current];
    }

    void next(){
        current = (current + 1) % ViaRhineConstants.TX_RING_SIZE;
    }
}
