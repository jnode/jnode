package org.jnode.fs.ramfs;

import org.jnode.driver.Device;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.FileSystemType;
import org.jnode.partitions.PartitionTableEntry;

/**
 * FileSystemType for RAMFS, a filesystem implementation in system RAM
 * 
 * @author peda
 */
public class RAMFileSystemType implements FileSystemType<RAMFileSystem> {

	private static final int DEFAULT_SIZE = 104857600;
	
	/** the name of this filesystem */
	public static final String NAME = "RAMFS";

	/** Virtual Device name for this filesystem */ 
	public static final String VIRTUAL_DEVICE_NAME = "ramfsdevice";

    
	/**
	 * (non-Javadoc)
	 * @see org.jnode.fs.FileSystemType#getName()
	 */
	public String getName() {
		return NAME;
	}

	/**
	 * (non-Javadoc)
	 * @see org.jnode.fs.FileSystemType#supports(org.jnode.partitions.PartitionTableEntry, byte[], org.jnode.driver.block.FSBlockDeviceAPI)
	 */
	public boolean supports(PartitionTableEntry pte, byte[] firstSector,
			FSBlockDeviceAPI devApi) {
		return false;
	}

	/**
	 * (non-Javadoc)
	 * @see org.jnode.fs.FileSystemType#create(org.jnode.driver.Device, boolean)
	 */
	public RAMFileSystem create(Device device, boolean readOnly)
			throws FileSystemException {
		
		return new RAMFileSystem(device, readOnly, DEFAULT_SIZE);
	}
}
