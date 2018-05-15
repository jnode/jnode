/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
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
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSDirectoryId;
import org.jnode.fs.FSEntry;

public class FatDirectory extends FatEntry implements FSDirectory, FSDirectoryId {
    public static final int MAXENTRIES = 65535; // 2^16-1; fatgen 1.03, page 33

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

    /**
     * Constructs a new FAT directory from a directory record.
     *
     * @param fs the file system.
     * @param parent the parent directory.
     * @param record the FAT record.
     */
    public FatDirectory(FatFileSystem fs, FatDirectory parent, FatRecord record) {
        super(fs, parent, record, true);
    }

    /**
     * Constructs a new FAT directory from a directory record.
     *
     * @param fs the file system.
     * @param parent the parent directory.
     * @param record the FAT record.
     * @param performValidation a flag indicating whether to perform validation on the data passed in.
     */
    public FatDirectory(FatFileSystem fs, FatDirectory parent, FatRecord record, boolean performValidation) {
        super(fs, parent, record, performValidation);
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
        return createDirEntry(entry, index, allowDeleted);
    }

    /**
     * Creates a new FAT directory entry.
     *
     * @param entry the FAT entry buffer.
     * @param index the index of the entry.
     * @param allowDeleted {@code true} to allow deleted entries to be returned, {@code false} to only allow live
     *     entries.
     * @return the FAT directory entry.
     */
    public FatDirEntry createDirEntry(FatMarshal entry, int index, boolean allowDeleted)
        throws IOException {
        int flag;
        FatAttr attr;

        flag = entry.getUInt8(0);
        attr = new FatAttr(entry.getUInt8(11));
        boolean free = flag == FatDirEntry.FREE;

        switch (flag) {
            case FatDirEntry.EOD:
                return new FatDirEntry(getFatFileSystem(), entry, index, flag);
            case FatDirEntry.FREE:
                if (!allowDeleted) {
                    return new FatDirEntry(getFatFileSystem(), entry, index, flag);
                } else {
                    // Fall through...
                    break;
                }
            case FatDirEntry.INVALID:
                throw new IOException("Invalid entry for index: " + index);
        }

        FatDirEntry fatDirEntry;
        // 0xffffffff is the end of long file name marker
        if (attr.isLong() || entry.getUInt32(28) == 0xffffffffL) {
            fatDirEntry = createLongDirEntry(entry, index);
        } else {
            fatDirEntry = createShortDirEntry(entry, index);
        }

        if (free) {
            // Still mark deleted entries as deleted.
            fatDirEntry.setFreeDirEntry(true);
        }

        return fatDirEntry;
    }

    /**
     * Creates a new short directory entry.
     *
     * @param entry the FAT marshal entry.
     * @param index the index of the entry.
     * @return the short directory entry.
     */
    protected FatDirEntry createShortDirEntry(FatMarshal entry, int index) {
        return new FatShortDirEntry(getFatFileSystem(), entry, index);
    }

    /**
     * Creates a new long directory entry.
     *
     * @param entry the FAT entry buffer.
     * @param index the index of the entry.
     * @return the long directory entry.
     */
    protected FatDirEntry createLongDirEntry(FatMarshal entry, int index) throws IOException {
        return new FatLongDirEntry(getFatFileSystem(), entry, index);
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
        FatDirEntry entry;
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

    /**
     * Creates a new entry factory.
     *
     * @param includeDeleted {@code true} if deleted files and directory entries should be returned, {@code false}
     *                       otherwise.
     * @return the entry factory.
     */
    protected FatEntriesFactory createEntriesFactory(boolean includeDeleted) {
        return new FatEntriesFactory(this, includeDeleted);
    }

    public Iterator<FSEntry> iterator() {
        return new FatEntriesIterator(children, createEntriesFactory(false), false);
    }

    /**
     * Creates a new directory entry iterator.
     *
     * @param includeDeleted {@code true} if deleted files and directory entries should be returned, {@code false}
     *                       otherwise.
     * @return the iterator.
     */
    public Iterator<FSEntry> createIterator(boolean includeDeleted) {
        return new FatEntriesIterator(new FatTable(), createEntriesFactory(includeDeleted), includeDeleted);
    }

    /**
     * used from a FatRootDirectory looking for its label
     */
    protected void scanDirectory() {
        FatEntriesFactory f = createEntriesFactory(false);

        while (f.hasNext())
            f.next();
    }

    public synchronized FSEntry getEntry(String name) {
        FatEntry child = children.get(name);

        if (child == null) {
            FatEntriesFactory f = createEntriesFactory(false);

            while (f.hasNext()) {
                FatEntry entry = f.next();
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
            FatEntriesFactory f = createEntriesFactory(true);

            while (f.hasNext()) {
                FatEntry entry = f.next();
                idMap.put(entry.getId(), entry);
            }

            return idMap.get(id);
        }

        return child;
    }

    public FatEntry getEntryByShortName(byte[] shortName) {
        FatEntry child = null;
        FatEntriesFactory f = createEntriesFactory(false);

        while (f.hasNext()) {
            FatEntry entry = f.next();
            if (entry.isShortName(shortName)) {
                child = entry;
                break;
            }
        }

        return child;
    }

    public FatEntry getEntryByName(String name) {
        FatEntry child = null;
        FatEntriesFactory f = createEntriesFactory(false);

        while (f.hasNext()) {
            FatEntry entry = f.next();
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
}
