/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */

package org.jnode.fs.service.def;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.VMFileSystemAPI;
import java.io.VMOpenMode;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FileSystem;
import org.jnode.java.io.VMFileHandle;

/**
 * @modif add mkDir mkFile Yves Galante (yves.galante@jmob.net) 01.04.2004
 * @author epr
 */
final class FileSystemAPIImpl implements VMFileSystemAPI {

    /** My logger */
    private static final Logger log = Logger.getLogger(FileSystemAPIImpl.class);

    /** My filesystem manager */
    final FileSystemManager fsm;

    /** The path to entry cache */
    private final FSEntryCache entryCache;

    /** The open file handle manager */
    private final FileHandleManager fhm;

    /** The virtual filesystem */
    private final VirtualFS vfs;

    /**
     * Create a new instance
     * 
     * @param fsm
     * @throws IOException
     */
    public FileSystemAPIImpl(FileSystemManager fsm, VirtualFS vfs) {
        this.fsm = fsm;
        this.entryCache = new FSEntryCache();
        this.fhm = new FileHandleManager();
        this.vfs = vfs;
    }

    /**
     * Does the given file exist?
     */
    public boolean fileExists(String file) {
        final FSEntry entry = getEntry(file);
        return (entry != null);
    }

    /**
     * Is the given File a plain file?
     */
    public boolean isFile(String file) {
        final FSEntry entry = getEntry(file);
        return (entry != null) && (entry.isFile());
    }

    /**
     * Is the given File a directory?
     */
    public boolean isDirectory(String file) {
        final FSEntry entry = getEntry(file);
        return (entry != null) && (entry.isDirectory());
    }

    /**
     * Can the given file be read?
     * 
     * @param file
     */
    public boolean canRead(String file) {
        // TODO implement me
        return true;
    }

    /**
     * Can the given file be written to?
     * 
     * @param file
     */
    public boolean canWrite(String file) {
        // TODO implement me
        return false;
    }

    /**
     * Gets the length in bytes of the given file or 0 if the file does not
     * exist.
     * 
     * @param file
     */
    public long getLength(String file) {
        final FSEntry entry = getEntry(file);
        if (entry != null) {
            if (entry.isFile()) {
                try {
                    return entry.getFile().getLength();
                } catch (IOException ex) {
                    log.debug("Error in getLength", ex);
                    return 0;
                }
            } else {
                log.debug("Not a file in getLength");
                return 0;
            }
        } else {
            log.debug("File not found in getLength (" + file + ")");
            return 0;
        }

    }

    /**
     * Gets the last modification date of the given file.
     * 
     * @param file
     */
    public long getLastModified(String file) {
        final FSEntry entry = getEntry(file);
        if (entry != null) {
            try {
                return entry.getLastModified();
            } catch (IOException ex) {
                return 0;
            }
        } else {
            return 0;
        }
    }

    /**
     * Sets the last modification date of the given file.
     * 
     * @param file
     */
    public void setLastModified(String file, long time) throws IOException {
        final FSEntry entry = getEntry(file);
        if (entry != null) {
            entry.setLastModified(time);
        } else {
            throw new FileNotFoundException(file);
        }
    }

    /**
     * Mark the given file as readonly.
     * 
     * @param file
     * @throws IOException
     */
    public void setReadOnly(String file) throws IOException {
        throw new IOException("Not implemented yet");
        // TODO implement me
    }

    /**
     * Delete the given file.
     * 
     * @param file
     * @throws IOException
     */
    public void delete(String file) throws IOException {
        final FSDirectory parentDirectory = getParentDirectoryEntry(file);
        if (parentDirectory == null) {
            throw new IOException("Parent of " + file + " not found");
        }

        parentDirectory.remove(getName(file));
        entryCache.removeEntries(file);
    }

    /**
     * This method returns an array of filesystem roots.
     */
    public File[] getRoots() {
        return new File[] { new File("/") };
    }

