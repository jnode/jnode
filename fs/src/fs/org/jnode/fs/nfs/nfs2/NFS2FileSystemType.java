/*
 * $Id: NFSFileSystemType.java 2452 2006-04-09 16:44:50Z fduminy $
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

package org.jnode.fs.nfs.nfs2;

import org.jnode.driver.Device;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.FileSystemType;
import org.jnode.partitions.PartitionTableEntry;

/**
 * @author Andrei Dore
 */
public class NFS2FileSystemType implements FileSystemType<NFS2FileSystem> {
    public static final String NAME = "NFS2";

    /**
     * Create a filesystem from a given device.
     * 
     * @param device
     * @param readOnly
     */
    public NFS2FileSystem create(Device device, boolean readOnly)
            throws FileSystemException {
        return new NFS2FileSystem((NFS2Device) device, readOnly);
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
     * @param pte
     *                The partition table entry, if any. If null, there is no
     *                partition table entry.
     * @param firstSector
     */
    public boolean supports(PartitionTableEntry pte, byte[] firstSector,
            FSBlockDeviceAPI devApi) {
        return false;
    }

    /**
     * Format a filesystem for a given device according to its Partition table
     * entry.
     * 
     * @param device
     *                The device on which you want to format with this
     *                FileSystemType
     * @param specificOptions
     *                the specific options for this filesystemType
     * @return the newly created FileSystem
     * @throws org.jnode.fs.FileSystemException
     * 
     */
    public NFS2FileSystem format(Device device, Object specificOptions)
            throws FileSystemException {
        return null;
    }
}