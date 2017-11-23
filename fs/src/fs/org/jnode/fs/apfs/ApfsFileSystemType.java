package org.jnode.fs.apfs;

import org.jnode.driver.Device;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.fs.BlockDeviceFileSystemType;
import org.jnode.fs.FileSystemException;
import org.jnode.partitions.PartitionTableEntry;

/**
 * @author Luke Quinane
 */
public class ApfsFileSystemType implements BlockDeviceFileSystemType<ApfsFileSystem> {
    public static final Class<ApfsFileSystemType> ID = ApfsFileSystemType.class;

    @Override
    public ApfsFileSystem create(Device device, boolean readOnly) throws FileSystemException {
        return new ApfsFileSystem(device, readOnly, this);
    }

    @Override
    public String getName() {
        return "APFS";
    }

    @Override
    public boolean supports(PartitionTableEntry pte, byte[] firstSector, FSBlockDeviceAPI devApi) {



        return false;
    }
}
