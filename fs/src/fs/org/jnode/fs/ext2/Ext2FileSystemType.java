package org.jnode.fs.ext2;

import org.apache.log4j.Logger;

import org.jnode.driver.Device;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.FileSystemType;
import org.jnode.fs.partitions.PartitionTableEntry;
import org.jnode.fs.partitions.ibm.IBMPartitionTableEntry;

/**
 * @author Andras Nagy
 */
public class Ext2FileSystemType implements FileSystemType {
   private static final Logger log = Logger.getLogger(Ext2FileSystemType.class);
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
		//see if the superblock contains the magic
		//XXX:  if we don't get 2048 bytes, we don't have the superblock
		//		how can we decide then?
		
		log.info("Try to mount ext2fs");
		
		if(pte instanceof IBMPartitionTableEntry)
			log.info("PARTITION TABLE startLBA: "+((IBMPartitionTableEntry)pte).getStartLba());
		else
			return false;
		
		/*
		if(firstSector.length < 1024+Superblock.SUPERBLOCK_LENGTH) {
			log.info("supports(): first block not long enough"); 
			return false;
		}
		
		try{
			byte[] sb = new byte[Superblock.SUPERBLOCK_LENGTH];
			System.arraycopy(firstSector, 1024, sb, 0, Superblock.SUPERBLOCK_LENGTH);
			new Superblock(sb);
		} catch(FileSystemException e) {
			return false;
		}
		//superblock constructed successfully
		 */
		//XXX figure out how to decide
		return true;
	}
}
