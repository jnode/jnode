/*
 * $Id$
 */
package org.jnode.fs.ntfs;

import org.jnode.driver.Device;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.FileSystemType;
import org.jnode.fs.partitions.PartitionTableEntry;
import org.jnode.fs.partitions.ibm.IBMPartitionTableEntry;
import org.jnode.fs.partitions.ibm.IBMPartitionTypes;

/**
 * @author Chira
 */
public class NTFSFileSystemType implements FileSystemType {

	public static final String NAME = "NTFS";
	public static final String TAG = "NTFS";

	public String getName() {
		return NAME;
	}

	/**
	 * @see org.jnode.fs.FileSystemType#supports(PartitionTableEntry, byte[], FSBlockDeviceAPI)
	 */
	public boolean supports(PartitionTableEntry pte, byte[] firstSector, FSBlockDeviceAPI devApi) {
	    if (pte instanceof IBMPartitionTableEntry) {
	        IBMPartitionTableEntry iPte = (IBMPartitionTableEntry)pte;
	        if (iPte.getSystemIndicator() == IBMPartitionTypes.PARTTYPE_NTFS) {
	    		return new String(firstSector, 0x03, 8).startsWith(TAG);	            
	        }
	    }
        return false;
	}
	
	/**
	 * @see org.jnode.fs.FileSystemType#create(Device, boolean)
	 */
	public FileSystem create(Device device, boolean readOnly) throws FileSystemException {
		return new NTFSFileSystem(device, readOnly);
	}

	/**
	 * @see org.jnode.fs.FileSystemType#format(org.jnode.driver.Device,
	 *      java.lang.Object)
	 */
	public FileSystem format(Device device, Object specificOptions) throws FileSystemException {
		throw new FileSystemException("Not yet implemented");
	}
}
