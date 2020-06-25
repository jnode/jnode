/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
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
 
package org.jnode.fs.hfsplus;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.jnode.driver.Device;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.fs.BlockDeviceFileSystemType;
import org.jnode.fs.FileSystemException;
import org.jnode.partitions.PartitionTableEntry;
import org.jnode.util.BigEndian;

public class HfsPlusFileSystemType implements BlockDeviceFileSystemType<HfsPlusFileSystem> {
    public static final Class<HfsPlusFileSystemType> ID = HfsPlusFileSystemType.class;

    public HfsPlusFileSystem create(final Device device, final boolean readOnly) throws FileSystemException {
        HfsPlusFileSystem fs = new HfsPlusFileSystem(device, readOnly, this);
        fs.read();
        return fs;
    }

    public String getName() {
        return "HFS+";
    }

    public boolean supports(final PartitionTableEntry pte, final byte[] firstSector, final FSBlockDeviceAPI devApi) {
        /*
         * if (pte != null) { if (pte instanceof IBMPartitionTableEntry) { if (((IBMPartitionTableEntry)
         * pte).getSystemIndicator() != IBMPartitionTypes.PARTTYPE_LINUXNATIVE) { return false; } } }
         */
        // need to check the magic
        ByteBuffer magic = ByteBuffer.allocate(4);
        try {
            devApi.read(1024, magic);
        } catch (IOException e) {
            return false;
        }

        int magicNumber = BigEndian.getInt16(magic.array(), 0);
        int version = BigEndian.getInt16(magic.array(), 2);

        return (magicNumber == SuperBlock.HFSPLUS_SUPER_MAGIC && version == 4)
            || (magicNumber == SuperBlock.HFSX_SUPER_MAGIC && version == 5);
    }
}
