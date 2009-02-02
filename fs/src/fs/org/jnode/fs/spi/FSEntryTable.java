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
 
package org.jnode.fs.spi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jnode.fs.FSEntry;

/**
 * A table containing all the entries of a directory. This class and its childs
 * have the responsability to identify an entry by its name (case sensitivity,
 * long file name, ...). The class can limit the number of entries (for root
 * directories ...) if necessary.
 * 
 * @author Fabien DUMINY
 */
public class FSEntryTable extends AbstractFSObject {

    private static final Logger log = Logger.getLogger(FSEntryTable.class);

    /**
     * An empty table that's used as a default table (that can't be modified)
     * for FSDirectory.
     */
    public static final FSEntryTable EMPTY_TABLE = new FSEntryTable() {
        // FIXME ... actually it CAN be modified!!
    };

    /**
     * Map of entries (key=name, value=entry). As a value may be null (a free
     * entry) we must use Hashtable and not Hashtable
     */
    private Map<String, FSEntry> entries; // must be a HashMap

    /**
     * Names of the entries (list of String or null)
     */
    private List<String> entryNames;

    /**
     * Private constuctor for EMPTY_TABLE
     */
    private FSEntryTable() {
        entries = Collections.emptyMap();
        entryNames = Collections.emptyList();
    }

    /**
     * Construct a FSEntryTable from a list of FSEntry
     * 
     * @param fs
     * @param entryList
     */
    public FSEntryTable(AbstractFileSystem fs, List<FSEntry> entryList) {
        super(fs);
        // As a value may be null (a free entry)
        // we must use HashMap and not Hashtable
        this.entries = new HashMap<String, FSEntry>();
        this.entryNames = new ArrayList<String>();

        for (FSEntry entry : entryList) {
            if (entry == null) {
                entries.put(null, null);
                entryNames.add(null);
            } else {
                final String name = normalizeName(entry.getName());
                log.debug("FSEntryTable: adding entry " + name + " (length=+" + name.length() + ")");
                entries.put(name, entry);
                entryNames.add(name);
            }
        }
    }

    protected int addEntry(FSEntry entry) {
        // grow the entry table
        if (entry == null) {
            entryNames.add(null);
        } else {
            entryNames.add(entry.getName());
        }
        return entryNames.size() - 1;
    }

    /**
     * Find the index of free entry. If not found, resize the table is possible.
     * If resize is impossible, an IOException is thrown.
     * 
     * @param entry
     * @return the index of a free entry
     */
    protected int findFreeEntry(FSEntry entry) {
        int size = entryNames.size();
        int freeIndex = -1;
        for (int i = 0; i < size; i++) {
            String n = entryNames.get(i);
            if (n == null) {
                freeIndex = i;
            }
        }

        if (freeIndex < 0) {
            freeIndex = addEntry(null);
        }

        return freeIndex;
    }

    /**
     * Get the entry given by its index. The result can be null.
     * 
     * @param index
     * @return the FSEntry at index
     */
    public final FSEntry get(int index) {
        return get(entryNames.get(index));
    }

    /**
     * Get the entry given by its name. The result can be null.
     * 
     * @param name
     * @return the FSEntry with given name
     */
    public FSEntry get(String name) {
        // name can't be null (it's reserved for free entries)
        if (name == null)
            return null;

        name = normalizeName(name);
        log.debug("get(" + name + ")");
        return entries.get(name);
    }

    /**
     * return a list of FSEntry names
     * 
     * @return a List of FSEntry names
     */
    protected List<String> getEntryNames() {
        return entryNames;
    }

    /**
     * return a list of used FSEntry
     * 
     * @return a list of used FSEntrys
     */
    protected List<FSEntry> getUsedEntries() {
        int nbEntries = entryNames.size();
        ArrayList<FSEntry> used = new ArrayList<FSEntry>(nbEntries / 2);

        for (int i = 0; i < nbEntries; i++) {
            String name = entryNames.get(i);
            if (name != null) {
                used.add(entries.get(name));
            }
        }

        return used;
    }

    /**
     * Get the index of an entry given byt its name. If there is no entry with
     * this name, return -1.
     * 
     * @param name
     * @return the index of the given entry name
     */
    protected int indexOfEntry(String name) {
        return entryNames.indexOf(normalizeName(name));
    }

