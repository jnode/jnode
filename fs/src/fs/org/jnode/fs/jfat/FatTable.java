/*
 *
 */
package org.jnode.fs.jfat;

import java.util.Iterator;
import java.util.Hashtable;

public class FatTable {
    private final Hashtable<FatKey, FatEntry> table = new Hashtable<FatKey, FatEntry>();

    public FatTable() {
    }

    public FatEntry get(String name) {
        return table.get(new FatKey(name));
    }

    public FatEntry put(FatEntry entry) {
        if (table.containsKey(entry.getName()))
            throw new IllegalArgumentException("shouldn't happen");

        table.put(new FatKey(entry.getName()), entry);

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

    public String toString() {
        StrWriter out = new StrWriter();

        Iterator<FatKey> i = table.keySet().iterator();

        out.println("Entries [");
        while (i.hasNext())
            out.println("\t\t  " + i.next());
        out.print("\t\t]");

        return out.toString();
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