    /**
     * Gets an array of names of all entries of the given directory. All names
     * are relative to the given directory.
     * 
     * @param directory
     * @param filter
     */
    public String[] list(String directory) throws IOException {
        final FSEntry entry = getEntry(directory);
        if (entry == null) {
            throw new FileNotFoundException(directory);
        }
        if (!entry.isDirectory()) {
            throw new IOException("Cannot list on non-directories " + directory);
        }
        final ArrayList<String> list = new ArrayList<String>();
        for (Iterator<? extends FSEntry> i = entry.getDirectory().iterator(); i.hasNext();) {
            final FSEntry child = i.next();
            final String name = child.getName();
            list.add(name);
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * Gets the FSEntry for the given path, or null if not found.
     * 
     * @param path
     *            must be an absolute canonical path
     */
    private FSEntry getEntry(String path) {
        try {
            if (path == null) {
                return null;
            }

            final int pathLen = path.length();
            if (pathLen > 0) {
                if ((path.charAt(0) == File.separatorChar)
                        || (path.charAt(pathLen - 1) == File.separatorChar)) {
                    throw new IllegalArgumentException("Invalid path: " + path);
                }
            }

            if (path.length() == 0) {
                return vfs.getRootEntry();
            }

            FSEntry entry = entryCache.getEntry(path);
            if (entry != null) {
                return entry;
            }
            final FSDirectory parentEntry = getParentDirectoryEntry(path);
            if (parentEntry != null) {
                try {
                    entry = parentEntry.getEntry(stripParentPath(path));
                    entryCache.setEntry(path, entry);
                    return entry;
                } catch (IOException ex) {
                    // Not found
                    log.debug("parent.getEntry failed", ex);
                    ex.printStackTrace();
                    return null;
                }
            } else {
                return null;
            }
        } catch (IOException e) {
            log.debug("Filesystem.getRootEntry failed", e);
            return null;
        }

    }

    /**
     * Open a given file
     * 
     * @param file
     *            absolute path
     * @throws IOException
     */
    public VMFileHandle open(String file, VMOpenMode mode) throws IOException {
        FSEntry entry = getEntry(file);
        if ((entry != null) && !entry.isFile()) {
            throw new IOException("Not a file " + file);
        }
        if (entry == null) {
            if (mode.canWrite()) {
                // Try to create the file
                FSDirectory parent = getParentDirectoryEntry(file);
                if (parent == null) {
                    throw new IOException("Cannot create " + file
                            + ", parent directory does not exist");
                }

                // Ok, add the file
                entry = parent.addFile(getName(file));
            } else {
                throw new FileNotFoundException(file);
            }
        }
        return fhm.open(entry.getFile(), mode);
        // TODO open need not create the file but throw FileNotFoundException
    }

    /**
     * Make a directory
     * 
     * @param file
     * @throws IOException
     */
    public boolean mkDir(String file) throws IOException {
        FSEntry entry = getEntry(file);
        if (entry != null) {
            log.debug(file + "exists");
            return false;
        }
        FSDirectory directory = getParentDirectoryEntry(file);
        if (directory == null) {
            return false;
        }
        // Ok, add the dir
        entry = directory.addDirectory(getName(file));
        return true;
    }

    /**
     * Make a file
     * 
     * @param file
     * @throws IOException
     */
    public boolean mkFile(String file, VMOpenMode mode) throws IOException {
        FSEntry entry = getEntry(file);
        if ((entry != null) || !mode.canWrite()) {
            return false;
        }
        FSDirectory directory = getParentDirectoryEntry(file);
        if (directory == null)
            return false;
        // Ok, make the file
        entry = directory.addFile(getName(file));
        return true;
    }

    /**
     * Mount the given filesystem at the fullPath, using the fsPath as root of
     * the to be mounted filesystem.
     * 
     * @param fullPath
     * @param fs
     * @param fsPath Null or empty to use the root of the filesystem.
     */
    public void mount(String fullPath, FileSystem fs, String fsPath)
    throws IOException {
        final String dir = getParentPath(fullPath);
        final String name = stripParentPath(fullPath);
        final FSEntry entry = getEntry(dir);
        if (entry == null) {
            throw new FileNotFoundException(dir);
        }
        if (!(entry instanceof VirtualDirEntry)) {
            throw new IOException("Cannot mount at " + dir);
        }
        final VirtualDirEntry vde = (VirtualDirEntry)entry;
        vde.addMount(name, fs, fsPath);
    }
    
    /**
     * Is the given directory a mount.
     * @param fullPath
     * @return
     */
    public boolean isMount(String fullPath) {
        final FSEntry entry = getEntry(fullPath);
        return (entry instanceof VirtualMountEntry);        
    }
    
    /**
     * The filesystem on the given device will be removed.
     * @param dev
     */
    final void unregisterFileSystem(Device dev) {
        vfs.unregisterFileSystem(dev);
    }
    
    /**
     * Get the parent entry of a file
     * 
     * @param file
     *            absolute path
     * @return the directory entry, null if not exite or not a directory
     * @throws IOException
     */
    private FSDirectory getParentDirectoryEntry(String file) throws IOException {
        if (file == null) {
            return null;
        }
        final FSEntry dirEntry = getEntry(getParentPath(file));
        if (dirEntry == null) {
            return null;
        }
        if (!dirEntry.isDirectory()) {
            return null;
        }
        return dirEntry.getDirectory();
    }

    /**
     * @param path
     * @return
     */
    private String getName(String path) {
        if (path == null) {
            return null;
        }

        int idx = path.lastIndexOf(File.separatorChar);
        return (idx >= 0) ? path.substring(idx + 1) : path;
    }

    /**
     * Gets the parent path of the given path
     * 
     * @param path
     * @return
     */
    private String getParentPath(String path) {
        if (path == null) {
            return null;
        } else {
            final int idx = path.lastIndexOf(File.separatorChar);
            return (idx >= 0) ? path.substring(0, idx) : "";
        }
    }

    /**
     * Gets the given path without the parent path
     * 
     * @param path
     * @return
     */
    private String stripParentPath(String path) {
        if (path == null) {
            return null;
        } else {
            final int idx = path.lastIndexOf(File.separatorChar);
            return (idx >= 0) ? path.substring(idx + 1) : path;
        }
    }
}
