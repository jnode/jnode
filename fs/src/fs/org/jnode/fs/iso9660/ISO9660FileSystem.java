/*
 * $Id$
 */
package org.jnode.fs.iso9660;

import java.io.IOException;

import org.jnode.driver.Device;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.spi.AbstractFileSystem;


/**
 * @author Chira
 */
public class ISO9660FileSystem extends AbstractFileSystem
{

	public static int DefaultLBNSize = 2048;

	private ISO9660Volume volume = null;

	/**
	 * @see org.jnode.fs.FileSystem#getDevice()
	 */
	public ISO9660FileSystem(Device device, boolean readOnly) throws FileSystemException	
	{
		super(device, readOnly);
		
		//byte[] buff = new byte[ISO9660FileSystem.DefaultLBNSize];
		try
		{
			volume = new ISO9660Volume(getApi());
		} catch (IOException e)
		{
			throw new FileSystemException(e);
		}
	}
	
	/**
	 * @see org.jnode.fs.FileSystem#getRootEntry()
	 */
	public FSEntry getRootEntry() throws IOException
	{
		return new ISO9660Entry(volume.getVolumeDescriptor().getRootDirectoryEntry());
		
	}
		
	/**
	 * @return Returns the volume.
	 */
	public ISO9660Volume getVolume() {
		return this.volume;
	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.AbstractFileSystem#flush()
	 */
	public void flush() throws IOException {
		//TODO: perhaps nothing todo (always readOnly ?)
	}
}
