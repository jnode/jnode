/*
 * $Id$
 */
package org.jnode.fs.iso9660;

import org.jnode.driver.*;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.fs.*;
import org.jnode.fs.partitions.*;

/**
 * @author Chira
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class ISO9660FileSystemType implements FileSystemType {

	public static final String NAME = "CDFS";
	public static final String TAG = "CDFS";

	public String getName() {
		return NAME;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jnode.fs.FileSystemType#supports(org.jnode.fs.partitions.PartitionTableEntry,
	 *      byte[])
	 */
	public boolean supports(PartitionTableEntry pte, byte[] firstSector, FSBlockDeviceAPI devApi) {
		return true;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jnode.fs.FileSystemType#create(org.jnode.driver.Device)
	 */
	public FileSystem create(Device device) throws FileSystemException {
		return new ISO9660FileSystem(device);
	}

	/**
	 * @see org.jnode.fs.FileSystemType#format(org.jnode.driver.Device,
	 *      java.lang.Object)
	 */
	public FileSystem format(Device device, Object specificOptions) throws FileSystemException {
		throw new FileSystemException("Not yet implemented");
	}
}
