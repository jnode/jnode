/*
 * $Id$
 */
package org.jnode.fs.fat;

import java.io.IOException;

import org.jnode.fs.FSAccessRights;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;

/**
 * @author epr
 */
public class FatRootEntry extends FatObject implements FSEntry {
	
	/** The actual root directory */
	private final FatRootDirectory rootDir;
	
	public FatRootEntry(FatRootDirectory rootDir) {
		super(rootDir.getFatFileSystem());
		this.rootDir = rootDir;
	}

	/**
	 * Gets the name of this entry.
	 */
	public String getName() {
		return "";
	}
	
	/**
	 * Gets the directory this entry is a part of.
	 */
	public FSDirectory getParent() {
		return null;
	}
	
	/**
	 * Gets the last modification time of this entry.
	 * @throws IOException
	 */

	public long getLastModified()
	throws IOException {
		return System.currentTimeMillis();
	}

	/**
	 * Is this entry refering to a file?
	 */
	public boolean isFile() {
		return false;
	}

	/**
	 * Is this entry refering to a (sub-)directory?
	 */
	public boolean isDirectory() {
		return true;
	}

	/**
	 * Sets the name of this entry.
	 */
	public void setName(String newName)
	throws IOException {
		throw new IOException("Cannot change name of root directory");
	}
	
	/**
	 * Sets the last modification time of this entry.
	 * @throws IOException
	 */
	public void setLastModified(long lastModified)
	throws IOException {
	}
	
	/**
	 * Gets the file this entry refers to. This method can only be called
	 * if <code>isFile</code> returns true.
	 */
	public FSFile getFile()
	throws IOException {
		throw new IOException("Not a file");
	}

	/**
	 * Gets the directory this entry refers to. This method can only be called
	 * if <code>isDirectory</code> returns true.
	 */
	public FSDirectory getDirectory() {
		return rootDir;
	}
	
	/**
	 * Gets the accessrights for this entry.
	 * @throws IOException
	 */
	public FSAccessRights getAccessRights()
	throws IOException {
		throw new IOException("Not implemented yet");
	}
}
