/*
 * $Id$
 */
package org.jnode.fs.ntfs;

import java.io.*;

import org.jnode.driver.*;
import org.jnode.driver.block.*;
import org.jnode.fs.*;

/**
 * @author Chira
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class NTFSFileSystem implements FileSystem
{
	
	private Device device = null;
	private final BlockDeviceAPI api;
	private NTFSVolume volume = null;
	/* (non-Javadoc)
	 * @see org.jnode.fs.FileSystem#getDevice()
	 */
	public NTFSFileSystem(Device device) throws FileSystemException 
	{
		if (device == null)
			throw new FileSystemException("null device!");

		this.device = device;
		try {
			api = (BlockDeviceAPI)device.getAPI(BlockDeviceAPI.class);
		} catch (ApiNotFoundException ex) {
			throw new FileSystemException(ex);
		}
		
		try {
			// initialize the NTFE volume
			volume = new NTFSVolume(api);
		} catch (IOException e) {
			throw new FileSystemException(e);
		}
	}
	
	public Device getDevice()
	{
		return device;
	}
	/* (non-Javadoc)
	 * @see org.jnode.fs.FileSystem#getRootEntry()
	 */
	public FSEntry getRootEntry() throws IOException
	{
		return new NTFSDirectory(
				volume.getRootDirectory()
		).getEntry(".");
		
	}
	/* (non-Javadoc)
	 * @see org.jnode.fs.FileSystem#close()
	 */
	public void close() throws IOException
	{
		// TODO Auto-generated method stub
	}
	/**
	 * @return Returns the volume.
	 */
	public NTFSVolume getNTFSVolume() {
		return this.volume;
	}

}
