/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
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
