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
 
package org.jnode.fs.hfs;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.driver.block.MappedBlockDeviceSupport;
import org.jnode.fs.BlockDeviceFileSystemType;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.hfsplus.HfsPlusFileSystem;
import org.jnode.partitions.PartitionTableEntry;

/**
 * A HFS wrapper around HFS+.
 *
 * @author Luke Quinane
 */
public class HfsWrapperFileSystemType implements BlockDeviceFileSystemType<HfsPlusFileSystem> {
    public static final Class<HfsWrapperFileSystemType> ID = HfsWrapperFileSystemType.class;

    @Override
    public final HfsPlusFileSystem create(final Device device, final boolean readOnly) throws FileSystemException {

        ByteBuffer mdbData = ByteBuffer.allocate(MasterDirectoryBlock.LENGTH);
        long deviceLength;

        try {
            BlockDeviceAPI blockDevice = device.getAPI(BlockDeviceAPI.class);
            blockDevice.read(0x400, mdbData);
            deviceLength = blockDevice.getLength();
        } catch (ApiNotFoundException e) {
            throw new FileSystemException("Failed to find the block device API", e);
        } catch (IOException e) {
            throw new FileSystemException("Error reading HFS wrapper MDB", e);
        }

        MasterDirectoryBlock mdb = new MasterDirectoryBlock(mdbData.array());

        // Calculate the offset and length of the embedded HFS+ file system
        // Limit the length of the embedded file system to the size of the device; some wrappers seem to report the
        // wrong length otherwise.
        long offset = mdb.getAllocationBlockStart() * 512 +
            mdb.getEmbeddedVolumeStartBlock() * mdb.getAllocationBlockSize();
        long length = Math.min(deviceLength - offset, mdb.getEmbeddedVolumeBlockCount() * mdb.getAllocationBlockSize());

        MappedBlockDeviceSupport subDevice;
        try {
            // Take a sub-section of the device to pass down to the HFS+ code
            subDevice = new MappedBlockDeviceSupport(device, offset, length);
        } catch (IOException e) {
            throw new FileSystemException("Error creating sub-device for HFS+", e);
        }

        HfsPlusFileSystem fs = new HfsPlusFileSystem(subDevice, readOnly, this);
        fs.read();
        return fs;
    }

    @Override
    public final String getName() {
        return "HFS Wrapper";
    }

    @Override
    public final boolean supports(final PartitionTableEntry pte, final byte[] firstSectors,
                                  final FSBlockDeviceAPI devApi) {

        if (firstSectors.length < 0x400) {
            // Not enough data for detection
            return false;
        }

        byte[] mdbData = new byte[MasterDirectoryBlock.LENGTH];
        System.arraycopy(firstSectors, 0x400, mdbData, 0, mdbData.length);
        MasterDirectoryBlock mdb = new MasterDirectoryBlock(mdbData);

        return
            mdb.getSignature() == MasterDirectoryBlock.HFS_MDB_SIGNATURE &&
                mdb.getEmbeddedSignature() == MasterDirectoryBlock.HFSPLUS_EMBEDDED_SIGNATURE;
    }
}

