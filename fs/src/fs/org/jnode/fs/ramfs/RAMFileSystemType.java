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
 
package org.jnode.fs.ramfs;

import org.jnode.driver.Device;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.FileSystemType;

/**
 * FileSystemType for RAMFS, a filesystem implementation in system RAM
 * 
 * @author peda
 */
public class RAMFileSystemType implements FileSystemType<RAMFileSystem> {
    public static final Class<RAMFileSystemType> ID = RAMFileSystemType.class;
    private static final int DEFAULT_SIZE = 104857600;

    /** Virtual Device name for this filesystem */
    public static final String VIRTUAL_DEVICE_NAME = "ramfsdevice";

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FileSystemType#getName()
     */
    public String getName() {
        return "RAMFS";
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.jnode.fs.FileSystemType#create(org.jnode.driver.Device, boolean)
     */
    public RAMFileSystem create(Device device, boolean readOnly) throws FileSystemException {
        return new RAMFileSystem(device, readOnly, DEFAULT_SIZE, this);
    }
}
