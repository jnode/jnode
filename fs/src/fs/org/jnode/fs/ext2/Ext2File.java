package org.jnode.fs.ext2;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystem;

/**
 * @author Andras Nagy
 */
public class Ext2File implements FSFile {
   private static final Logger log = Logger.getLogger(Ext2File.class);
	INode iNode;

	public Ext2File(INode iNode) {
		this.iNode = iNode;
	}

	/**
	 * @see org.jnode.fs.FSFile#getLength()
	 */
	public long getLength() {
		return iNode.getISize();
	}

	/**
	 * @see org.jnode.fs.FSFile#setLength(long)
	 */
	public void setLength(long length) {
		// empty
	}

	/**
	 * @see org.jnode.fs.FSFile#read(long, byte[], int, int)
	 */
	public void read(long fileOffset, byte[] dest, int off, int len) throws IOException {
		long blockSize = iNode.getExt2FileSystem().getBlockSize();
		long bytesRead = 0;
		while (bytesRead < len) {
			long blockNr = (fileOffset + bytesRead) / blockSize;
			long blockOffset = (fileOffset + bytesRead) % blockSize;
			long copyLength = Math.min(len - bytesRead, blockSize - blockOffset);

			log.debug(
				"blockNr: " + blockNr + ", blockOffset: " + blockOffset + ", copyLength: " + copyLength);
			System.arraycopy(
				iNode.getDataBlock(blockNr),
				(int)blockOffset,
				dest,
				off + (int)bytesRead,
				(int)copyLength);

			bytesRead += copyLength;
		}
	}

	/**
	 * @see org.jnode.fs.FSFile#write(long, byte[], int, int)
	 */
	public void write(long fileOffset, byte[] src, int off, int len) {
		// empty
	}

	/**
	 * @see org.jnode.fs.FSObject#isValid()
	 */
	public boolean isValid() {
		return false;
	}

	/**
	 * @see org.jnode.fs.FSObject#getFileSystem()
	 */
	public FileSystem getFileSystem() {
		return null;
	}

}
