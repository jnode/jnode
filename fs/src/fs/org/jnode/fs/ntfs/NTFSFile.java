/*
 * $Id$
 */
package org.jnode.fs.ntfs;

import java.io.IOException;

import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystem;
import org.jnode.fs.ntfs.attributes.NTFSIndexEntry;

/**
 * @author vali
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class NTFSFile implements FSFile {

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSFile#getLength()
	 */
	private NTFSFileRecord fileRecord = null;
	private NTFSIndexEntry indexEntry = null;
	
	public NTFSFile(NTFSIndexEntry indexEntry)
	{
		this.indexEntry = indexEntry; 
	}
	
	public long getLength() {
		return indexEntry.getRealFileSize();
	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSFile#setLength(long)
	 */
	public void setLength(long length) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSFile#read(long, byte[], int, int)
	 */
	public void read(long fileOffset, byte[] dest, int off, int len) throws IOException {
		getFileRecord().readData(fileOffset,dest,off,len);
	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSFile#write(long, byte[], int, int)
	 */
	public void write(long fileOffset, byte[] src, int off, int len) {
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

	/**
	 * @return Returns the fileRecord.
	 */
	public NTFSFileRecord getFileRecord() 
	{
		if(fileRecord == null)
			try {
				fileRecord = indexEntry.getParentFileRecord().getVolume().getIndexedFileRecord(indexEntry);
			} catch (IOException e) {
				e.printStackTrace();
			}
		return this.fileRecord;
	}

	/**
	 * @param fileRecord The fileRecord to set.
	 */
	public void setFileRecord(NTFSFileRecord fileRecord) {
		this.fileRecord = fileRecord;
	}

	/**
	 * Flush any cached data to the disk.
	 * @throws IOException
	 */
	public void flush()
	throws IOException {
	    // TODO implement me
	}
}
