/*
 *
 */
package org.jnode.fs.jfat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;

public class FatDirectory extends FatEntry implements FSDirectory {
    private final int MAXENTRIES = 65535; // 2^16-1; fatgen 1.03, page 33

    private static final Logger log = Logger.getLogger(FatDirectory.class);

    private final FatTable children = new FatTable();

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
    private FatDirEntry getFatDirEntry(int index) throws IOException {
        FatMarshal entry = new FatMarshal(FatDirEntry.LENGTH);
        getChain().read(index * entry.length(), entry.getByteBuffer());
        return FatDirEntry.create(getFatFileSystem(), entry, index);
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
                entry = getFatDirEntry(index);
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
        return new EntriesIterator();
    }

    /*
     * used from a FatRootDirectory looking for its label
     */
    protected void scanDirectory() {
        EntriesFactory f = new EntriesFactory();

        while (f.hasNextEntry())
            f.createNextEntry();
    }

    public synchronized FSEntry getEntry(String name) {
        FatEntry child = children.get(name);

        if (child == null) {
            EntriesFactory f = new EntriesFactory();

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

    public FatEntry getEntryByShortName(byte[] shortName) {
        FatEntry child = null;
        EntriesFactory f = new EntriesFactory();

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
        EntriesFactory f = new EntriesFactory();

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
        return children.put(file);
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
        return children.put(dir);
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

    }

    public String toString() {
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

    private class EntriesIterator extends EntriesFactory implements Iterator<FSEntry> {
        public EntriesIterator() {
            super();
        }

        public boolean hasNext() {
            return hasNextEntry();
        }

        public FSEntry next() {
            return children.look(createNextEntry());
        }

        public void remove() {
            throw new UnsupportedOperationException("remove");
        }
    }

    private class EntriesFactory {
        private boolean label;
        private int index;
        private int next;
        private FatEntry entry;

        protected EntriesFactory() {
            label = false;
            index = 0;
            next = 0;
            entry = null;
        }

        protected boolean hasNextEntry() {
            int i;
            FatDirEntry e;
            FatRecord v = new FatRecord();

            if (index > MAXENTRIES)
                log.debug("Full Directory: invalid index " + index);

            for (i = index;;) {
                /*
                 * create a new entry from the chain
                 */
                try {
                    e = getFatDirEntry(i);
                    i++;
                } catch (NoSuchElementException ex) {
                    entry = null;
                    return false;
                } catch (IOException ex) {
                    log.debug("cannot read entry " + i);
                    i++;
                    continue;
                }

                if (e.isFreeDirEntry()) {
                    v.clear();
                } else if (e.isLongDirEntry()) {
                    FatLongDirEntry l = (FatLongDirEntry) e;
                    if (l.isDamaged()) {
                        log.debug("Damaged entry at " + (i - 1));
                        v.clear();
                    } else
                        v.add(l);
                } else if (e.isShortDirEntry()) {
                    FatShortDirEntry s = (FatShortDirEntry) e;
                    if (s.isLabel()) {
                        if (isRoot()) {
                            FatRootDirectory r = (FatRootDirectory) FatDirectory.this;
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
                this.entry = new FatDirectory(getFatFileSystem(), FatDirectory.this, v);
            else
                this.entry = new FatFile(getFatFileSystem(), FatDirectory.this, v);

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
