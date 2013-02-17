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
 
package org.jnode.vm.classmgr;

import org.jnode.vm.InternString;
import org.jnode.vm.objects.BootableHashMap;
import org.jnode.vm.objects.VmSystemObject;

/**
 * This class is used to maintain a mapping between a method signature (name+type)
 * and a unique selector.
 *
 * @author epr
 */
public class SelectorMap extends VmSystemObject {

    private final BootableHashMap<String, Integer> map = new BootableHashMap<String, Integer>(8192);
    private int lastSelector = 1;

    /**
     * Gets the selector for a given name &amp; type
     *
     * @param name
     * @param signature
     * @return The global unique selector
     */
    public int get(String name, String signature) {
        final String id = InternString.internString(name + '#' + signature);
        final Integer selector = (Integer) map.get(id);
        if (selector != null) {
            return selector;
        } else {
            return getNew(id);
        }
    }

    /**
     * Get was not able to get a selector, do a synchronized test
     * and create a new selector if needed.
     *
     * @param id
     * @return The selector
     */
    private synchronized int getNew(String id) {
        Integer selector = (Integer) map.get(id);
        if (selector != null) {
            return selector;
        } else {
            final int sel = ++lastSelector;
            map.put(id, sel);
            return sel;
        }
    }
}
