/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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

import org.jnode.system.resource.ResourceManager;

/**
 * @author Levente S\u00e1ntha
 */
abstract class ViaRhineRing<T extends ViaRhineDesc> {
    final int RING_SIZE;
    T[] ring;
    int ringAddr = 0;
    int current;

    @SuppressWarnings("unchecked")
    ViaRhineRing(ResourceManager rm, int size) {
        this.RING_SIZE = size;
        ring = (T[]) new ViaRhineDesc[RING_SIZE];
        for (int i = 0; i < RING_SIZE; i++) {
            ring[i] = createDescr(rm);
            if (i == 0)
                ringAddr = ring[0].descAddr;
            else
                ring[i - 1].setNextDescAddr(ring[i].descAddr);


        }
        ring[RING_SIZE - 1].setNextDescAddr(ringAddr);
    }

    abstract T createDescr(ResourceManager rm);

    T currentDesc() {
        return (T) ring[current];
    }

    void next() {
        current = (current + 1) % RING_SIZE;
    }
}
