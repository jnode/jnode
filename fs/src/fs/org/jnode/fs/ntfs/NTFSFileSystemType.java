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

package org.jnode.fs.ntfs;

import org.jnode.driver.Device;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.FileSystemType;
import org.jnode.fs.BlockDeviceFileSystemType;
import org.jnode.partitions.PartitionTableEntry;
import org.jnode.partitions.ibm.IBMPartitionTableEntry;
import org.jnode.partitions.ibm.IBMPartitionTypes;

/**
 * @author Chira
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class NTFSFileSystemType implements BlockDeviceFileSystemType<NTFSFileSystem> {

    public static final Class<NTFSFileSystemType> NAME = NTFSFileSystemType.class;

    public static final String TAG = "NTFS";

    public String getName() {
        return "NTFS";
    }

    /**
     * @see org.jnode.fs.FileSystemType#supports(PartitionTableEntry, byte[],
     *      FSBlockDeviceAPI)
     */
    public boolean supports(PartitionTableEntry pte, byte[] firstSector,
            FSBlockDeviceAPI devApi) {
        if (pte instanceof IBMPartitionTableEntry) {
            IBMPartitionTableEntry iPte = (IBMPartitionTableEntry) pte;
            if (iPte.getSystemIndicator() == IBMPartitionTypes.PARTTYPE_NTFS)
            {
            	return new String(firstSector, 0x03, 8).startsWith(TAG);
            }
        }
        return false;
    }

    /**
     * @see org.jnode.fs.FileSystemType#create(Device, boolean)
     */
    public NTFSFileSystem create(Device device, boolean readOnly)
            throws FileSystemException {
        return new NTFSFileSystem(device, readOnly);
    }
}
