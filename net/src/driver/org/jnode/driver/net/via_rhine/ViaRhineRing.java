/*
 * $Id$
 */
package org.jnode.driver.net.via_rhine;

import org.jnode.system.ResourceManager;
import java.util.Arrays;

/**
 * @author Levente Sántha
 */
abstract class  ViaRhineRing<T extends ViaRhineDesc> {
    final int RING_SIZE;
    ViaRhineDesc[] ring;
    int ringAddr;
    int current;

    ViaRhineRing(ResourceManager rm, int size){
        this.RING_SIZE = size;
        ring = new ViaRhineDesc[RING_SIZE];
        for(int i = 0; i < RING_SIZE; i++){
            ring[i] = createDescr(rm);
            if(i == 0)
                ringAddr = ring[0].descAddr;
            else
                ring[i - 1].setNextDescAddr(ring[i].descAddr);


        }
        ring[RING_SIZE - 1].setNextDescAddr(ringAddr);
    }

    abstract ViaRhineDesc createDescr(ResourceManager rm);

    T currentDesc(){
        return (T) ring[current];
    }

    void next(){
        current = (current + 1) % RING_SIZE;
    }
}
