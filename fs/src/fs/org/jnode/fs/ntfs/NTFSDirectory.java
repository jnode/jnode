/*
 * $Id$
 */
package org.jnode.fs.ntfs;

import java.io.*;
import java.util.*;

import org.jnode.fs.*;
import org.jnode.fs.FileSystem;
import org.jnode.fs.ntfs.attributes.*;

/**
 * @author vali
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class NTFSDirectory implements FSDirectory {

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSDirectory#iterator()
	 */
	NTFSIndex index = null;
	
	public NTFSDirectory(NTFSFileRecord record)
	{
		this.index = new NTFSIndex(record);
	}
	
	public Iterator iterator() throws IOException 
	{
		return new Iterator()
		{

			Iterator it = index.iterator();
			
			public boolean hasNext()
			{
				return it.hasNext();
			}

			public Object next()
			{
				return  new NTFSEntry((NTFSIndexEntry)it.next());
			}

			public void remove()
			{
				it.remove();
			}
		};
	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSDirectory#getEntry(java.lang.String)
	 */
	public FSEntry getEntry(String name) throws IOException {
		for(Iterator it = this.iterator();it.hasNext();)
		{
			NTFSEntry entry = (NTFSEntry) it.next();
			if(entry.getName().equals(name))
				return entry;
		}
			
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSDirectory#addFile(java.lang.String)
	 */
	public FSEntry addFile(String name) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSDirectory#addDirectory(java.lang.String)
	 */
	public FSEntry addDirectory(String name) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSDirectory#remove(java.lang.String)
	 */
	public void remove(String name) throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSObject#isValid()
	 */
	public boolean isValid() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSObject#getFileSystem()
	 */
	public FileSystem getFileSystem() {
		// TODO Auto-generated method stub
		return null;
	}
}
