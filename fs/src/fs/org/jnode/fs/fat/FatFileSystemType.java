/*
 * $Id$
 */
package org.jnode.fs.fat;

import org.jnode.driver.Device;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.FileSystemType;
import org.jnode.fs.partitions.PartitionTableEntry;
import org.jnode.fs.partitions.ibm.IBMPartitionTableEntry;
import org.jnode.fs.partitions.ibm.IBMPartitionTypes;

/**
 * @author epr
 */
public class FatFileSystemType implements FileSystemType {

	/** Name of this filesystem type */
	public static final String NAME = "FAT";

	/**
	 * Public default constructor
	 */
	public FatFileSystemType() {
	}

	/**
	 * Gets the unique name of this file system type.
	 */
	public String getName() {
		return NAME;
	}
	
	/**
	 * Can this file system type be used on the given first sector of a blockdevice?
	 * @param pte The partition table entry, if any. If null, there is no partition table entry. 
	 * @param firstSector
	 */
	public boolean supports(PartitionTableEntry pte, byte[] firstSector) {
		if (pte != null) {
			if (!pte.isValid()) {
				return false;
			}
			if (!(pte instanceof IBMPartitionTableEntry)) {
				return false;
			} 
			final IBMPartitionTableEntry ipte = (IBMPartitionTableEntry)pte;
			final int type = ipte.getSystemIndicator();
			switch (type) {
				case IBMPartitionTypes.PARTTYPE_DOS_FAT12:
				case IBMPartitionTypes.PARTTYPE_DOS_FAT16_LT32M:
				case IBMPartitionTypes.PARTTYPE_DOS_FAT16_GT32M:
					return true;
				default:
					return false;
			}
			
		}
		
      if (!new BootSector(firstSector).isaValidBootSector())
        return false;
      
		// Very ugly, but good enough for now.
		return true;
	}
	
	/**
	 * Create a filesystem for a given device.
	 * @param device
	 */
	public FileSystem create(Device device) 
	throws FileSystemException {
		return new FatFileSystem(device);
	}
}
