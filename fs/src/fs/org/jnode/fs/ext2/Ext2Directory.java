/*
 * $Id$
 */
package org.jnode.fs.ext2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.spi.AbstractFSDirectory;
import org.jnode.fs.spi.AbstractFileSystem;
import org.jnode.fs.spi.FSEntryTable;
import org.jnode.fs.util.FSUtils;

/**
 * @author Andras Nagy
 */
public class Ext2Directory extends AbstractFSDirectory {
	
	INode iNode;
	private Ext2Entry entry;		
	private final Logger log = Logger.getLogger(getClass());
			
	/**
	 * @param entry	the Ext2Entry representing this directory
	 */
	public Ext2Directory(Ext2Entry entry) throws IOException {
		super((Ext2FileSystem) entry.getFileSystem());
		this.iNode = entry.getINode();
		Ext2FileSystem fs = (Ext2FileSystem) entry.getFileSystem();		
		this.entry = entry;
		log.setLevel(Level.DEBUG);
		boolean readOnly;
		if((iNode.getFlags() & Ext2Constants.EXT2_INDEX_FL)== 1)
			readOnly = true;		//force readonly
		else
			readOnly = fs.isReadOnly();
		setRights(true, !readOnly);
		
		log.debug("directory size: "+iNode.getSize());		
	}
	
	/**
	 * Method to create a new ext2 directory entry from the given name
	 * @param name
	 * @return
	 * @throws IOException
	 */
	public FSEntry createDirectoryEntry(String name) throws IOException {
		if(!canWrite())
			throw new IOException("Filesystem or directory is mounted read-only!");

		//create a new iNode for the file
		//TODO: access rights, file type, UID and GID should be passed through the FSDirectory interface
		INode newINode;
		Ext2DirectoryRecord dr;
		Ext2Entry newEntry;
		Ext2FileSystem fs = (Ext2FileSystem) getFileSystem(); 
		try{
			int rights = 0xFFFF & (Ext2Constants.EXT2_S_IRWXU | Ext2Constants.EXT2_S_IRWXG | Ext2Constants.EXT2_S_IRWXO);
			newINode = fs.createINode((int)iNode.getGroup(), Ext2Constants.EXT2_S_IFDIR, rights, 0, 0);
			
			dr = new Ext2DirectoryRecord(fs, newINode.getINodeNr(), Ext2Constants.EXT2_FT_DIR, name);

			addDirectoryRecord(dr);
			
			newINode.setLinksCount( newINode.getLinksCount()+1 );

			newEntry = new Ext2Entry(newINode, name, Ext2Constants.EXT2_FT_DIR, fs, this);
			
			//add "."
			
			Ext2Directory newDir = new Ext2Directory(newEntry);
			Ext2DirectoryRecord drThis = new Ext2DirectoryRecord( fs, newINode.getINodeNr(), Ext2Constants.EXT2_FT_DIR, "." );
			newDir.addDirectoryRecord( drThis );
			newINode.setLinksCount( 2 );
						
			//add ".."
			long parentINodeNr = ((Ext2Directory)entry.getDirectory()).getINode().getINodeNr();
			Ext2DirectoryRecord drParent=new Ext2DirectoryRecord( fs, parentINodeNr, Ext2Constants.EXT2_FT_DIR, ".." );
			newDir.addDirectoryRecord( drParent );
			
			//increase the reference count for the parent directory
			INode parentINode = fs.getINode((int)parentINodeNr);
			parentINode.setLinksCount( parentINode.getLinksCount()+1 ); 
			
			//update the number of used directories in the block group
			int group = (int)( (newINode.getINodeNr()-1) / fs.getSuperblock().getINodesPerGroup()) ;
			fs.modifyUsedDirsCount(group, 1);
			
			//update both affected directory inodes
			iNode.update();
			newINode.update();
		}catch(FileSystemException fse) {
			throw new IOException(fse);
		}
		
		return newEntry;
	}

	/**
	 * Abstract method to create a new ext2 file entry from the given name
	 * @param name
	 * @return
	 * @throws IOException
	 */
	public FSEntry createFileEntry(String name) throws IOException {
		if(!canWrite())
			throw new IOException("Filesystem or directory is mounted read-only!");

		//create a new iNode for the file
		//TODO: access rights, file type, UID and GID should be passed through the FSDirectory interface
		INode newINode;
		Ext2DirectoryRecord dr;
		Ext2FileSystem fs = (Ext2FileSystem) getFileSystem();		
		try{
			int rights = 0xFFFF & (Ext2Constants.EXT2_S_IRWXU | Ext2Constants.EXT2_S_IRWXG | Ext2Constants.EXT2_S_IRWXO);
			newINode = fs.createINode((int)iNode.getGroup(), Ext2Constants.EXT2_S_IFREG, rights, 0, 0);
			
			dr = new Ext2DirectoryRecord(fs, newINode.getINodeNr(), Ext2Constants.EXT2_FT_REG_FILE, name);

			addDirectoryRecord(dr);
			
			newINode.setLinksCount( newINode.getLinksCount()+1 );

			// update the directory inode
			iNode.update();
		}catch(FileSystemException fse) {
			throw new IOException(fse);
		}
		return new Ext2Entry(newINode, name, Ext2Constants.EXT2_FT_REG_FILE, fs, this); 
	}
	
