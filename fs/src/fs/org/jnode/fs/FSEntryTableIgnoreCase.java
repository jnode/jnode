/**
 * $Id$
 */
package org.jnode.fs;

import java.util.List;

import org.jnode.fs.spi.*;
import org.jnode.fs.spi.AbstractFileSystem;

/**
 * A child class of FSEntryTable that ignore the case of entry names.
 *  
 * @author Fabien DUMINY
 */
public class FSEntryTableIgnoreCase extends FSEntryTable {
	/**
	 * Construct a FSEntryTableIgnoreCase from a list of FSEntry 
	 * @param fs
	 * @param entryList
	 */
	public FSEntryTableIgnoreCase(AbstractFileSystem fs, List/*<FSEntry>*/ entryList) {
		super(fs, entryList);
	}
	
	/**
	 * To Ignore case, we convert all entry names to upper case
	 */
	protected String normalizeName(String name)
	{
		if(name == null)
			return null;
		
		return name.toUpperCase();
	}
}
