/*
 * $Id$
 */
package org.jnode.fs.ext2;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
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
	private Ext2FileSystem fs;
	private final Logger log = Logger.getLogger(getClass());
	private boolean readOnly;
			
	public Ext2Directory(INode iNode, Ext2FileSystem fs) {
		this.iNode=iNode;
		valid=true;
		this.fs = fs;
		log.setLevel(Level.DEBUG);
		if((iNode.getFlags() & Ext2Constants.EXT2_INDEX_FL)== 1)
			readOnly = true;		//force readonly
		else
			readOnly = fs.isReadOnly();
		
		log.debug("directory size: "+iNode.getSize());
	}
	
	/**
	 * @see org.jnode.fs.FSDirectory#addDirectory(String)
	 */
	public FSEntry addDirectory(String name) throws IOException {
		if(isReadOnly())
			throw new IOException("Filesystem or directory is mounted read-only!");
			 
		//create a new iNode for the file
		//TODO: access rights, file type, UID and GID should be passed through the FSDirectory interface
		INode newINode;
		Ext2DirectoryRecord dr;
		try{
			int rights = 0xFFFF & (Ext2Constants.EXT2_S_IRWXU | Ext2Constants.EXT2_S_IRWXG | Ext2Constants.EXT2_S_IRWXO);
			newINode = fs.createINode((int)iNode.getGroup(), Ext2Constants.EXT2_S_IFDIR, rights, 0, 0);
			
			dr = new Ext2DirectoryRecord(newINode.getINodeNr(), Ext2Constants.EXT2_FT_DIR, name);
		}catch(FileSystemException fse) {
			throw new IOException(fse);
		}
		
		try{
			addDirectoryRecord(dr);
		}catch(FileSystemException fse) {
			throw new IOException(fse);
		}
		
		return new Ext2Entry(newINode, name, Ext2Constants.EXT2_FT_REG_FILE, fs); 
	}

	/**
	 * @see org.jnode.fs.FSDirectory#addFile(String)
	 */
	public FSEntry addFile(String name) throws IOException {
		if(isReadOnly())
			throw new IOException("Filesystem or directory is mounted read-only!");
			
		//create a new iNode for the file
		//TODO: access rights, file type, UID and GID should be passed through the FSDirectory interface
		INode newINode;
		Ext2DirectoryRecord dr;
		try{
			int rights = 0xFFFF & (Ext2Constants.EXT2_S_IRWXU | Ext2Constants.EXT2_S_IRWXG | Ext2Constants.EXT2_S_IRWXO);
			newINode = fs.createINode((int)iNode.getGroup(), Ext2Constants.EXT2_S_IFREG, rights, 0, 0);
			
			dr = new Ext2DirectoryRecord(newINode.getINodeNr(), Ext2Constants.EXT2_FT_REG_FILE, name);
		}catch(FileSystemException fse) {
			throw new IOException(fse);
		}
		
		try{
			addDirectoryRecord(dr);
		}catch(FileSystemException fse) {
			throw new IOException(fse);
		}
		
		return new Ext2Entry(newINode, name, Ext2Constants.EXT2_FT_REG_FILE, fs); 
	}
	
	private void addDirectoryRecord(Ext2DirectoryRecord dr) throws IOException, FileSystemException{
		Ext2File dir = new Ext2File(iNode);		//read itself as a file
		
		//find the last directory record
		FSEntryIterator iterator = (FSEntryIterator)iterator();
		Ext2DirectoryRecord rec=null;
		while(iterator.hasNext()) {
			rec = iterator.nextDirectoryRecord();
		}
		
		long lastPos = rec.getFileOffset();
		long lastLen = rec.getRecLen();
		
		//truncate the last record to its minimal size (cut the padding from the end)
		rec.truncateRecord();
		//directoryRecords may not extend over block boundaries:
		//	see if the new record fits in the same block after truncating the last record
		long remainingLength = fs.getBlockSize() - (lastPos%fs.getBlockSize()) - rec.getRecLen();
		log.debug("LAST-1 record: begins at: "+lastPos+", length: "+lastLen);
		log.debug("LAST-1 truncated length: "+rec.getRecLen());
		log.debug("Remaining length: "+remainingLength);
		if(remainingLength >= dr.getRecLen()) {			
			//write back the last record truncated
			dir.write( lastPos, rec.getData(), rec.getOffset(), rec.getRecLen() );

			//pad the end of the new record with zeroes
			dr.expandRecord(lastPos+rec.getRecLen(), lastPos+rec.getRecLen()+remainingLength);
			//append the new record at the end of the list
			dir.write( lastPos+rec.getRecLen(), dr.getData(), dr.getOffset(), dr.getRecLen() );
			log.debug("addDirectoryRecord(): LAST   record: begins at: "+
					 (rec.getFileOffset()+rec.getRecLen())+", length: "+dr.getRecLen());
		} else {
			//the new record must go to the next block
			//(the previously last record (rec) stays padded to the end of the block, so we can 
			// append after that)
			dr.expandRecord(lastPos+lastLen, lastPos+lastLen+fs.getBlockSize());
			
			dir.write( lastPos+lastLen, dr.getData(), dr.getOffset(), dr.getRecLen() );
			log.debug("addDirectoryRecord(): LAST   record: begins at: "+(lastPos+lastLen)+", length: "+dr.getRecLen());	
		}
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
	public FSEntry getEntry(String name) throws IOException{
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
		byte data[];
		int index;
		
		Ext2DirectoryRecord current;
		
		public FSEntryIterator(INode iNode)throws IOException {
			//read itself as a file
			Ext2File directoryFile = new Ext2File(iNode);
			//read the whole directory
			data = new byte[(int)directoryFile.getLength()];
			directoryFile.read(0, data, 0, (int)directoryFile.getLength());
			index = 0;
		}
		
		public boolean hasNext() {
			Ext2DirectoryRecord dr;
			do {
				if(index>=iNode.getSize())
					return false;
				
				dr = new Ext2DirectoryRecord(data, index, index);
				index+=dr.getRecLen();				
			} while(dr.getINodeNr()==0);			//inode nr=0 means the entry is unused

			current = dr;
			return true;
		}
		
		public Object next() {
			
			if(current == null) {
				//hasNext actually reads the next element
				if(!hasNext())
					throw new NoSuchElementException();
			}
			
			Ext2DirectoryRecord dr = current;
			current = null;
			try{
				return new Ext2Entry( ((Ext2FileSystem)getFileSystem()).getINode(dr.getINodeNr()),
										dr.getName(), dr.getType(), fs );
			}catch(IOException e) {
				throw new NoSuchElementException("Root cause: "+e.getMessage());
			}catch(FileSystemException e) {
				throw new NoSuchElementException("Root cause: "+e.getMessage());
			}	
		}
		
		/**
		 * Returns the next record as an Ext2DirectoryRecord instance
		 * @return
		 */
		protected Ext2DirectoryRecord nextDirectoryRecord() {
			if(current == null) {
				//hasNext actually reads the next element
				if(!hasNext())
					throw new NoSuchElementException();
			}
			
			Ext2DirectoryRecord dr = current;
			current = null;
			return dr;
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
	public Iterator iterator() throws IOException {
		log.debug("Ext2Directory.Iterator()");
		return new FSEntryIterator(iNode);				
	}

	/**
	 * @see org.jnode.fs.FSDirectory#remove(String)
	 */
	public void remove(String name) throws IOException {
		if(isReadOnly())
			throw new IOException("Filesystem or directory is mounted read-only!");
		throw new IOException("remove not yet implemented");
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

	private boolean isReadOnly() {
		return readOnly;
	}
}
