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
 
package org.jnode.fs.smbfs;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import org.jnode.fs.FSDirectory;

/**
 * @author Levente S\u00e1ntha
 */
public class SMBFSDirectory extends SMBFSEntry implements FSDirectory {
    private static final long REFRESH_TIMEOUT = 5000;
    private Map<String, SMBFSEntry> entries = new HashMap<String, SMBFSEntry>();

    protected SMBFSDirectory(SMBFSDirectory parent, SmbFile smbFile) {
        super(parent, smbFile);
    }

    /**
     * @see org.jnode.fs.FSDirectory#addDirectory(String)
     */
    public SMBFSEntry addDirectory(final String name) throws IOException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<SMBFSEntry>() {
                public SMBFSEntry run() throws Exception {
                    SmbFile dir = new SmbFile(smbFile, dirName(name));
                    dir.mkdir();
                    SMBFSDirectory sdir = new SMBFSDirectory(SMBFSDirectory.this, dir);
                    entries.put(name, sdir);
                    return sdir;
                }
            });
        } catch (PrivilegedActionException pae) {
            Exception e = pae.getException();
            if (e instanceof IOException) {
                throw (IOException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * @see org.jnode.fs.FSDirectory#addFile(String)
     */
    public SMBFSEntry addFile(final String name) throws IOException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<SMBFSEntry>() {
                public SMBFSEntry run() throws Exception {
                    SmbFile file = new SmbFile(smbFile, name);
                    file.createNewFile();
                    SMBFSFile sfile = new SMBFSFile(SMBFSDirectory.this, file);
                    entries.put(name, sfile);
                    return sfile;
                }
            });
        } catch (PrivilegedActionException pae) {
            Exception e = pae.getException();
            if (e instanceof IOException) {
                throw (IOException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * @see org.jnode.fs.FSDirectory#flush()
     */
    public void flush() throws IOException {
        //nothing to do here
    }

    /**
     * @see org.jnode.fs.FSDirectory#getEntry(String)
     */
    public SMBFSEntry getEntry(String name) throws IOException {
        refreshEntries();
        return entries.get(name);
    }

    /**
     * @see org.jnode.fs.FSDirectory#iterator()
     */
    public Iterator<? extends SMBFSEntry> iterator() throws IOException {
        refreshEntries();
        return entries.values().iterator();
    }

    /**
     * @see org.jnode.fs.FSDirectory#remove(String)
     */
    public void remove(String name) throws IOException {
        SMBFSEntry ent = entries.get(name);
        String fname = ent.isDirectory() ? dirName(name) : name;
        SmbFile file = new SmbFile(smbFile, fname);
        file.delete();
        entries.remove(name);
    }

    private long lastRefresh;

    private void refreshEntries() throws SmbException {
        if (System.currentTimeMillis() - lastRefresh < REFRESH_TIMEOUT)
            return;

        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                public Object run() throws Exception {
                    SmbFile[] smb_list;
                    try {
                        smb_list = smbFile.listFiles();
                    } catch (SmbException e) {
                        e.printStackTrace();
                        throw e;
                    }
                    entries.clear();

                    for (SmbFile f : smb_list) {
                        if (f.isDirectory()) {
                            String name = getSimpleName(f);
                            entries.put(name, new SMBFSDirectory(SMBFSDirectory.this, f));
                        } else if (f.isFile()) {
                            String name = getSimpleName(f);
                            entries.put(name, new SMBFSFile(SMBFSDirectory.this, f));
                        }
                    }
                    lastRefresh = System.currentTimeMillis();
                    return null;
                }
            });
        } catch (PrivilegedActionException pae) {
            Exception e = pae.getException();
            if (e instanceof SmbException) {
                throw (SmbException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    private String dirName(String name) {
        String dname = name;
        if (!dname.endsWith("/"))
            dname += "/";
        return dname;
    }
}
