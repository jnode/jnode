/*
 * $Id$
 */
package org.jnode.fs;

import org.jnode.driver.Device;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.fs.partitions.PartitionTableEntry;

/**
 * Descriptor and entry point for a class of filesystems. Samples of
 * FileSystemType's are FAT16, EXT3.
 * 
 * @author epr
 */
public interface FileSystemType {

	/**
	 * Gets the unique name of this file system type.
	 */
	public String getName();

	/**
	 * Can this file system type be used on the given first sector of a
	 * blockdevice?
	 * 
	 * @param pte
	 *           The partition table entry, if any. If null, there is no
	 *           partition table entry.
	 * @param firstSector
	 */
	public boolean supports(PartitionTableEntry pte, byte[] firstSector, FSBlockDeviceAPI devApi);

	/**
	 * Create a filesystem from a given device.
	 * 
	 * @param device
	 */
	public FileSystem create(Device device) throws FileSystemException;

    /**
     * Format a filesystem for a given device according to its Partition table entry.
     * 
     * @param device The device on which you want to format with this FileSystemType
     * @param specificOptions the specific options for this filesystemType
     * @return the newly created FileSystem
     * @throws FileSystemException
     */
   public FileSystem format(Device device, Object specificOptions) throws FileSystemException;
    

}
