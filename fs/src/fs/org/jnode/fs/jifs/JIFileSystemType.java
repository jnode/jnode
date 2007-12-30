/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

 package org.jnode.fs.jifs;

import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.FileSystemType;
import org.jnode.partitions.PartitionTableEntry;

/**
 * @author Andreas H\u00e4nel
 */
public class JIFileSystemType implements FileSystemType<JIFileSystem> {

	/** Name of this filesystem type */
	public static final String NAME = "JIFS";
    public static final String VIRTUAL_DEVICE_NAME = "jifs";

    /**	Logger*/
	private static final Logger log = Logger.getLogger(JIFileSystemType.class);

    /**
     * Gets the unique name of this file system type.
     */
    public String getName() {
        return NAME;
    }

	/**
	 * Can this FileSystemType be used on the given first sector of a
	 * BlockDevice? Since this FileSystemType is for a virtual filesystem, it does not support any of the given ones.
	 *
	 * @param pte
	 *          The partition table entry, if any. If null, there is no
	 *          partition table entry.
	 * @param firstSector
	 * 			First sector of a BlockDevice.
	 * @return false
	 */
	public boolean supports(PartitionTableEntry pte, byte[] firstSector, FSBlockDeviceAPI devApi) {
		return false;
	}

	/**
	 * Create a filesystem for a given device.
	 *
	 * @param device
	 * @param readOnly
	 * @return JIFileSystem(device,readOnly)
	 * @throws FileSystemException
	 */
	public JIFileSystem create(Device device, boolean readOnly) throws FileSystemException {
		JIFileSystem ret = new JIFileSystem(device, readOnly);
		if (ret== null){
			log.error("got NULL from the JIFileSystem...");
		}
		return ret;
	}
}
