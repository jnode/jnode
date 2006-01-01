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
 
package org.jnode.fs.ftpfs;

import org.jnode.fs.FileSystemType;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;
import org.jnode.driver.Device;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.partitions.PartitionTableEntry;

/**
 * @author Levente S\u00e1ntha
 */
public class FTPFileSystemType implements FileSystemType {
    public static final String NAME = "FTPFS";
    /**
     * Create a filesystem from a given device.
     *
     * @param device
     * @param readOnly
     */
    public FTPFileSystem create(Device device, boolean readOnly) throws FileSystemException {
        return new FTPFileSystem((FTPFSDevice) device);  
    }

    /**
     * Gets the unique name of this file system type.
     */
    public String getName() {
        return NAME;
    }

    /**
     * Can this file system type be used on the given first sector of a
     * blockdevice?
     *
     * @param pte         The partition table entry, if any. If null, there is no
     *                    partition table entry.
     * @param firstSector
     */
    public boolean supports(PartitionTableEntry pte, byte[] firstSector, FSBlockDeviceAPI devApi) {
        return false;
    }

    /**
     * Format a filesystem for a given device according to its Partition table entry.
     *
     * @param device          The device on which you want to format with this FileSystemType
     * @param specificOptions the specific options for this filesystemType
     * @return the newly created FileSystem
     * @throws org.jnode.fs.FileSystemException
     *
     */
    public FileSystem format(Device device, Object specificOptions) throws FileSystemException {
        return null;
    }
}
