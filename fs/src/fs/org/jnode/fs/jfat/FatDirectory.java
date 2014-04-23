/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import org.apache.log4j.Logger;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSDirectoryId;
import org.jnode.fs.FSEntry;

public class FatDirectory extends FatEntry implements FSDirectory, FSDirectoryId {
    private static final int MAXENTRIES = 65535; // 2^16-1; fatgen 1.03, page 33

    private static final Logger log = Logger.getLogger(FatDirectory.class);

    private final FatTable children = new FatTable();

    /**
     * The map of ID -> entry.
     */
    private final Map<String, FatEntry> idMap = new HashMap<String, FatEntry>();

    /*
     * for root directory
     */
    protected FatDirectory(FatFileSystem fs) {
        super(fs);
    }

    /*
     * from a directory record;
     */
    public FatDirectory(FatFileSystem fs, FatDirectory parent, FatRecord record) {
        super(fs, parent, record);
    }

    /*
     * initialize a new created directory
     */
    private void initialize() throws IOException {
        FatFileSystem fs = getFatFileSystem();
        FatDirectory parent = getParent();
        FatShortDirEntry entry = getEntry();
        FatChain chain = getChain();

        chain.allocateAndClear(1);

        int parentCluster = parent.isRoot() ? 0 : parent.getEntry().getStartCluster();
        int thisCluster = chain.getStartCluster();

        FatDotDirEntry dot = new FatDotDirEntry(fs, false, entry, thisCluster);
        FatDotDirEntry dotDot = new FatDotDirEntry(fs, true, entry, parentCluster);

        setFatDirEntry(dot);
        setFatDirEntry(dotDot);
    }

    /*
     * this is actually a FatDirEntry factory and not a standard read method ...
     * but how would you call it?
     */
    public FatDirEntry getFatDirEntry(int index, boolean allowDeleted) throws IOException {
        FatMarshal entry = new FatMarshal(FatDirEntry.LENGTH);
        getChain().read(index * entry.length(), entry.getByteBuffer());
        return FatDirEntry.create(getFatFileSystem(), entry, index, allowDeleted);
    }

    /*
     * this instead is a "write" method: it needs a "created" entry
     */
    public void setFatDirEntry(FatDirEntry entry) throws IOException {
        getChain().write(entry.getIndex() * entry.length(), entry.getByteBuffer());
    }

    public FatDirEntry[] getFatFreeEntries(int n) throws IOException {
        int i = 0;
        int index = 0;
        FatDirEntry entry = null;
        FatDirEntry[] entries = new FatDirEntry[n];

        while (i < n) {
            try {
                entry = getFatDirEntry(index, false);
                index++;
            } catch (NoSuchElementException ex) {
                if (index > MAXENTRIES)
                    throw new IOException("Directory is full");
                getChain().allocateAndClear(1);
                // restart the search, fixes infinite loop
                // TODO review it for a better solution
                i = 0;
                index = 0;
                continue;
            }

            if (entry.isFreeDirEntry() || entry.isLastDirEntry()) {
                entries[i] = entry;
                i++;
            } else {
                i = 0;
            }
        }

        return entries;
    }

    @Override
    public String getDirectoryId() {
        return Integer.toString(getStartCluster());
    }

    public boolean isDirectory() {
        return true;
    }

    public FSDirectory getDirectory() {
        return this;
    }

    protected FatTable getVisitedChildren() {
        return children;
    }

    public Iterator<FSEntry> iterator() {
        return new EntriesIterator(children, this, false);
    }

    /**
     * Creates a new directory entry iterator.
     *
     * @param includeDeleted {@code true} if deleted files and directory entries should be returned, {@code false}
     *                       otherwise.
     * @return the iterator.
     */
    public Iterator<FSEntry> createIterator(boolean includeDeleted) {
        return new EntriesIterator(new FatTable(), this, includeDeleted);
    }

    /*
     * used from a FatRootDirectory looking for its label
     */
    protected void scanDirectory() {
        EntriesFactory f = new EntriesFactory(this, false);

        while (f.hasNextEntry())
            f.createNextEntry();
    }

