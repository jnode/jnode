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

import org.jnode.system.ResourceManager;

/**
 * @author Levente S\u00e1ntha
 */
abstract class ViaRhineRing<T extends ViaRhineDesc> {
    final int RING_SIZE;
    ViaRhineDesc[] ring;
    int ringAddr;
    int current;

    ViaRhineRing(ResourceManager rm, int size) {
        this.RING_SIZE = size;
        ring = new ViaRhineDesc[RING_SIZE];
        for (int i = 0; i < RING_SIZE; i++) {
            ring[i] = createDescr(rm);
            if (i == 0)
                ringAddr = ring[0].descAddr;
            else
                ring[i - 1].setNextDescAddr(ring[i].descAddr);


        }
        ring[RING_SIZE - 1].setNextDescAddr(ringAddr);
    }

    abstract ViaRhineDesc createDescr(ResourceManager rm);

    T currentDesc() {
        return (T) ring[current];
    }

    void next() {
        current = (current + 1) % RING_SIZE;
    }
}
