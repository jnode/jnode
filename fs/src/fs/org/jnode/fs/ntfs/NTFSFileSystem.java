/*
 * $Id$
 */
package org.jnode.fs.ntfs;

import java.io.IOException;

import org.jnode.driver.Device;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.spi.AbstractFileSystem;

/**
 * @author Chira
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class NTFSFileSystem extends AbstractFileSystem
{
	
	private NTFSVolume volume = null;
	/* (non-Javadoc)
	 * @see org.jnode.fs.FileSystem#getDevice()
	 */
	public NTFSFileSystem(Device device, boolean readOnly) throws FileSystemException 
	{
		super(device, readOnly);
		
		try {
			// initialize the NTFE volume
			volume = new NTFSVolume(getApi());
		} catch (IOException e) {
			throw new FileSystemException(e);
		}
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
	
	/**
	 * @return Returns the volume.
	 */
	public NTFSVolume getNTFSVolume() {
		return this.volume;
	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.AbstractFileSystem#flush()
	 */
	public void flush() throws IOException {
		// TODO Auto-generated method stub						
	}

}
