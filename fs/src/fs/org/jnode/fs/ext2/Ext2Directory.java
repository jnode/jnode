/*
 * $Id$
 */
package org.jnode.fs.ext2;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;

/**
 * @author Andras Nagy
 */
public class Ext2Directory implements FSDirectory {
	
	INode iNode;
	boolean valid;
		
	public Ext2Directory(INode iNode) {
		this.iNode=iNode;
		valid=true;
	}
	/**
	 * @see org.jnode.fs.FSDirectory#addDirectory(String)
	 */
	public FSEntry addDirectory(String name) throws IOException {
		throw new IOException("EXT2 implementation is currently readonly");
	}

	/**
	 * @see org.jnode.fs.FSDirectory#addFile(String)
	 */
	public FSEntry addFile(String name) throws IOException {
		throw new IOException("EXT2 implementation is currently readonly");
	}

	/** 
	 * Return the number of the block that contains the given byte
	 */
	int translateToBlock(long index) {
		return (int)(index / iNode.getExt2FileSystem().getBlockSize());
	}
	
	/**
	 * Return the offset inside the block that contains the given byte
	 */
	int translateToOffset(long index) {
		return (int)(index % iNode.getExt2FileSystem().getBlockSize());
	}

	/**
	 * @see org.jnode.fs.FSDirectory#getEntry(String)
	 */
	public FSEntry getEntry(String name) {
		//parse the directory and search for the file
		Iterator iterator=iterator();
		while(iterator.hasNext()) {
			FSEntry entry = (FSEntry)iterator.next();
			if(entry.getName().equals(name))
				return entry;
		}
		return null;
	}

	class FSEntryIterator implements Iterator {
		int lastBlockIndex;
		int blockIndex;
		int blockOffset;
		byte blockData[];
		int index;
		Ext2DirectoryRecord current;
		boolean noMoreEntries = false;
		
		//INode iNode;
		
		public FSEntryIterator(INode iNode) {
			//this.iNode = iNode;
			
			lastBlockIndex = -1;
			blockIndex = 0;
			//the byte index where the directory parsing has reached
			index=0;
			//the Ext2DirectoryRecord that has been read last
			current = null;
			
			Ext2Debugger.debug("FSEntryIterator()",2);
		}
		
		/**
		 * @see java.util.Iterator#hasNext()
		 * hasNext() has to actually read the next entry to see if
		 * it is a real entry or a not
		 */
		public boolean hasNext() {
			Ext2Debugger.debug("FSEntryIterator.hasNext()",3);
			if(noMoreEntries)
				return false;
			
			if(index>=iNode.getSize())
				return false;
				
			//read the inode number of the next entry:
			blockIndex = Ext2Directory.this.translateToBlock( index );
			blockOffset= Ext2Directory.this.translateToOffset( index );
			
			try{
				//read a new block if needed
				if(blockIndex != lastBlockIndex) {
					blockData = iNode.getDataBlock(blockIndex);
					lastBlockIndex = blockIndex;
				}
			
				//get the next directory record
				Ext2DirectoryRecord dr = new Ext2DirectoryRecord(blockData, blockOffset);
				index+=dr.getRecLen();
								
				//inode nr=0 means the end of the directory
				if(dr.getINodeNr()!=0) {
					current = dr;
					return true;
				}
				else {
					Ext2Debugger.debug("FSEntryIterator.hasNext(): null inode",2);
					current = null;
					noMoreEntries=true;
					return false;
				}
			}catch(IOException e) {
				return false;
			}
		}

		/**
		 * @see java.util.Iterator#next()
		 */
		public Object next() {
			Ext2Debugger.debug("FSEntryIterator.next()",2);
			
			if(current == null) {
				//hasNext actually reads the next element
				if(!hasNext())
					throw new NoSuchElementException();
			}
			
			Ext2DirectoryRecord dr = current;
			current = null;
			try{
				return new Ext2Entry( ((Ext2FileSystem)getFileSystem()).getINode(dr.getINodeNr()),
										dr.getName(), dr.getType() );
			}catch(IOException e) {
				throw new NoSuchElementException();
			}catch(FileSystemException e) {
				throw new NoSuchElementException();
			}	
		}

		/**
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			throw new UnsupportedOperationException("No remove operation!");
		}
	}


	/**
	 * @see org.jnode.fs.FSDirectory#iterator()
	 */
	public Iterator iterator() {
		Ext2Debugger.debug("Ext2Directory.Iterator()",2);
		return new FSEntryIterator(iNode);				
	}

	/**
	 * @see org.jnode.fs.FSDirectory#remove(String)
	 */
	public void remove(String name) throws IOException {
		throw new IOException("EXT2 implementation is currently readonly");
	}

	/**
	 * @see org.jnode.fs.FSObject#getFileSystem()
	 */
	public FileSystem getFileSystem() {
		return iNode.getExt2FileSystem();
	}

	/**
	 * @see org.jnode.fs.FSObject#isValid()
	 */
	public boolean isValid() {
		return valid;
	}
}
