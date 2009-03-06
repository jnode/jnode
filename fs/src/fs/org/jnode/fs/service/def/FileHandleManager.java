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
 
package org.jnode.fs.service.def;

import java.io.IOException;
import java.io.VMOpenMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jnode.fs.FSFile;

/**
 * @author epr
 */
final class FileHandleManager {

    /** My logger */
    private static final Logger log = Logger.getLogger(FileHandleManager.class);
    /** A map between File and FileData */
    public final Map<FSFile, FileData> openFiles = new HashMap<FSFile, FileData>();

    /**
     * Create a filehandle for a given file entry.
     * 
     * @param file
     * @param mode
     * @throws IOException
     */
    public synchronized FileHandleImpl open(FSFile file, VMOpenMode mode) throws IOException {
        FileData fd = openFiles.get(file);
        if (fd == null) {
            fd = new FileData(file);
            openFiles.put(file, fd);
        }

        return fd.open(mode);
    }

    /**
     * Close a filehandle.
     * 
     * @param handle file handle to close.
     * 
     * @throws IOException if file is not already open.
     */
    public synchronized void close(FileHandleImpl handle) throws IOException {
        final FSFile file = handle.getFile();
        final FileData fd = openFiles.get(file);
        if (fd != null) {
            fd.close(handle);
            if (!fd.hasHandles()) {
                openFiles.remove(file);
            }
        } else {
            log.error("FileHandle tried to close an unknown file!!");
        }
    }

    /**
     * Duplicate a filehandle.
     * 
     * @param handle file handle to duplicate.
     * @param newMode new file open mode.
     * 
     * @throws IOException if file is not already open.
     */
    public synchronized FileHandleImpl dup(FileHandleImpl handle, VMOpenMode newMode)
        throws IOException {
        final FSFile file = handle.getFile();
        final FileData fd = openFiles.get(file);
        if (fd != null) {
            return fd.dup(handle, newMode);
        } else {
            throw new IOException("FileHandle tried to dup an unknown file!!");
        }
    }

    class FileData {

        /** My logger */
        private final Logger fdLog = Logger.getLogger(getClass());
        /** The actual file */
        private final FSFile file;
        /** Set of open filehandles */
        private final HashSet<FileHandleImpl> handles = new HashSet<FileHandleImpl>();
        /** Is any of the handles opened for write? */
        private boolean hasWriters;

        public FileData(FSFile file) {
            this.file = file;
        }

        /**
         * Open an extra handle for this file.
         * 
         * @param mode file open mode.
         * 
         * @throws IOException if file is already open in write mode.
         */
        public FileHandleImpl open(VMOpenMode mode) throws IOException {
            if (mode.canWrite()) {
                if (hasWriters) {
                    throw new IOException("File is already open for writing");
                } else {
                    hasWriters = true;
                }
            }
            final FileHandleImpl handle = new FileHandleImpl(file, mode, FileHandleManager.this);
            handles.add(handle);
            return handle;
        }

        /**
         * Duplicate the given handle for this file.
         * 
         * @param handle file handle.
         * @param newMode new open mode.
         * 
         * @return duplicate file handle.
         * 
         * @throws IOException if handle doesn't exists or file is already open in write mode.
         */
        public FileHandleImpl dup(FileHandleImpl handle, VMOpenMode newMode) throws IOException {
            if (handles.contains(handle)) {
                if (newMode.canWrite()) {
                    if (hasWriters) {
                        throw new IOException("File is already open for writing");
                    } else {
                        hasWriters = true;
                    }
                }
                final FileHandleImpl newHandle;
                newHandle = new FileHandleImpl(file, newMode, FileHandleManager.this);
                handles.add(newHandle);
                return newHandle;
            } else {
                throw new IOException("FileHandle is not known in FileData.dup!!");
            }
        }

        /**
         * Close the given handle for this file.
         * 
         * @param handle file handle.
         * 
         * @throws IOException if handle doesn't exists.
         */
        public void close(FileHandleImpl handle) throws IOException {
            if (handles.contains(handle)) {
                handles.remove(handle);
                if (handle.getMode().canWrite()) {
                    hasWriters = false;
                }
            } else {
            	throw new IOException("FileHandle is not known in FileData.close!!");
            }
        }

        /**
         * Are there open handles for this file?
         * 
         * @return <tt>true</tt> if there are open handles.
         */
        public boolean hasHandles() {
            return !handles.isEmpty();
        }
    }
}