    /**
     * Indicate if the table need to be saved to the device.
     * 
     * @return if the table needs to be saved to the device
     * @throws IOException
     */
    public final boolean isDirty() throws IOException {
        if (super.isDirty()) {
            return true;
        }

        for (FSEntry entry : entries.values()) {
            if (entry != null) {
                if (entry.isDirty()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Iterator that returns all used entries (ie entries that aren't null)
     * 
     * @return an Iterator with all used entries
     */
    public final Iterator<FSEntry> iterator() {
        return new Iterator<FSEntry>() {
            private int index = 0;

            private List<FSEntry> usedEntries = getUsedEntries();

            public boolean hasNext() {
                return index < usedEntries.size();
            }

            public FSEntry next() {
                final FSEntry entry = usedEntries.get(index);
                index++;
                return entry;
            }

            /**
             * @see java.util.Iterator#remove()
             */
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Return a normalized entry name (for case insensitivity for example)
     * 
     * @param name
     * @return a normalized name (e.g. case insensitiv)
     */
    protected String normalizeName(String name) {
        return name;
    }

    /**
     * Remove an entry given by its name
     * 
     * @param name
     * @return the index of removed entry
     */
    public int remove(String name) {
        name = normalizeName(name);
        int index = entryNames.indexOf(name);
        if (index < 0)
            return -1;

        // in entries and entryNames, a free (deleted) entry
        // is represented by null
        entries.put(name, null);
        entryNames.set(index, null);

        return index;
    }

    /**
     * Rename an entry given by its oldName.
     * 
     * @param oldName
     * @param newName
     * @return the index of renamed file
     */
    public int rename(String oldName, String newName) {
        log.debug("<<< BEGIN rename oldName=" + oldName + " newName=" + newName + " >>>");
        log.debug("rename: table=" + toString());
        oldName = normalizeName(oldName);
        newName = normalizeName(newName);
        log.debug("rename oldName=" + oldName + " newName=" + newName);
        if (!entryNames.contains(oldName)) {
            log.debug("<<< END rename return false (oldName not found) >>>");
            return -1;
        }

        int index = entryNames.indexOf(oldName);
        if (index < 0)
            return -1;

        entryNames.set(index, newName);

        FSEntry entry = entries.remove(oldName);
        entries.put(newName, entry);

        log.debug("<<< END rename return true >>>");
        return index;
    }

    /**
     * Find a free entry in the table and set it with newEntry. If the table is
     * too small, it is resized. If the table can't be resized, an IOException
     * is thrown
     * 
     * @param newEntry
     * @return the index of the stored entry
     * @throws IOException if directory is full (can't be resized)
     */
    public int setFreeEntry(FSEntry newEntry) throws IOException {
        String name = normalizeName(newEntry.getName());
        int index = findFreeEntry(newEntry);
        if (index < 0) {
            log.debug("setFreeEntry: ERROR: entry table is full");
            throw new IOException("Directory is full");
        }

        /* Object oldN = */entryNames.set(index, name);
        /* Object oldE = */entries.put(name, newEntry);

        // entry added, so need to be flushed later
        setDirty();
        return index;
    }

    /**
     * Get the actual size of the table : the number of free entries + the
     * number of used entries.
     * 
     * @return the complete size of the entry table
     */
    public final int size() {
        return entryNames.size();
    }

    /**
     * Return a list of FSEntry representing the content of the table.
     * 
     * @return a list of all FSEntries
     */
    public List/* <FSEntry> */toList() {
        // false means not compacted (ie can contain some null entries)
        return toList(false);
    }

    /**
     * Return a list of FSEntry representing the content of the table. The table
     * can be compacted (ie: without null entries) or uncompacted (with null
     * entries if there are).
     * 
     * @param compacted
     * @return a list of FSEntries
     */
    public List<FSEntry> toList(boolean compacted) {
        ArrayList<FSEntry> entryList = new ArrayList<FSEntry>();

        int nbEntries = entryNames.size();
        for (int i = 0; i < nbEntries; i++) {
            FSEntry entry = get(i);
            if (!compacted || (compacted && (entry != null))) {
                entryList.add(entry);
            }
        }

        return entryList;
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        int nbEntries = entryNames.size();
        String name;
        StringBuilder sb = new StringBuilder(nbEntries * 16);
        for (int i = 0; i < nbEntries; i++) {
            name = entryNames.get(i);
            sb.append("name:").append(name);
            sb.append("->entry:").append(entries.get(name));
            sb.append('\n');
        }

        return sb.toString();
    }
}
