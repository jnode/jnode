/**
 * $Id$
 */
package org.jnode.fs.fat;

import java.io.IOException;

/**
 * <description>
 * 
 * @author epr
 */
public class FatDirectory extends AbstractDirectory {

	private FatFile file;

	/**
	 * Constructor for Directory.
	 * @param fs
	 * @param file
	 */
	public FatDirectory(FatFileSystem fs, FatFile file) 
	throws IOException {
		super(fs, 0, file);
		this.file = file;
		read();
	}

	/** 
	 * Read the contents of this directory from the persistent storage at the
	 * given offset.
	 */
	protected synchronized void read() 
	throws IOException {
		entries = new FatDirEntry[(int)(file.getLengthOnDisk() / 32)];
		final byte[] data = new byte[entries.length * 32];
		file.read(0, data, 0, data.length);
		read(data);
		resetDirty();
	}

	/** 
	 * Write the contents of this directory to the given persistent storage at
	 * the given offset.
	 */
	protected synchronized void write() 
	throws IOException {
		final byte[] data = new byte[entries.length * 32];
		file.setLength(data.length);
		write(data);		
		file.write(0, data, 0, data.length);
		resetDirty();
	}	
	

	/** 
	 * Flush the contents of this directory to the persistent storage 
	 */
	protected void flush() 
	throws IOException {
		write();
	}
	
	/**
	 * @see org.jnode.fs.fat.AbstractDirectory#canChangeSize(int)
	 */
	protected boolean canChangeSize(int newSize) {
		return true;
	}
}
