package org.jnode.fs.ext2;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FileSystem;

/**
 * @author Andras Nagy
 */
public class Ext2Directory implements FSDirectory {

	protected static final Logger log = Logger.getLogger(Ext2Directory.class);

	INode iNode;

	public Ext2Directory(INode iNode) {
		this.iNode = iNode;
		//XXX
	}
	/**
	 * @see org.jnode.fs.FSDirectory#addDirectory(String)
	 */
	public FSEntry addDirectory(String name) {
		return null;
	}

	/**
	 * @see org.jnode.fs.FSDirectory#addFile(String)
	 */
	public FSEntry addFile(String name) {
		return null;
	}

	/**
	 * Return the number of the block that contains the given byte
	 */
	protected int translateToBlock(long index) {
		return (int) (index / iNode.getExt2FileSystem().getBlockSize());
	}

	/**
	 * Return the offset inside the block that contains the given byte
	 */
	protected int translateToOffset(long index) {
		return (int) (index % iNode.getExt2FileSystem().getBlockSize());
	}

	/**
	 * @see org.jnode.fs.FSDirectory#getEntry(String)
	 */
	public FSEntry getEntry(String name) {
		//parse the directory and search for the file
		Iterator iterator = iterator();
		while (iterator.hasNext()) {
			FSEntry entry = (FSEntry)iterator.next();
			if (entry.getName().equals(name))
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

		INode iteratedNode;

		public FSEntryIterator(INode iNode) {
			this.iteratedNode = iNode;

			lastBlockIndex = -1;
			blockIndex = 0;
			blockData = null;
			index = 0;
			current = null;

			log.debug("FSEntryIterator()");
		}

		/**
		 * @see java.util.Iterator#hasNext() hasNext() has to actually read the
		 *      next entry to see if it is a real entry or a not
		 */
		public boolean hasNext() {
			log.debug("FSEntryIterator.hasNext()");
			if (noMoreEntries)
				return false;

			if (index >= iteratedNode.getISize())
				return false;

			//read the inode number of the next entry:
			blockIndex = translateToBlock(index);
			blockOffset = translateToOffset(index);

			try {
				//read a new block if needed
				if (blockIndex != lastBlockIndex)
					blockData = iteratedNode.getDataBlock(blockIndex);
				lastBlockIndex = blockIndex;

				//get the next directory record
				Ext2DirectoryRecord dr = new Ext2DirectoryRecord(blockData, blockOffset);
				index += dr.getRecLen();

				//inode nr=0 means the end of the directory
				if (dr.getINodeNr() != 0) {
					current = dr;
					return true;
				} else {
					log.debug("FSEntryIterator.hasNext(): null inode");
					current = null;
					noMoreEntries = true;
					return false;
				}
			} catch (IOException e) {
				return false;
			}

		}

		/**
		 * @see java.util.Iterator#next()
		 */
		public Object next() {
			log.debug("FSEntryIterator.next()");

			if (current == null) {
				//hasNext reads the next element
				if (!hasNext())
					throw new NoSuchElementException();
			}

			Ext2DirectoryRecord dr = current;
			current = null;
			try {
				return new Ext2Entry(((Ext2FileSystem)getFileSystem()).getINode(dr.getINodeNr()), dr.getName());
			} catch (IOException e) {
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
		log.debug("Ext2Directory.Iterator()");
		return new FSEntryIterator(iNode);
	}

	/**
	 * @see org.jnode.fs.FSDirectory#remove(String)
	 */
	public void remove(String name) {
		// empty
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
		return false;
	}

}
