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
	
	public NTFSFile(NTFSIndexEntry indexEntry) throws IOException
	{
		fileRecord = indexEntry.getParentFileRecord().getVolume().getIndexedFileRecord(indexEntry);
	}
	
	public long getLength() {
		if(fileRecord!= null)
			return fileRecord.getRealSize();
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSFile#setLength(long)
	 */
	public void setLength(long length) throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSFile#read(long, byte[], int, int)
	 */
	public void read(long fileOffset, byte[] dest, int off, int len) throws IOException {
		fileRecord.readData(fileOffset,dest,off,len);
	}

	/* (non-Javadoc)
	 * @see org.jnode.fs.FSFile#write(long, byte[], int, int)
	 */
	public void write(long fileOffset, byte[] src, int off, int len) throws IOException {
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
