package org.jnode.fs.ext2;

import java.io.IOException;

import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystem;

/**
 * @author Andras Nagy
 */
public class Ext2File implements FSFile {

	INode iNode;
	boolean valid;

	public Ext2File(INode iNode) {
		this.iNode=iNode;		
		valid = true;
	}

	/**
	 * @see org.jnode.fs.FSFile#getLength()
	 */
	public long getLength() {
		return iNode.getSize();
	}

	/**
	 * @see org.jnode.fs.FSFile#setLength(long)
	 */
	public void setLength(long length) throws IOException {
		throw new IOException("EXT2 implementation is currently readonly");
	}

	/**
	 * @see org.jnode.fs.FSFile#read(long, byte[], int, int)
	 */
	public void read(long fileOffset, byte[] dest, int off, int len) throws IOException {
		if(len+off>getLength())
			throw new IOException("Can't read past the file!");
		long blockSize = iNode.getExt2FileSystem().getBlockSize();
		long bytesRead=0;
		while( bytesRead < len ) {
			long blockNr = (fileOffset+bytesRead) / blockSize;
			long blockOffset = (fileOffset+bytesRead) % blockSize;
			long copyLength = Math.min(len-bytesRead, blockSize - blockOffset);
			
			System.arraycopy( 	iNode.getDataBlock(blockNr), (int)blockOffset,
								dest, off+(int)bytesRead, (int)copyLength);
								
			bytesRead += copyLength;
		}
	}
				

	/**
	 * @see org.jnode.fs.FSFile#write(long, byte[], int, int)
	 */
	public void write(long fileOffset, byte[] src, int off, int len) throws IOException {
		throw new IOException("EXT2 implementation is currently readonly");		
	}

	/**
	 * @see org.jnode.fs.FSObject#isValid()
	 */
	public boolean isValid() {
		return valid;
	}

	/**
	 * @see org.jnode.fs.FSObject#getFileSystem()
	 */
	public FileSystem getFileSystem() {
		return null;
	}

	/**
	 * Sets the valid status.
	 * @param valid The valid status to set
	 */
	public void setValid(boolean valid) {
		this.valid = valid;
	}

}
