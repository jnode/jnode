/*
 * $Id$
 */
package org.jnode.driver.block;

import java.io.IOException;

import org.jnode.fs.partitions.PartitionTableEntry;

/**
 * An FSBlockDeviceAPI is an API for blockdevices that (may) contain a filesystem.
 * @author epr
 */
public interface FSBlockDeviceAPI extends BlockDeviceAPI {
	
	/**
	 * Gets the sector size for this device.
 	 * @return The sector size in bytes
	 */
	public int getSectorSize() throws IOException;
	
	/**
	 * Gets the partition table entry specifying this device.
	 * @return A PartitionTableEntry or null if no partition table entry exists.
	 */
	public PartitionTableEntry getPartitionTableEntry();

}
