/*
 * $Id$
 */
package org.jnode.fs;

import java.io.IOException;

/**
 * @author epr
 */
public interface FSDirectory extends FSObject {
	
	/**
	 * Gets an iterator used to iterate over all the entries of this 
	 * directory.
	 * All elements returned by the iterator must be instanceof FSEntry.
	 */
	public FSEntryIterator iterator()
	throws IOException;
	
	/**
	 * Gets the entry with the given name.
	 * @param name
	 * @throws IOException
	 */
	public FSEntry getEntry(String name)
	throws IOException;
	
	/**
	 * Add a new file with a given name to this directory.
	 * @param name
	 * @throws IOException
	 */
	public FSEntry addFile(String name)
	throws IOException; 

	/**
	 * Add a new (sub-)directory with a given name to this directory.
	 * @param name
	 * @throws IOException
	 */
	public FSEntry addDirectory(String name)
	throws IOException;
	
	/**
	 * Remove the entry with the given name from this directory.
	 * @param name
	 * @throws IOException
	 */
	public void remove(String name)
	throws IOException; 
	
	/**
	 * Save all dirty (unsaved) data to the device 
	 * @throws IOException
	 */
	public void flush() throws IOException;

}
