package org.jnode.fs.ftpfs;

import com.enterprisedt.net.ftp.FTPFile;
import org.jnode.fs.FSDirectory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Levente S\u00e1ntha
 */
public class FTPFSDirectory extends FTPFSEntry implements FSDirectory {
    private Map<String, FTPFSEntry> entries;

    public FTPFSDirectory(FTPFileSystem fileSystem, FTPFile ftpFile) {
        super(fileSystem, ftpFile);
    }

    /**
     * Gets the entry with the given name.
     *
     * @param name
     * @throws java.io.IOException
     */
    public FTPFSEntry getEntry(String name) throws IOException {
        ensureEntries();
        return entries.get(name);
    }

    /**
     * Gets an iterator used to iterate over all the entries of this
     * directory.
     * All elements returned by the iterator must be instanceof FSEntry.
     */
    public Iterator<? extends FTPFSEntry> iterator() throws IOException {
        ensureEntries();
        return entries.values().iterator();
    }

    private void ensureEntries() throws IOException {
        try {
            if (entries == null) {
                entries = new HashMap<String, FTPFSEntry>();
                FTPFile[] ftpFiles = null;
                synchronized(fileSystem){
                    ftpFiles = fileSystem.dirDetails(path());
                }
                for (FTPFile f : ftpFiles) {
                    FTPFSEntry e = f.isDir() ? new FTPFSDirectory(fileSystem, f) : new FTPFSFile(fileSystem, f);
                    e.setParent(this);
                    entries.put(f.getName(), e);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Read error");
        }
    }

    String path() throws IOException{
        StringBuilder p = new StringBuilder("/");
        FTPFSDirectory root = fileSystem.getRootEntry();
        FTPFSDirectory d = this;
        while (d != root){
            p.insert(0, d.getName());
            p.insert(0, '/');
            d = d.parent;
        }
        return p.toString();
    }

    /**
     * Add a new (sub-)directory with a given name to this directory.
     *
     * @param name
     * @throws java.io.IOException
     */
    public FTPFSEntry addDirectory(String name) throws IOException {
        return null;
    }

    /**
     * Add a new file with a given name to this directory.
     *
     * @param name
     * @throws java.io.IOException
     */
    public FTPFSEntry addFile(String name) throws IOException {
        return null;
    }

    /**
     * Save all dirty (unsaved) data to the device
     *
     * @throws java.io.IOException
     */
    public void flush() throws IOException {

    }

    /**
     * Remove the entry with the given name from this directory.
     *
     * @param name
     * @throws java.io.IOException
     */
    public void remove(String name) throws IOException {

    }
}
