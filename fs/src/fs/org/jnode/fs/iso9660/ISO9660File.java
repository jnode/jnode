/*
 * $Id$
 */
package org.jnode.fs.iso9660;

import java.io.IOException;

import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystem;


/**
 * @author Chira
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ISO9660File implements FSFile
{

	private ISO9660Entry entry = null;
	/**
	 * @param entry
	 */
	public ISO9660File(ISO9660Entry entry)
	{
		this.entry = entry;
	}

	/**
	 * @see org.jnode.fs.FSFile#getLength()
	 */
	public long getLength()
	{
		return entry.getCDFSentry().getDataLength();
	}

	/**
	 * @see org.jnode.fs.FSFile#setLength(long)
	 */
	public void setLength(long length) throws IOException
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/**
	 * @see org.jnode.fs.FSFile#read(long, byte[], int, int)
	 */
	public void read(long fileOffset, byte[] dest, int off, int len)
			throws IOException
	{
		this.entry.getCDFSentry().readFileData(fileOffset,dest,off,len);
	}

	/**
	 * @see org.jnode.fs.FSFile#write(long, byte[], int, int)
	 */
	public void write(long fileOffset, byte[] src, int off, int len)
			throws IOException
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/**
	 * @see org.jnode.fs.FSFile#flush()
	 */
	public void flush() throws IOException
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/**
	 * @see org.jnode.fs.FSObject#isValid()
	 */
	public boolean isValid()
	{
		return true;
	}

	/**
	 * @see org.jnode.fs.FSObject#getFileSystem()
	 */
	public FileSystem getFileSystem()
	{
		return null;
	}

}
