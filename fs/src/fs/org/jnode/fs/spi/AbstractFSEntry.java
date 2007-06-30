/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.fs.spi;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.jnode.fs.FSAccessRights;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.jnode.fs.util.FSUtils;

/**
 * An abstract implementation of FSEntry that contains common things
 * among many FileSystems
 * @author Fabien DUMINY
 */
public abstract class AbstractFSEntry extends AbstractFSObject implements FSEntry {

	private static final Logger log = Logger.getLogger(AbstractFSEntry.class);	

	// common types of entries found in most FileSystems: 
	/** Fake entry: lower bound */
	public static final int FIRST_ENTRY = -2;
	/** Other entry */
	public static final int OTHER_ENTRY = -1;
	/** directory entry */
	public static final int DIR_ENTRY   =  0;
	/** file entry */
	public static final int FILE_ENTRY  =  1;
	/** root entry */
	public static final int ROOT_ENTRY  =  2;
	/** fake entry: upper bound */
	public static final int LAST_ENTRY  =  3;


	/** Type of entry */
	private int type;
	
	/** name of the entry */
	private String name;
	
	/** Date of last modification of the entry */
	private long lastModified;
	
	/** access rights of the entry */
	final private FSAccessRights rights;
	
	/** Parent directory of the entry */
	private FSDirectory parent; // parent is null for a root
	
	/** Table of entries of our parent */
	private FSEntryTable table; // table is null for a root
	
	/** should we treat this directory entry as a file entry ? */
	private boolean treatDirectoryAsFile = false;

	
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
		this.lastModified = System.currentTimeMillis();
		this.parent = parent;
		this.rights = new UnixFSAccessRights(fs);
	}

	/**
	 * Return the name of this entry
	 * @return the name of this entry
	 */
	public final String getName() {
		return name;
	}
	
	/**
	 * Return the parent directory (if any) of this entry
	 * @return the parent directory of this entry
	 */
	public final FSDirectory getParent() {
		return parent;
	}
	
	/**
	 * Return the date of the last modification of this entry
	 * @return the date of the last modification
	 * @throws IOException 
	 */
	public long getLastModified() throws IOException {
		return lastModified;
	}
	
	/**
	 * Indicate if this entry is a file
	 * @return if this entry denotes a file
	 */
	public final boolean isFile() {
		return treatDirectoryAsFile || (type == FILE_ENTRY);
	}
	
	/**
	 * Indicate if this entry is a directory
	 * @return is this entry denotes a directory
	 */
	public final boolean isDirectory() {
		return (type == DIR_ENTRY) || isRoot();
	}

	/**
	 * Indicate if this entry is a root-directory
	 * @return if this entry is the root directory
	 */
	public final boolean isRoot() {
		return (type == ROOT_ENTRY) ;
	}

	/**
	 * Change the name of this entry
	 * @param newName 
	 * @throws IOException 
	 */
	public final void setName(String newName) throws IOException {
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
	 * @param lastModified 
	 * @throws IOException 
	 */
	public final void setLastModified(long lastModified) throws IOException {
		/*
		if(isRoot()) {
			throw new IOException("Cannot change last modified of root directory");
		}
		 */	
		this.lastModified = lastModified;
	}
	
	/**
	 * Return the file associated with this entry
	 * @return the FSFile associated with this entry
	 * @throws IOException 
	 */
	public final FSFile getFile() throws IOException {
		if(!isFile())
			throw new IOException(getName()+" is not a file");
		
		return ((AbstractFileSystem) getFileSystem()).getFile(this);
	}
	
	/**
	 * Return the directory associated with this entry
	 * @return the directory associated with this entry
	 * @throws IOException 
	 */
	public final FSDirectory getDirectory() throws IOException {
		if(!isDirectory())
			throw new IOException(getName()+" is not a directory");
		
		return ((AbstractFileSystem) getFileSystem()).getDirectory(this);
	}
	
	/**
	 * Return the access rights for this entry
	 * @return the FSAccessRights for this entry
	 * @throws IOException 
	 */
	public final FSAccessRights getAccessRights() throws IOException {
		return rights;
	}
	
	/**
	 * Should we treat this directory entry as a file entry ? 
	 *
	 */
	protected final void setTreatDirectoryAsFile() {
		this.treatDirectoryAsFile = true;
	}
	
	/**
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return FSUtils.toString(this, false);
	}
}
