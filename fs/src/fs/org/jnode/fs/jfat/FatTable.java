/*
 * $Id$
 *
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
 
package org.jnode.fs.jfat;

import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;

public class FatTable {
    private final Map<FatKey, FatEntry> table = new HashMap<FatKey, FatEntry>();

    public FatTable() {
    }

    public FatEntry get(String name) {
        return table.get(new FatKey(name));
    }

    public FatEntry put(FatEntry entry) {
        FatKey key = new FatKey(entry.getName());
        if (table.containsKey(key)) {
            throw new IllegalArgumentException("shouldn't happen");
        }
        table.put(key, entry);
        return entry;
    }

    public FatEntry remove(FatEntry entry) {
        FatKey key = new FatKey(entry.getName());

        if (!table.containsKey(key))
            throw new IllegalArgumentException("shouldn't happen");

        return table.remove(key);
    }

    public FatEntry put(String name, FatEntry entry) {
        FatKey key = new FatKey(name);

        if (table.containsKey(key))
            throw new IllegalArgumentException("shouldn't happen");

        table.put(key, entry);

        return entry;
    }

    public FatEntry look(FatEntry entry) {
        FatEntry e = get(entry.getName());

        if (e != null)
            return e;
        else
            return put(entry);
    }

    public int size() {
        return table.size();
    }

    /**
     * Print FAT table content.
     */
    public String toString() {
        StrWriter out = null;
        try {
            out = new StrWriter();

            Iterator<FatKey> i = table.keySet().iterator();

            out.println("Entries [");
            while (i.hasNext())
                out.println("\t\t  " + i.next());
            out.print("\t\t]");

            return out.toString();
        } finally {
            out.close();
        }
    }

    private class FatKey {
        private final String key;

        private FatKey(String value) {
            this.key = value;
        }

        private String getKey() {
            return key;
        }

        public int hashCode() {
            return FatUtils.toIgnoreCase(key).hashCode();
        }

        public boolean equals(Object anObject) {
            if (anObject instanceof FatKey) {
                FatKey anotherKey = (FatKey) anObject;
                return FatUtils.compareIgnoreCase(key, anotherKey.getKey());
            }

            if (anObject instanceof String) {
                String anotherString = (String) anObject;
                return FatUtils.compareIgnoreCase(key, anotherString);
            }

            return false;
        }

        public String toString() {
            return key;
        }
    }
}
