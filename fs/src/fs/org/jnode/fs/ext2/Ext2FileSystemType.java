/*
 * $Id$
 */
package org.jnode.fs.ext2;

import java.io.IOException;

import org.jnode.driver.Device;
import org.jnode.driver.block.FSBlockDeviceAPI;
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
	 * @see org.jnode.fs.FileSystemType#create(Device, boolean)
	 */
	public synchronized FileSystem create(Device device, boolean readOnly) throws FileSystemException {
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
				return (((IBMPartitionTableEntry)pte).getSystemIndicator() == IBMPartitionTypes.PARTTYPE_LINUXNATIVE);
		}
		else {	//no partition table entry (e.g. ramdisk)
			//need to check the magic
			byte[] magic=new byte[2];
			try{
				devApi.read(1024+56, magic, 0, 2);
			}catch(IOException e) {
				return false;
			}
			return (Ext2Utils.get16(magic,0)==0xEF53);
		}
		return false;
	}

	/**
	 * @see org.jnode.fs.FileSystemType#format(org.jnode.driver.Device, java.lang.Object)
	 */
	public synchronized FileSystem format(Device device, Object specificOptions) throws FileSystemException {
        //throw new FileSystemException("Not ye implemented");
		
		//currently the only option is the block size
		int blockSize = 1024*((Integer)specificOptions).intValue();
		
		Ext2FileSystem fs = new Ext2FileSystem(device, false);
		fs.create(blockSize);
		return fs;
	}
}