/*
 * $Id$
 */
package org.jnode.driver.block;

import java.io.IOException;

import org.jnode.fs.partitions.PartitionTableEntry;

/**
 * Alignment support class implementing the FSBlockDeviceAPI.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class FSBlockAlignmentSupport
	extends BlockAlignmentSupport
	implements FSBlockDeviceAPI {

	private final FSBlockDeviceAPI parentApi;

	/**
	 * @param parentApi
	 * @param alignment
	 */
	public FSBlockAlignmentSupport(FSBlockDeviceAPI parentApi, int alignment) {
		super(parentApi, alignment);
		this.parentApi = parentApi;
	}

	/**
	 * @see org.jnode.driver.block.FSBlockDeviceAPI#getPartitionTableEntry()
	 * @return The partition table entry
	 */
	public PartitionTableEntry getPartitionTableEntry() {
		return parentApi.getPartitionTableEntry();
	}

	/**
	 * @see org.jnode.driver.block.FSBlockDeviceAPI#getSectorSize()
	 * @return int
	 */
	public int getSectorSize() throws IOException {
		return parentApi.getSectorSize();
	}
}