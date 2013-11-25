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

        try {
            device.getAPI(BlockDeviceAPI.class).read(0x400, mdbData);
        } catch (ApiNotFoundException e) {
            throw new FileSystemException("Failed to find the block device API", e);
        } catch (IOException e) {
            throw new FileSystemException("Error reading HFS wrapper MDB", e);
        }

        MasterDirectoryBlock mdb = new MasterDirectoryBlock(mdbData.array());

        // Calculate the offset and length of the embedded HFS+ file system
        long offset = mdb.getAllocationBlockStart() * 512 +
            mdb.getEmbeddedVolumeStartBlock() * mdb.getAllocationBlockSize();
        long length = mdb.getEmbeddedVolumeBlockCount() * mdb.getAllocationBlockSize();

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
    public final boolean supports(final PartitionTableEntry pte, final byte[] firstSector,
                                  final FSBlockDeviceAPI devApi) {
        byte[] mdbData = new byte[MasterDirectoryBlock.LENGTH];
        System.arraycopy(firstSector, 0x400, mdbData, 0, mdbData.length);
        MasterDirectoryBlock mdb = new MasterDirectoryBlock(mdbData);

        return
            mdb.getSignature() == MasterDirectoryBlock.HFS_MDB_SIGNATURE &&
                mdb.getEmbeddedSignature() == MasterDirectoryBlock.HFSPLUS_EMBEDDED_SIGNATURE;
    }
}

