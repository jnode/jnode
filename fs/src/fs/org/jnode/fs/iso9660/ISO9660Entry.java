/*
 * $Id$
 */
package org.jnode.fs.iso9660;

import java.io.IOException;

import org.jnode.fs.FSAccessRights;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystem;


/**
 * @author Chira
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ISO9660Entry implements FSEntry
{
	
	private EntryRecord CDFSentry = null;
	
	public ISO9660Entry(EntryRecord entry)
	{
		this.CDFSentry = entry;
	}
	
	/* (non-Javadoc)
	 * @see org.jnode.fs.FSEntry#getName()
	 */
	public String getName()
	{
		return CDFSentry.getFileIdentifier();
	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSEntry#getParent()
	 */
	public FSDirectory getParent()
	{
		throw new UnsupportedOperationException("not yet implemented");
	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSEntry#getLastModified()
	 */
	public long getLastModified() throws IOException
	{
		throw new UnsupportedOperationException("not yet implemented");
	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSEntry#isFile()
	 */
	public boolean isFile()
	{
		return !CDFSentry.isDirectory();
	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSEntry#isDirectory()
	 */
	public boolean isDirectory()
	{
		return CDFSentry.isDirectory();
	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSEntry#setName(java.lang.String)
	 */
	public void setName(String newName) throws IOException
	{
		throw new UnsupportedOperationException("not yet implemented");
	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSEntry#setLastModified(long)
	 */
	public void setLastModified(long lastModified) throws IOException
	{
		throw new UnsupportedOperationException("not yet implemented");
	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSEntry#getFile()
	 */
	public FSFile getFile() throws IOException
	{
			return new ISO9660File(this);
	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSEntry#getDirectory()
	 */
	public FSDirectory getDirectory() throws IOException
	{
		return new ISO9660Directory(this);
	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSEntry#getAccessRights()
	 */
	public FSAccessRights getAccessRights() throws IOException
	{
		throw new UnsupportedOperationException("not yet implemented");
	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSObject#isValid()
	 */
	public boolean isValid()
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSObject#getFileSystem()
	 */
	public FileSystem getFileSystem()
	{
		return null;
	}

	/**
	 * @return Returns the cDFSentry.
	 */
	public EntryRecord getCDFSentry()
	{
		return CDFSentry;
	}
	/**
	 * @param sentry The cDFSentry to set.
	 */
	public void setCDFSentry(EntryRecord sentry)
	{
		CDFSentry = sentry;
	}
}
