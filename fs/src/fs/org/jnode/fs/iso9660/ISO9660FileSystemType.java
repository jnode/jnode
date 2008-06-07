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

package org.jnode.fs.iso9660;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jnode.driver.Device;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.fs.BlockDeviceFileSystemType;
import org.jnode.fs.FileSystemException;
import org.jnode.partitions.PartitionTableEntry;

/**
 * @author Chira
 */
public class ISO9660FileSystemType implements BlockDeviceFileSystemType<ISO9660FileSystem> {
    public static final Class<ISO9660FileSystemType> ID = ISO9660FileSystemType.class;

    public final String getName() {
        return "ISO9660";
    }

    /**
     * @see org.jnode.fs.FileSystemType#supports(PartitionTableEntry, byte[],
     *      FSBlockDeviceAPI)
     */
    public boolean supports(PartitionTableEntry pte, byte[] firstSector, FSBlockDeviceAPI devApi) {
        if (pte != null) {
            // CD-ROM's do not have a partition table.
            return false;
        } else {
            try {
                final int blockSize = devApi.getSectorSize();
                if (blockSize < 2048) {
                    return false;
                }
                final int offset = blockSize * 16;
                final ByteBuffer data = ByteBuffer.allocate(blockSize);
                devApi.read(offset, data);

                final String id = new String(data.array(), 1, 5, "US-ASCII");
                //System.out.println("id=" + id);
                return id.equals("CD001");
            } catch (IOException ex) {
                // Ignore
            }
            return false;
        }
    }

    /**
     * @see org.jnode.fs.FileSystemType#create(Device, boolean)
     */
    public ISO9660FileSystem create(Device device, boolean readOnly) throws FileSystemException {
        return new ISO9660FileSystem(device, readOnly, this);
    }
}
