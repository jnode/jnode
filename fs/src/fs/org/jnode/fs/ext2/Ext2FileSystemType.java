package org.jnode.fs.ext2;

import org.apache.log4j.Logger;

import org.jnode.driver.Device;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.FileSystemType;
import org.jnode.fs.partitions.PartitionTableEntry;
import org.jnode.fs.partitions.ibm.IBMPartitionTableEntry;
import org.jnode.fs.partitions.ibm.IBMPartitionTypes;

/**
 * @author Andras Nagy
 */
public class Ext2FileSystemType implements FileSystemType {

	public static final String NAME = "EXT2";

	/**
	 * @see org.jnode.fs.FileSystemType#create(Device)
	 */
	public FileSystem create(Device device) throws FileSystemException {
		return new Ext2FileSystem(device);
	}

	/**
	 * @see org.jnode.fs.FileSystemType#getName()
	 */
	public String getName() {
		return NAME;
	}

	/**
	 * @see org.jnode.fs.FileSystemType#supports(PartitionTableEntry, byte[])
	 */
	public boolean supports(PartitionTableEntry pte, byte[] firstSector) {				
		if(pte instanceof IBMPartitionTableEntry) {
			if(((IBMPartitionTableEntry)pte).getSystemIndicator()==IBMPartitionTypes.PARTTYPE_LINUXNATIVE)
				return true;
			else
				return false;
		}
		else
			return false;
	}
}
