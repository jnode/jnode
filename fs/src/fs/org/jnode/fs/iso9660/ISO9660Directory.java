/*
 * Created on 25.03.2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.jnode.fs.iso9660;

import java.io.IOException;
import java.util.Iterator;

import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FileSystem;


/**
 * @author Chira
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ISO9660Directory implements FSDirectory
{
	private ISO9660Entry entry = null;

	/**
	 * @param entry
	 */
	public ISO9660Directory(ISO9660Entry entry)
	{
		this.entry = entry;
	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSDirectory#iterator()
	 */
	public Iterator iterator() throws IOException
	{
		return new Iterator()
		{
			int offset = 0;
			EntryRecord parent = ISO9660Directory.this.entry.getCDFSentry();
			byte[] buffer = parent.getExtentData();
			
			public boolean hasNext()
			{
				return buffer[offset] > 0;
			}

			public Object next()
			{
				EntryRecord fEntry = new EntryRecord(parent.getVolume(),buffer,offset);
				offset += fEntry.getLengthOfDirectoryEntry(); 
				return new ISO9660Entry(fEntry);
			}

			public void remove()
			{
				throw new UnsupportedOperationException("Not yet implemented");
			}
			
		};
	}
	/* (non-Javadoc)
	 * @see org.jnode.fs.FSDirectory#getEntry(java.lang.String)
	 */
	public FSEntry getEntry(String name) throws IOException
	{
		for(Iterator it = this.iterator();it.hasNext();)
		{
			ISO9660Entry entry = (ISO9660Entry) it.next();
			if(entry.getName().equalsIgnoreCase(name))
				return entry;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSDirectory#addFile(java.lang.String)
	 */
	public FSEntry addFile(String name) throws IOException
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSDirectory#addDirectory(java.lang.String)
	 */
	public FSEntry addDirectory(String name) throws IOException
	{
		throw new UnsupportedOperationException("Not yet implemented");	
	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSDirectory#remove(java.lang.String)
	 */
	public void remove(String name) throws IOException
	{
		throw new UnsupportedOperationException("Not yet implemented");	
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

}