    public synchronized FSEntry getEntry(String name) {
        FatEntry child = children.get(name);

        if (child == null) {
            EntriesFactory f = new EntriesFactory(this, false);

            while (f.hasNextEntry()) {
                FatEntry entry = f.createNextEntry();
                if (FatUtils.compareIgnoreCase(entry.getName(), name)) {
                    child = children.put(entry);
                    break;
                }
            }
        }

        return child;
    }

    @Override
    public FSEntry getEntryById(String id) throws IOException {
        FatEntry child = idMap.get(id);

        if (child == null) {
            EntriesFactory f = new EntriesFactory(this, true);

            while (f.hasNextEntry()) {
                FatEntry entry = f.createNextEntry();
                idMap.put(entry.getId(), entry);
            }

            return idMap.get(id);
        }

        return child;
    }

    public FatEntry getEntryByShortName(byte[] shortName) {
        FatEntry child = null;
        EntriesFactory f = new EntriesFactory(this, false);

        while (f.hasNextEntry()) {
            FatEntry entry = f.createNextEntry();
            if (entry.isShortName(shortName)) {
                child = entry;
                break;
            }
        }

        return child;
    }

    public FatEntry getEntryByName(String name) {
        FatEntry child = null;
        EntriesFactory f = new EntriesFactory(this, false);

        while (f.hasNextEntry()) {
            FatEntry entry = f.createNextEntry();
            if (FatUtils.compareIgnoreCase(entry.getName(), name)) {
                child = entry;
                break;
            }
        }

        return child;
    }

    public boolean collide(byte[] shortName) {
        return !(getEntryByShortName(shortName) == null);
    }

    public boolean collide(String name) {
        return !(getEntryByName(name) == null);
    }

    public boolean isEmpty() {
        if (isRoot())
            return false;
        Iterator<FSEntry> i = iterator();
        while (i.hasNext()) {
            String name = i.next().getName();
            if (!name.equals(".") && !name.equals(".."))
                return false;
        }
        return true;
    }

    public synchronized FSEntry addFile(String name) throws IOException {
        FatName fatName = new FatName(this, name);
        if (collide(fatName.getLongName()))
            throw new IOException("File [" + fatName.getLongName() + "] already exists");
        FatRecord record = new FatRecord(this, fatName);
        record.getShortEntry().setArchive();
        FatFile file = new FatFile(getFatFileSystem(), this, record);
        file.flush();

        FatEntry entry = children.put(file);
        idMap.put(entry.getId(), entry);
        return entry;
    }

    public synchronized FSEntry addDirectory(String name) throws IOException {
        FatFileSystem fs = getFatFileSystem();
        FatName fatName = new FatName(this, name);
        if (collide(fatName.getLongName()))
            throw new IOException("File [" + fatName.getLongName() + "] already exists");
        FatRecord record = new FatRecord(this, fatName);
        record.getShortEntry().setDirectory();
        FatDirectory dir = new FatDirectory(fs, this, record);
        dir.initialize();
        dir.flush();

        FatEntry entry = children.put(dir);
        idMap.put(entry.getId(), entry);
        return entry;
    }

    public synchronized void remove(String name) throws IOException {
        FatEntry entry = (FatEntry) getEntry(name);
        if (entry == null)
            throw new FileNotFoundException(name);
        if (entry.isFile()) {
            FatFile file = (FatFile) entry.getFile();
            children.remove(entry);
            file.delete();
            file.freeAllClusters();
            file.flush();
        } else {
            FatDirectory dir = (FatDirectory) entry;
            if (!dir.isEmpty())
                throw new UnsupportedOperationException("Directory is not empty: " + name);
            children.remove(entry);
            dir.delete();
            dir.freeAllClusters();
            dir.flush();
        }

        idMap.remove(entry.getId());
    }

    @Override
    public String toString() {
        return String.format("FatDirectory [%s] index:%d", getName(), getIndex());
    }

