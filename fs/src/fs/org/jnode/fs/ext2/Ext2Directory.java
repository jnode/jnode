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
	private Ext2Entry entry;
	private final Logger log = Logger.getLogger(getClass());
	private boolean readOnly;
			
	/**
	 * 
	 * @param iNode
	 * @param fs
	 * @param entry	the Ext2Entry representing this directory
	 */
	public Ext2Directory(INode iNode, Ext2FileSystem fs, Ext2Entry entry) {
		this.iNode=iNode;
		valid=true;
		this.fs = fs;
		this.entry = entry;
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
		Ext2Entry newEntry;
		try{
			int rights = 0xFFFF & (Ext2Constants.EXT2_S_IRWXU | Ext2Constants.EXT2_S_IRWXG | Ext2Constants.EXT2_S_IRWXO);
			newINode = fs.createINode((int)iNode.getGroup(), Ext2Constants.EXT2_S_IFDIR, rights, 0, 0);
			
			dr = new Ext2DirectoryRecord(newINode.getINodeNr(), Ext2Constants.EXT2_FT_DIR, name);

			addDirectoryRecord(dr);

			newEntry = new Ext2Entry(newINode, name, Ext2Constants.EXT2_FT_DIR, fs, this.entry);
			
			//add "."
			
			Ext2Directory newDir = new Ext2Directory(newINode, fs, newEntry);
			Ext2DirectoryRecord drThis = new Ext2DirectoryRecord( newINode.getINodeNr(), Ext2Constants.EXT2_FT_DIR, "." );
			newDir.addDirectoryRecord( drThis );
			newINode.setLinksCount( 2 );
						
			//add ".."
			long parentINodeNr = ((Ext2Directory)entry.getDirectory()).getINode().getINodeNr();
			Ext2DirectoryRecord drParent=new Ext2DirectoryRecord(parentINodeNr, Ext2Constants.EXT2_FT_DIR, ".." );
			newDir.addDirectoryRecord( drParent );
			
			//increase the reference count for the parent directory
			INode parentINode = fs.getINode((int)parentINodeNr);
			//to be able to synchronize to the inode object requires that an inode cache is used and
			//it contains only one copy of any inode
			synchronized(parentINode) {
				parentINode.setLinksCount( parentINode.getLinksCount()+1 ); 
			}
			
			//update the number of used directories in the block group
			int group = (int)( (newINode.getINodeNr()-1) / fs.getSuperblock().getINodesPerGroup()) ;
			fs.modifyUsedDirsCount(group, 1);
			
		}catch(FileSystemException fse) {
			throw new IOException(fse);
		}
		
		return newEntry; 
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

			addDirectoryRecord(dr);
		}catch(FileSystemException fse) {
			throw new IOException(fse);
		}
		
		return new Ext2Entry(newINode, name, Ext2Constants.EXT2_FT_REG_FILE, fs, this.entry); 
	}
	
	private void addDirectoryRecord(Ext2DirectoryRecord dr) throws IOException, FileSystemException{
		Ext2File dir = new Ext2File(iNode);		//read itself as a file

		//find the last directory record (if any)
		FSEntryIterator iterator = (FSEntryIterator)iterator();
		Ext2DirectoryRecord rec=null;
		while(iterator.hasNext()) {
			rec = iterator.nextDirectoryRecord();
		}
		
		if(rec!=null) {
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
		} else {	//rec==null, ie. this is the first record in the directory
			dr.expandRecord(0, fs.getBlockSize());
			dir.write(0, dr.getData(), dr.getOffset(), dr.getRecLen());
			log.debug("addDirectoryRecord(): LAST   record: begins at: 0, length: "+dr.getRecLen());				
		}


		
		iNode.setMtime(System.currentTimeMillis()/1000);

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
	
	private INode getINode() {
		return iNode;
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
										dr.getName(), dr.getType(), fs, Ext2Directory.this.entry );
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
