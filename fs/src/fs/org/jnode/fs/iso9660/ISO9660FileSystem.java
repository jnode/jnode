/*
 * $Id$
 */
package org.jnode.fs.iso9660;

import java.io.IOException;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;


/**
 * @author Chira
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ISO9660FileSystem implements FileSystem
{

	public static int DefaultLBNSize = 2048;

	private Device device = null;
	private final BlockDeviceAPI api;
	private ISO9660Volume volume = null;
	/* (non-Javadoc)
	 * @see org.jnode.fs.FileSystem#getDevice()
	 */
	public ISO9660FileSystem(Device device) throws FileSystemException 
	{
		if (device == null)
			throw new FileSystemException("null device!");

		this.device = device;
		try {
			api = (BlockDeviceAPI)device.getAPI(BlockDeviceAPI.class);
		} catch (ApiNotFoundException ex) {
			throw new FileSystemException(ex);
		}
		byte[] buff = new byte[ISO9660FileSystem.DefaultLBNSize];
		try
		{
			volume = new ISO9660Volume(api);
		} catch (IOException e)
		{
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
		return new ISO9660Entry(volume.getVolumeDescriptor().getRootDirectoryEntry());
		
	}
	/* (non-Javadoc)
	 * @see org.jnode.fs.FileSystem#close()
	 */
	public void close()
	{
		// TODO Auto-generated method stub
	}
	/**
	 * @return Returns the volume.
	 */
	public ISO9660Volume getVolume() {
		return this.volume;
	}


}
