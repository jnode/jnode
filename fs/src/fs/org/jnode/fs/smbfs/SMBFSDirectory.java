package org.jnode.fs.smbfs;

import org.jnode.fs.FSDirectory;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbException;

/**
 * @author Levente S\u00e1ntha
 */
public class SMBFSDirectory extends SMBFSEntry implements FSDirectory {
    private Map<String, SMBFSEntry> entries = new HashMap<String, SMBFSEntry>();

    protected SMBFSDirectory(SMBFSDirectory parent, SmbFile smbFile) {
        super(parent, smbFile);
    }

    /**
     * Add a new (sub-)directory with a given name to this directory.
     *
     * @param name
     * @throws java.io.IOException
     */
    public SMBFSEntry addDirectory(String name) throws IOException {
        return null;
    }

    /**
     * Add a new file with a given name to this directory.
     *
     * @param name
     * @throws java.io.IOException
     */
    public SMBFSEntry addFile(String name) throws IOException {
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
     * Gets the entry with the given name.
     *
     * @param name
     * @throws java.io.IOException
     */
    public SMBFSEntry getEntry(String name) throws IOException {
        return entries.get(name);
    }

    /**
     * Gets an iterator used to iterate over all the entries of this
     * directory.
     * All elements returned by the iterator must be instanceof FSEntry.
     */
    public Iterator<? extends SMBFSEntry> iterator() throws IOException {
        SmbFile[] smb_list = null;
        try{
            smb_list = smbFile.listFiles();
        } catch(SmbException e){
            e.printStackTrace();
            throw e;
        }
        entries.clear();
        for(SmbFile f : smb_list){
            if(f.isDirectory()){
                entries.put(f.getName(), new SMBFSDirectory(this, f));
            } else if(f.isFile()){
                entries.put(f.getName(), new SMBFSFile(this, f));
            }
        }
        return entries.values().iterator();
    }

    /**
     * Remove the entry with the given name from this directory.
     *
     * @param name
     * @throws java.io.IOException
     */
    public void remove(String name) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
