/*
 * $Id$
 */
package org.jnode.fs.ntfs;

import java.io.*;

import org.jnode.fs.*;
import org.jnode.fs.FileSystem;
import org.jnode.fs.ntfs.attributes.*;

/**
 * @author vali
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class NTFSEntry implements FSEntry {

	private FSObject cachedFSObject = null;
	private NTFSIndexEntry indexEntry = null;
	/* (non-Javadoc)
	 * @see org.jnode.fs.FSEntry#getName()
	 */
	public NTFSEntry(NTFSFileRecord fileRecord)
	{
	}
	
	public NTFSEntry(NTFSIndexEntry indexEntry)
	{
		this.indexEntry = indexEntry;
	}
	
	public String getName() {
		if(indexEntry != null)
			return indexEntry.getFileName();
		return null;
	}			

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSEntry#getParent()
	 */
	public FSDirectory getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSEntry#getLastModified()
	 */
	public long getLastModified() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSEntry#isFile()
	 */
	public boolean isFile() {
		// TODO Auto-generated method stub
		return !isDirectory();
	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSEntry#isDirectory()
	 */
	public boolean isDirectory() {
		// TODO Auto-generated method stub
		return indexEntry.isDirectory();
	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSEntry#setName(java.lang.String)
	 */
	public void setName(String newName) throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSEntry#setLastModified(long)
	 */
	public void setLastModified(long lastModified) throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSEntry#getFile()
	 */
	public FSFile getFile() throws IOException {
		if(this.isFile())
		{	
			if(cachedFSObject == null)
				cachedFSObject = new NTFSFile(indexEntry);
			return (FSFile) cachedFSObject;
		}
		else
			return null;
		
	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSEntry#getDirectory()
	 */
	public FSDirectory getDirectory() throws IOException {
		if(this.isDirectory())
		{	
			if(cachedFSObject == null)
				cachedFSObject = new NTFSDirectory(this.getFileRecord().getVolume().getIndexedFileRecord(indexEntry));
			return (FSDirectory) cachedFSObject;
		}
		else
			return null;
	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSEntry#getAccessRights()
	 */
	public FSAccessRights getAccessRights() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSObject#isValid()
	 */
	public boolean isValid() {
		// TODO Auto-generated method stub
		return true;
	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSObject#getFileSystem()
	 */
	public FileSystem getFileSystem() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return Returns the fileRecord.
	 */
	public NTFSFileRecord getFileRecord() throws IOException {
		return indexEntry.getParentFileRecord().getVolume().getIndexedFileRecord(indexEntry);
	}


	/**
	 * @return Returns the indexEntry.
	 */
	public NTFSIndexEntry getIndexEntry()
	{
		return indexEntry;
	}
	/**
	 * @param indexEntry The indexEntry to set.
	 */
	public void setIndexEntry(NTFSIndexEntry indexEntry)
	{
		this.indexEntry = indexEntry;
	}
}
