/*
 * $Id$
 */
package org.jnode.fs.ext2;

import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;

/**
 * @author Andras Nagy
 */
public class Ext2File implements FSFile {

	INode iNode;
	boolean valid;
	private final Logger log = Logger.getLogger(getClass());

	public Ext2File(INode iNode) {
		this.iNode=iNode;		
		valid = true;
		log.setLevel(Level.DEBUG);
	}

	/**
	 * @see org.jnode.fs.FSFile#getLength()
	 */
	public long getLength() {
		//log.debug("getLength(): "+iNode.getSize());
		return iNode.getSize();
	}

	private long getLengthInBlocks() {
		return iNode.getSizeInBlocks();
	}

	/**
	 * @see org.jnode.fs.FSFile#setLength(long)
	 */
	public void setLength(long length) throws IOException {
		long blockSize = iNode.getExt2FileSystem().getBlockSize();
		
		//if length<getLength(), then the file is truncated
		if(length<getLength()) {
			long blockNr 	 = length / blockSize;
			long blockOffset = length % blockSize;
			long nextBlock;
			if(blockOffset==0)
				nextBlock=blockNr;
			else
				nextBlock=blockNr+1;
				
			for(long i=iNode.getAllocatedBlockCount()-1; i>=nextBlock; i--)  {
				log.debug("setLength(): freeing up block "+i+" of inode");
				try{
					iNode.freeDataBlock(i);
				}catch(FileSystemException fse) {
					throw new IOException(fse);
				}
				
			}
			iNode.setSize(length);

			iNode.setMtime(System.currentTimeMillis()/1000);

			return;
		}
		
		//if length>getLength(), then new blocks are allocated for the file
		//The content of the new blocks is undefined (see the setLength(long i) 
		//method of java.io.RandomAccessFile
		if(length>getLength()) {
			long len = length - getLength();
			long blocksAllocated = getLengthInBlocks();
			long bytesAllocated  = getLength();
			long bytesCovered=0;
			while( bytesCovered < len ) {
				long blockIndex = 	(bytesAllocated+bytesCovered) / blockSize;
				long blockOffset = 	(bytesAllocated+bytesCovered) % blockSize;
				long newSection = Math.min(len-bytesCovered, blockSize - blockOffset);
			
				//allocate a new block if needed
				if(blockIndex >= blocksAllocated) {
					try{
						iNode.allocateDataBlock(blockIndex);
					}catch(FileSystemException fe) {
						throw new IOException("Internal filesystem exception",fe);
					}
					blocksAllocated++;
				}
				
				bytesCovered += newSection;
			}
		iNode.setSize(length);

		iNode.setMtime(System.currentTimeMillis()/1000);
		
		return;
		}		
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
	 * Write into the file. fileOffset is between 0 and getLength() (see the methods 
	 * write(byte[], int, int), setPosition(long), setLength(long) in 
	 * org.jnode.fs.service.def.FileHandleImpl)
	 * 
	 * @see org.jnode.fs.FSFile#write(long, byte[], int, int)
	 */
	public void write(long fileOffset, byte[] src, int off, int len) throws IOException {
		//throw new IOException("EXT2 implementation is currently readonly");
		if(fileOffset > getLength())
			throw new IOException("Can't write beyond the end of the file! (fileOffset: "+
			fileOffset+", getLength()"+getLength());
		if(off+len>src.length)
			throw new IOException("src is shorter than what you want to write");
		
		log.debug("write(fileOffset="+fileOffset+", src, off, len="+len+")");
		
		final long blockSize = iNode.getExt2FileSystem().getBlockSize();
		long blocksAllocated = iNode.getAllocatedBlockCount();
		long bytesWritten=0;
		while( bytesWritten < len ) {
			long blockIndex  = (fileOffset+bytesWritten) / blockSize;
			long blockOffset = (fileOffset+bytesWritten) % blockSize;
			long copyLength = Math.min(len-bytesWritten, blockSize - blockOffset);
			
			//If only a part of the block is written, then read the block 
			//and update its contents with the data in src. If the whole block
			//is overwritten, then skip reading it.
			byte[] dest;
			if( !( (blockOffset==0)&&(copyLength==blockSize) ) &&
				 (blockIndex < blocksAllocated)) 
				dest = iNode.getDataBlock(blockIndex);
			else 
				dest = new byte[(int)blockSize];
			
			System.arraycopy( src, (int)(off+bytesWritten), dest, (int)blockOffset, (int)copyLength);
						
			//allocate a new block if needed
			if(blockIndex >= blocksAllocated) {
				try{
					iNode.allocateDataBlock(blockIndex);
				}catch(FileSystemException fe) {
					throw new IOException("Internal filesystem exception",fe);
				}
				blocksAllocated++;
			}

			//write the block
			iNode.writeDataBlock(blockIndex, dest);
			
			bytesWritten += copyLength;
		}
		iNode.setSize( fileOffset+len );
		
		iNode.setMtime(System.currentTimeMillis()/1000);
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

	/**
	 * Flush any cached data to the disk.
	 * @throws IOException
	 */
	public void flush() throws IOException {
		log.debug("Ext2File.flush()");
		try{
			iNode.update();
		}catch(FileSystemException fse) {
			throw new IOException(fse);
		}
		//update the group descriptors and superblock: needed if blocks have been 
		//allocated or deallocated
		iNode.getExt2FileSystem().updateFS();
	}
}
