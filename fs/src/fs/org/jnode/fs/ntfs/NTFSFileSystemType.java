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

	public static final String NAME = "NTFS";
	
	
	public String getName() {
		return NAME;
	}
	
	/* (non-Javadoc)
	 * @see org.jnode.fs.FileSystemType#supports(org.jnode.fs.partitions.PartitionTableEntry, byte[])
	 */
	public boolean supports(PartitionTableEntry pte, byte[] firstSector)
	{
		return new String(firstSector,0x03,8).startsWith("NTFS");
	}
	/* (non-Javadoc)
	 * @see org.jnode.fs.FileSystemType#create(org.jnode.driver.Device)
	 */
	public FileSystem create(Device device) throws FileSystemException
	{
		return new NTFSFileSystem(device);
	}
}
