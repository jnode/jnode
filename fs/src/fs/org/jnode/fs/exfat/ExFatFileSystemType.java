package org.jnode.fs.exfat;

import org.jnode.driver.Device;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.fs.BlockDeviceFileSystemType;
import org.jnode.fs.FileSystemException;
import org.jnode.partitions.PartitionTableEntry;

/**
 * @author Luke Quinane
 */
public class ExFatFileSystemType implements BlockDeviceFileSystemType<ExFatFileSystem> {
    public static final Class<ExFatFileSystemType> ID = ExFatFileSystemType.class;

    @Override
    public ExFatFileSystem create(Device device, boolean readOnly) throws FileSystemException {
        return new ExFatFileSystem(device, readOnly, this);
    }

    @Override
    public String getName() {
        return "ExFAT";
    }

    @Override
    public boolean supports(PartitionTableEntry pte, byte[] firstSector, FSBlockDeviceAPI devApi) {

        // Check for ExFAT
        if (firstSector[11] == 0x0 &&
            firstSector[3] == 'E' &&
            firstSector[4] == 'X' &&
            firstSector[5] == 'F' &&
            firstSector[6] == 'A' &&
            firstSector[7] == 'T') {
            return true;
        }

        return false;
    }
}
