/*
 * $Id$
 */
package org.jnode.fs;

import java.io.IOException;

/**
 * @author epr
 */
/**
 * Entry of an FSDirectory.
 * @author epr
 */
public interface FSEntry extends FSObject {
	
	/**
	 * Gets the name of this entry.
	 */
	public String getName();
	
	/**
	 * Gets the directory this entry is a part of.
	 */
	public FSDirectory getParent();
	
	/**
	 * Gets the last modification time of this entry.
	 * @throws IOException
	 */

	public long getLastModified()
	throws IOException;

	/**
	 * Is this entry refering to a file?
	 */
	public boolean isFile();

	/**
	 * Is this entry refering to a (sub-)directory?
	 */
	public boolean isDirectory();

	/**
	 * Sets the name of this entry.
	 */
	public void setName(String newName)
	throws IOException;
	
	/**
	 * Gets the last modification time of this entry.
	 * @throws IOException
	 */
	public void setLastModified(long lastModified)
	throws IOException;
	
	/**
	 * Gets the file this entry refers to. This method can only be called
	 * if <code>isFile</code> returns true.
	 *  
	 * @return The file described by this entry
	 */
	public FSFile getFile()
	throws IOException;

	/**
	 * Gets the directory this entry refers to. This method can only be called
	 * if <code>isDirectory</code> returns true.
	 *  
	 * @return The directory described by this entry
	 */
	public FSDirectory getDirectory()
	throws IOException;
	
	/**
	 * Gets the accessrights for this entry.
	 * @throws IOException
	 */
	public FSAccessRights getAccessRights()
	throws IOException;
}