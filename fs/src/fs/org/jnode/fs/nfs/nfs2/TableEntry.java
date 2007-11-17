package org.jnode.fs.nfs.nfs2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TableEntry {

    private Map<String, EntryInfo> entryMap;

    public TableEntry() {
        entryMap = new HashMap<String, EntryInfo>();
    }

    public synchronized NFS2Entry getEntry(String name) {

        EntryInfo entryInfo = entryMap.get(name);

        if (entryInfo == null) {
            return null;
        }

        if (entryInfo.getExpirationTime() < System.currentTimeMillis()) {
            entryMap.remove(name);
            return null;
        }

        return entryInfo.getEntry();
    }

    public synchronized void addEntry(NFS2Entry entry) {

        entryMap.put(entry.getName(), new EntryInfo(entry, System
                .currentTimeMillis() + 30000));

    }

    public synchronized void removeEntry(String name) {
        entryMap.remove(name);
    }

    public synchronized Set<NFS2Entry> getEntrySet() {

        if (entryMap.size() == 0) {
            return new HashSet<NFS2Entry>();
        }

        Set<NFS2Entry> entrySet = new HashSet<NFS2Entry>(entryMap.size());
        for (String name : entryMap.keySet()) {

            NFS2Entry entry = getEntry(name);
            if (entry != null) {
                entrySet.add(entry);
            }

        }

        return entrySet;

    }

    public synchronized void clear() {
        entryMap.clear();
    }

    public synchronized int size() {

        return entryMap.size();
    }

    private class EntryInfo {

        private NFS2Entry entry;
        private long expirationTime;

        public EntryInfo(NFS2Entry entry, long expirationTime) {
            this.entry = entry;
            this.expirationTime = expirationTime;
        }

        public NFS2Entry getEntry() {
            return entry;
        }

        public long getExpirationTime() {
            return expirationTime;
        }

    }

}
