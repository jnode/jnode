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
 
package org.jnode.fs.ext2;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jnode.driver.Device;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.FileSystemType;
import org.jnode.partitions.PartitionTableEntry;
import org.jnode.partitions.ibm.IBMPartitionTableEntry;
import org.jnode.partitions.ibm.IBMPartitionTypes;

/**
 * @author Andras Nagy
 */
public class Ext2FileSystemType implements FileSystemType<Ext2FileSystem> {

	public static final String NAME = "EXT2";

	/**
	 * @see org.jnode.fs.FileSystemType#create(Device, boolean)
	 */
	public Ext2FileSystem create(Device device, boolean readOnly) throws FileSystemException {
		Ext2FileSystem fs = new Ext2FileSystem(device, readOnly);
		fs.read();
		return fs;
	}

	/**
	 * @see org.jnode.fs.FileSystemType#getName()
	 */
	public String getName() {
		return NAME;
	}

	/**
	 * @see org.jnode.fs.FileSystemType#supports(PartitionTableEntry, byte[], FSBlockDeviceAPI)
	 */
	public boolean supports(PartitionTableEntry pte, byte[] firstSector, FSBlockDeviceAPI devApi) {
		if(pte!=null) {
			if (pte instanceof IBMPartitionTableEntry)
			    if (((IBMPartitionTableEntry)pte).getSystemIndicator() != IBMPartitionTypes.PARTTYPE_LINUXNATIVE)
				return false;
		}

		//need to check the magic            
		ByteBuffer magic = ByteBuffer.allocate(2);
		try{
		    devApi.read(1024+56, magic);
		} catch(IOException e) {
		    return false;
		}
		return (Ext2Utils.get16(magic.array(), 0) == 0xEF53);
	}

	/**
	 * @see org.jnode.fs.FileSystemType#format(org.jnode.driver.Device, java.lang.Object)
	 */
	public synchronized Ext2FileSystem format(Device device, Object specificOptions) throws FileSystemException {
        //throw new FileSystemException("Not ye implemented");
		
		//currently the only option is the block size
		int blockSize = 1024*((Integer)specificOptions).intValue();
		
		Ext2FileSystem fs = new Ext2FileSystem(device, false);
		fs.create(blockSize);
		return fs;
	}
}
