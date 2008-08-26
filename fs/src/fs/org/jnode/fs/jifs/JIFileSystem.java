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

package org.jnode.fs.jifs;

import org.jnode.driver.Device;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.jifs.directories.JIFSDrootDir;

/**
 * @author Andreas H\u00e4nel
 */
public class JIFileSystem implements FileSystem<JIFSDirectory> {

    private JIFSDirectory rootDir = null;
    private Device device;
    private final JIFileSystemType type;

    /**
     * Constructor for JIFileSystem in specified readOnly mode
     */
    public JIFileSystem(Device device, boolean readOnly, JIFileSystemType type)
        throws FileSystemException {
        if (readOnly == false) {
            throw new FileSystemException("JIFS can not be created as writable...");
        }
        this.device = device;
        this.type = type;
        rootDir = new JIFSDrootDir(device.getId());
    }

    public final JIFileSystemType getType() {
        return type;
    }

    /**
     * Is the filesystem mounted in readonly mode ?
     */
    public boolean isReadOnly() {
        // always readOnly
        return true;
    }

    public void close() {
        //nothing to do
    }

    public boolean isClosed() {
        return false;
    }

    public Device getDevice() {
        return device;
    }

    /**
     * Gets the root entry of this filesystem. This is usually a director, but
     * this is not required.
     * 
     * @return rootDir
     */
    public JIFSDirectory getRootEntry() {
        return rootDir;
    }

    public long getFreeSpace() {
        return -1;
    }

    public long getTotalSpace() {
        return -1;
    }

    public long getUsableSpace() {
        return -1;
    }
}