    public String toDebugString() {
        StrWriter out = new StrWriter();
        out.println("*******************************************");
        out.println("FatDirectory");
        out.println("*******************************************");
        out.println("Index\t\t" + getIndex());
        out.println(toStringValue());
        out.println("Visited\t\t" + getVisitedChildren());
        out.print("*******************************************");
        return out.toString();
    }

    public static class EntriesIterator extends EntriesFactory implements Iterator<FSEntry> {

        private final FatTable fatTable;

        public EntriesIterator(FatTable fatTable, FatDirectory directory, boolean includeDeleted) {
            super(directory, includeDeleted);

            this.fatTable = fatTable;
        }

        public boolean hasNext() {
            return hasNextEntry();
        }

        public FSEntry next() {
            if (includeDeleted) {
                return createNextEntry();
            } else {
                return fatTable.look(createNextEntry());
            }
        }

        public void remove() {
            throw new UnsupportedOperationException("remove");
        }
    }

    private static class EntriesFactory {
        private boolean label;
        private int index;
        private int next;
        private FatEntry entry;
        protected boolean includeDeleted;
        private FatDirectory directory;

        protected EntriesFactory(FatDirectory directory, boolean includeDeleted) {
            label = false;
            index = 0;
            next = 0;
            entry = null;
            this.includeDeleted = includeDeleted;
            this.directory = directory;
        }

        protected boolean hasNextEntry() {
            int i;
            FatDirEntry e;
            FatRecord v = new FatRecord();

            if (index > MAXENTRIES)
                log.debug("Full Directory: invalid index " + index);

            for (i = index;; ) {
                /*
                 * create a new entry from the chain
                 */
                try {
                    e = directory.getFatDirEntry(i, includeDeleted);
                    i++;
                } catch (NoSuchElementException ex) {
                    entry = null;
                    return false;
                } catch (IOException ex) {
                    log.debug("cannot read entry " + i);
                    i++;
                    continue;
                }

                if (e.isFreeDirEntry() && e.isLongDirEntry() && includeDeleted) {
                    // Ignore damage on deleted long directory entries
                    ((FatLongDirEntry) e).setDamaged(false);
                }

                if (e.isFreeDirEntry() && !includeDeleted) {
                    v.clear();
                } else if (e.isLongDirEntry()) {
                    FatLongDirEntry l = (FatLongDirEntry) e;
                    if (l.isDamaged()) {
                        log.debug("Damaged entry at " + (i - 1));
                        v.clear();
                    } else {
                        v.add(l);
                    }
                } else if (e.isShortDirEntry()) {
                    FatShortDirEntry s = (FatShortDirEntry) e;
                    if (s.isLabel()) {
                        if (directory.isRoot()) {
                            FatRootDirectory r = (FatRootDirectory) directory;
                            if (label) {
                                log.debug("Duplicated label in root directory");
                            } else {
                                r.setEntry(s);
                                label = true;
                            }
                        } else {
                            log.debug("Volume label in non root directory");
                        }
                    } else {
                        break;
                    }
                } else if (e.isLastDirEntry()) {
                    entry = null;
                    return false;
                } else
                    throw new UnsupportedOperationException(
                        "FatDirEntry is of unknown type, shouldn't happen");
            }

            if (!e.isShortDirEntry())
                throw new UnsupportedOperationException("shouldn't happen");

            v.close((FatShortDirEntry) e);

            /*
             * here recursion is in action for the entries factory it creates
             * directory nodes and file leafs
             */
            if (((FatShortDirEntry) e).isDirectory())
                this.entry = new FatDirectory(directory.getFatFileSystem(), directory, v);
            else
                this.entry = new FatFile(directory.getFatFileSystem(), directory, v);

            this.next = i;

            return true;
        }

        protected FatEntry createNextEntry() {
            if (index == next)
                hasNextEntry();
            if (entry == null)
                throw new NoSuchElementException();
            index = next;
            return entry;
        }
    }
}
