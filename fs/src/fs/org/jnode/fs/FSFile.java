/*
 * $Id$
 */
package org.jnode.fs;

import java.io.IOException;

/**
 * A FSFile is a representation of a single block of bytes on a filesystem.
 * It is comparable to an inode in Unix.
 * 
 * An FSFile does not have any knownledge of who is using this file. It is also possible
 * that is system uses a single FSFile instances to create two inputstream's for
 * two different principals.
 * 
 * @author epr
 */
public interface FSFile extends FSObject {

	/**
	 * Gets the length (in bytes) of this file
	 * @return long
	 */
	public long getLength();
	
	/**
	 * Sets the length of this file.
	 * @param length
	 * @throws IOException
	 */
	public void setLength(long length)
	throws IOException;

	/**
	 * Read <code>len</code> bytes from the given position.
	 * The read data is read fom this file starting at offset <code>fileOffset</code>
	 * and stored in <code>dest</code> starting at offset <code>ofs</code>.
	 * @param fileOffset
	 * @param dest
	 * @param off
	 * @param len
	 * @throws IOException
	 */	
	public void read(long fileOffset, byte[] dest, int off, int len)
	throws IOException;
	
	/**
	 * Write <code>len</code> bytes to the given position. 
	 * The data is read from <code>src</code> starting at offset
	 * <code>ofs</code> and written to this file starting at offset <code>fileOffset</code>.
	 * @param fileOffset
	 * @param src
	 * @param off
	 * @param len
	 * @throws IOException
	 */	
	public void write(long fileOffset, byte[] src, int off, int len)
	throws IOException;
	
	/**
	 * Flush any cached data to the disk.
	 * @throws IOException
	 */
	public void flush()
	throws IOException;
}