	/**
	 * Attach an inode to a directory (not used normally, only during fs creation)
	 * @param iNodeNr
	 * @return
	 * @throws IOException
	 */
	protected FSEntry addINode(int iNodeNr, String linkName, int fileType) throws IOException {
		if(!canWrite())
			throw new IOException("Filesystem or directory is mounted read-only!");
			
		//TODO: access rights, file type, UID and GID should be passed through the FSDirectory interface
		Ext2DirectoryRecord dr;
		Ext2FileSystem fs = (Ext2FileSystem) getFileSystem();		
		try{
			dr = new Ext2DirectoryRecord(fs, iNodeNr, fileType, linkName);
			addDirectoryRecord(dr);

			// update the directory inode
			iNode.update();		
			
			INode linkedINode = fs.getINode(iNodeNr);
			
			linkedINode.setLinksCount( linkedINode.getLinksCount()+1 );
			
			return new Ext2Entry(linkedINode, linkName, fileType, fs, this); 
		
		}catch(FileSystemException fse) {
			throw new IOException(fse);
		}
	}
	
	private void addDirectoryRecord(Ext2DirectoryRecord dr) throws IOException, FileSystemException{
		Ext2File dir = new Ext2File(iNode);		//read itself as a file

		//a single inode may be represented by more than one Ext2Directory instances, 
		//but each will use the same instance of the underlying inode (see Ext2FileSystem.getINode()),
		//so synchronize to the inode
		synchronized(iNode) {	
			//find the last directory record (if any)
			Ext2FSEntryIterator iterator = new Ext2FSEntryIterator(iNode);
			Ext2DirectoryRecord rec=null;
			while(iterator.hasNext()) {
				rec = iterator.nextDirectoryRecord();
			}
			
			Ext2FileSystem fs = (Ext2FileSystem) getFileSystem();		
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
			
			//dir.flush();
			iNode.setMtime(System.currentTimeMillis()/1000);
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

	private INode getINode() {
		return iNode;
	}
	
	class Ext2FSEntryIterator implements org.jnode.fs.FSEntryIterator {
		byte data[];
		int index;
		
		Ext2DirectoryRecord current;
		
		public Ext2FSEntryIterator(INode iNode)throws IOException {
			//read itself as a file
			Ext2File directoryFile = new Ext2File(iNode);
			//read the whole directory
			data = new byte[(int)directoryFile.getLength()];
			directoryFile.read(0, data, 0, (int)directoryFile.getLength());
			index = 0;
		}
		
		public boolean hasNext() {
			Ext2DirectoryRecord dr;
			Ext2FileSystem fs = (Ext2FileSystem) getFileSystem();			
			do {
				if(index>=iNode.getSize())
					return false;
				
				dr = new Ext2DirectoryRecord(fs, data, index, index);
				index+=dr.getRecLen();	
			} while(dr.getINodeNr()==0);			//inode nr=0 means the entry is unused

			current = dr;
			return true;
		}
		
		public FSEntry next() {
			
			if(current == null) {
				//hasNext actually reads the next element
				if(!hasNext())
					throw new NoSuchElementException();
			}
			
			Ext2DirectoryRecord dr = current;
			Ext2FileSystem fs = (Ext2FileSystem) getFileSystem();			
			current = null;
			try{
				return new Ext2Entry( ((Ext2FileSystem)getFileSystem()).getINode(dr.getINodeNr()),
										dr.getName(), dr.getType(), fs, Ext2Directory.this);
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
	}

	/**
	 * Read the entries from the device and return the result in
	 * a new FSEntryTable
	 * @return 
	 */
	protected FSEntryTable readEntries() throws IOException {
		Ext2FSEntryIterator it = new Ext2FSEntryIterator(iNode);
		ArrayList entries = new ArrayList();
		
		while(it.hasNext())
		{
			FSEntry entry = it.next();
			log.debug("readEntries: entry="+FSUtils.toString(entry, false));
			entries.add(entry);
		}
		
		FSEntryTable table = new FSEntryTable((AbstractFileSystem) getFileSystem(), entries);
		
		return table;
	}

	/**
	 * Write the entries in the table to the device.
	 * @param table
	 */
	protected void writeEntries(FSEntryTable table) throws IOException {
		//nothing to do because createFileEntry and createDirectoryEntry do the job
	}
}
