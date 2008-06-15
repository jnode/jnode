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

package org.jnode.fs.service.def;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemType;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class VirtualFS implements FileSystem<VirtualDirEntry> {
    static final Logger log = Logger.getLogger(VirtualFS.class);
    private final Device dev;
    private final VirtualDirEntry root;

    public final FileSystemType<FileSystem<VirtualDirEntry>> getType() {
        throw new UnsupportedOperationException("should not be called");
    }

    /**
     * Initialize this instance.
     * 
     * @throws IOException
     */
    VirtualFS(Device dev) {
        this.dev = dev;
        try {
            this.root = new VirtualDirEntry(this, "/", null);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * @see org.jnode.fs.FileSystem#close()
     */
    public void close() throws IOException {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.jnode.fs.FileSystem#getDevice()
     */
    public Device getDevice() {
        return dev;
    }

    /**
     * @see org.jnode.fs.FileSystem#getRootEntry()
     */
    public VirtualDirEntry getRootEntry() {
        return root;
    }

    /**
     * @see org.jnode.fs.FileSystem#isReadOnly()
     */
    public boolean isReadOnly() {
        return false;
    }

    /**
     * @see org.jnode.fs.FileSystem#isClosed()
     */
    public boolean isClosed() {
        return false;
    }

    /**
     * The filesystem on the given device will be removed.
     * 
     * @param dev
     */
    final void unregisterFileSystem(Device dev) {
        root.unregisterFileSystem(dev);
    }

    public long getFreeSpace() {
        // TODO implement me
        return -1;
    }

    public long getTotalSpace() {
        // TODO implement me
        return -1;
    }

    public long getUsableSpace() {
        // TODO implement me
        return -1;
    }
}
