/*
 * $Id$
 */
package org.jnode.fs.ntfs;

import org.jnode.driver.*;
import org.jnode.fs.*;
import org.jnode.fs.partitions.*;

/**
 * @author Chira
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class NTFSFileSystemType implements FileSystemType
{

	public static final String NAME = "EXT2";
	
	
	public String getName() {
		return NAME;
	}
	
	/* (non-Javadoc)
	 * @see org.jnode.fs.FileSystemType#supports(org.jnode.fs.partitions.PartitionTableEntry, byte[])
	 */
	public boolean supports(PartitionTableEntry pte, byte[] firstSector)
	{
		// TODO Auto-generated method stub
		return false;
	}
	/* (non-Javadoc)
	 * @see org.jnode.fs.FileSystemType#create(org.jnode.driver.Device)
	 */
	public FileSystem create(Device device) throws FileSystemException
	{
		return new NTFSFileSystem(device);
	}
}
