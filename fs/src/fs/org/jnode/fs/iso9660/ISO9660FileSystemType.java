/*
 * $Id$
 */
package org.jnode.fs.iso9660;

import java.io.IOException;

import org.jnode.driver.Device;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.FileSystemType;
import org.jnode.fs.partitions.PartitionTableEntry;

/**
 * @author Chira
 */
public class ISO9660FileSystemType implements FileSystemType {

    public static final String NAME = "ISO9660";

    public final String getName() {
        return NAME;
    }

    /**
     * @see org.jnode.fs.FileSystemType#supports(PartitionTableEntry, byte[],
     *      FSBlockDeviceAPI)
     */
    public boolean supports(PartitionTableEntry pte, byte[] firstSector,
            FSBlockDeviceAPI devApi) {
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
                final byte[] data = new byte[ blockSize];
                devApi.read(offset, data, 0, data.length);

                final String id = new String(data, 1, 5, "US-ASCII");
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
    public FileSystem create(Device device, boolean readOnly) throws FileSystemException {
        return new ISO9660FileSystem(device, readOnly);  // not readOnly !
    }

    /**
     * @see org.jnode.fs.FileSystemType#format(org.jnode.driver.Device,
     *      java.lang.Object)
     */
    public FileSystem format(Device device, Object specificOptions)
            throws FileSystemException {
        throw new FileSystemException("Not yet implemented");
    }
}