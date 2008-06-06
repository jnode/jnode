/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
        SmbFile[] smb_list;
        try{
            smb_list = smbFile.listFiles();
        } catch(SmbException e){
            e.printStackTrace();
            throw e;
        }
        entries.clear();
        
        for(SmbFile f : smb_list){
            if(f.isDirectory()){
                String name = getSimpleName(f);
                entries.put(name, new SMBFSDirectory(this, f));
            } else if(f.isFile()){
                String name = getSimpleName(f);
                entries.put(name, new SMBFSFile(this, f));
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
