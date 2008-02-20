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

package org.jnode.fs.fat;

import org.jnode.driver.Device;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.fs.BlockDeviceFileSystemType;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.util.FSUtils;
import org.jnode.partitions.PartitionTableEntry;
import org.jnode.partitions.ibm.IBMPartitionTableEntry;
import org.jnode.partitions.ibm.IBMPartitionTypes;

/**
 * @author epr
 */
public class FatFileSystemType implements BlockDeviceFileSystemType<FatFileSystem> {
    public static final Class<FatFileSystemType> ID = FatFileSystemType.class;

	/**
	 * Gets the unique name of this file system type.
	 */
	public String getName() {
		return "FAT";
	}

	/**
	 * Can this file system type be used on the given first sector of a
	 * blockdevice?
	 *
	 * @param pte
	 *           The partition table entry, if any. If null, there is no
	 *           partition table entry.
	 * @param firstSector
	 */
	public boolean supports(PartitionTableEntry pte, byte[] firstSector, FSBlockDeviceAPI devApi) {
		if (pte != null) {
			if (!pte.isValid()) {
				return false;
			}
			if (!(pte instanceof IBMPartitionTableEntry)) {
				return false;
			}
			final IBMPartitionTableEntry ipte = (IBMPartitionTableEntry)pte;
			final IBMPartitionTypes type = ipte.getSystemIndicator();
			if((type == IBMPartitionTypes.PARTTYPE_DOS_FAT12) ||
			   (type == IBMPartitionTypes.PARTTYPE_DOS_FAT16_LT32M) ||
			   (type == IBMPartitionTypes.PARTTYPE_DOS_FAT16_GT32M) )
			{
				return true;
			}
			else
			{
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
	 *
	 * @param device
	 * @param readOnly
	 */
	public FatFileSystem create(Device device, boolean readOnly) throws FileSystemException {
		return new FatFileSystem(device, readOnly, this);
	}
}
