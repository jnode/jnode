/**
 * $Id$
 */
package org.jnode.fs.spi;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.jnode.fs.FSAccessRights;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSEntryTable;
import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystem;
import org.jnode.fs.util.FSUtils;

/**
 * An abstract implementation of FSEntry that contains common things
 * among many FileSystems
 * @author Fabien DUMINY
 */
public abstract class AbstractFSEntry extends AbstractFSObject implements FSEntry {
	// common types of entries found in most FileSystems 
	public static final int FIRST_ENTRY = -2; // fake entry: lower bound
	public static final int OTHER_ENTRY = -1; // other entry
	public static final int DIR_ENTRY   =  0; // directory entry
	public static final int FILE_ENTRY  =  1; // file entry
	public static final int ROOT_ENTRY  =  2; // root entry
	public static final int LAST_ENTRY  =  3; // fake entry: upper bound

	/**
	 * Constructor for a root entry
	 * 
	 * @param fs
	 */
	public AbstractFSEntry(AbstractFileSystem fs) {
		// parent and table are null for a root
		this(fs, null, null, "/", ROOT_ENTRY);
	}

	/**
	 * Constructor for a non-root entry
	 * @param fs
	 * @param table
	 * @param parent
	 * @param name
	 * @param type
	 */
	public AbstractFSEntry(AbstractFileSystem fs, FSEntryTable table, FSDirectory parent, String name, int type) {		
		super(fs);
		if((type <= FIRST_ENTRY) || (type >= LAST_ENTRY))
			throw new IllegalArgumentException("type must be DIR_ENTRY, FILE_ENTRY, ROOT_ENTRY or OTHER_ENTRY");

		this.type = type; 
		this.name = name;
		this.table = table;
		this.lastModified = 0;
		this.parent = parent;
		//TODO : this.rights = ???????;
	}

	/**
	 * Return the name of this entry
	 */
	final public String getName() {
		return name;
	}
	
	/**
	 * Return the parent directory (if any) of this entry
	 */
	final public FSDirectory getParent() {
		return parent;
	}
	
	/**
	 * Return the date of the last modification of this entry
	 */
	final public long getLastModified() {
		return lastModified;
	}
	
	/**
	 * Indicate if this entry is a file
	 */
	final public boolean isFile() {
		return treatDirectoryAsFile || (type == FILE_ENTRY);
	}
	
	/**
	 * Indicate if this entry is a directory
	 */
	final public boolean isDirectory() {
		return (type == DIR_ENTRY) || isRoot();
	}

	/**
	 * Indicate if this entry is a root-directory
	 * @return
	 */
	final public boolean isRoot() {
		return (type == ROOT_ENTRY) ;
	}

	/**
	 * Change the name of this entry
	 */
	final public void setName(String newName) throws IOException {
		log.debug("<<< BEGIN setName newName="+newName+" >>>");
		// NB: table is null for a root
		if(isRoot())
		{
			log.debug("<<< END setName newName="+newName+" ERROR: root >>>");
			throw new IOException("Cannot change name of root directory");
		}
	
		// It's not a root --> table != null 
		if(table.rename(name, newName) < 0)
		{
			log.debug("<<< END setName newName="+newName+" ERROR: table can't rename >>>");
			throw new IOException("Cannot change name");
		}
		
		this.name = newName;
		log.debug("<<< END setName newName="+newName+" >>>");
	}
	
	/**
	 * Change the date of the last modification of this entry 
	 */
	final public void setLastModified(long lastModified) throws IOException {
/*		if(isRoot())
		{
			throw new IOException("Cannot change last modified of root directory");
		}
*/	
		this.lastModified = lastModified;
	}
	
	/**
	 * Return the file associated with this entry
	 */
	final public FSFile getFile() throws IOException {
		if(!isFile())
			throw new IOException(getName()+" is not a file");
		
		return ((AbstractFileSystem) getFileSystem()).getFile(this);
	}
	
	/**
	 * Return the directory associated with this entry
	 */
	final public FSDirectory getDirectory() throws IOException {
		if(!isDirectory())
			throw new IOException(getName()+" is not a directory");
		
		return ((AbstractFileSystem) getFileSystem()).getDirectory(this);
	}
	
	/**
	 * Return the access rights for this entry
	 */
	final public FSAccessRights getAccessRights() throws IOException {
		return rights;
	}
	
	/**
	 * Should we treat this directory entry as a file entry ? 
	 *
	 */
	final protected void setTreatDirectoryAsFile()
	{
		this.treatDirectoryAsFile = true;
	}
	
	public String toString()
	{
		return FSUtils.toString(this, false);
	}
	
	/**
	 * Type of entry
	 */
	private int type;
	
	/**
	 * name of the entry
	 */
	private String name;
	
	/**
	 * Date of last modification of the entry
	 */
	private long lastModified;
	
	/**
	 * access rights of the entry
	 */
	private FSAccessRights rights;
	
	/**
	 * Parent directory of the entry
	 */
	private FSDirectory parent; // parent is null for a root
	
	/**
	 * Table of entries of our parent
	 */
	private FSEntryTable table; // table is null for a root
	
	/**
	 * should we treat this directory entry as a file entry ?
	 */
	private boolean treatDirectoryAsFile = false;
	
	private static final Logger log = Logger.getLogger(AbstractFSEntry.class);	
}
