/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.driver.system.ram;

import java.util.ArrayList;
import java.util.List;

/**
 * RAMModuleCollection.
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Licence: GNU LGPL
 * </p>
 * <p>
 * </p>
 *
 * @author Francois-Frederic Ozog
 * @version 1.0
 */
public class RAMModuleCollection {

    private final List<RAMModuleInfo> slots;

    /**
     * Creates the list of slots
     *
     * @param slots number of possible slots
     */
    public RAMModuleCollection(int slots) {
        this.slots = new ArrayList<RAMModuleInfo>(slots);
    }

    /**
     * Retrieves the actual total number of slots, empty or occupied
     */
    public int capacity() {
        return slots.size();
    }

    /**
     * Retrieves the actual number of occupied slots
     *
     * @return number of occupied slots
     */
    public int size() {
        int s = 0;
        for (int i = 0; i < slots.size(); i++)
            s += slots.get(i) != null ? 1 : 0;
        return s;
    }

    /**
     * get details on one slot
     *
     * @param index slot number
     * @return RAM module information
     * @see org.jnode.driver.system.ram.RAMModuleInfo
     */
    public RAMModuleInfo getSlot(int index) {
        return slots.get(index);
    }
}
