/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
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
