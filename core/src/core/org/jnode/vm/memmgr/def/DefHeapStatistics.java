/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */

package org.jnode.vm.memmgr.def;

import java.util.TreeMap;

import org.jnode.util.Counter;
import org.jnode.vm.memmgr.HeapStatistics;

/**
 * @author Martin Husted Hartvig (hagar@jnode.org)
 */

final class DefHeapStatistics extends HeapStatistics {
    
    private TreeMap<String, Counter> countData = new TreeMap<String, Counter>();

    public void add(String className) {
        Counter count = (Counter) countData.get(className);

        if (count == null) {
            count = new Counter(className);

            countData.put(className, count);
        }

        count.inc();
    }

    public String toString() {
        final StringBuffer stringBuffer = new StringBuffer();
        boolean first = true;

        for (Counter c : countData.values()) {
            if (first) {
                first = false; 
            } else {
                stringBuffer.append('\n');
            }
            stringBuffer.append(c);
        }

        return stringBuffer.toString();
    }

}
