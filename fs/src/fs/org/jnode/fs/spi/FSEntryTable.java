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
 
package org.jnode.fs.spi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSEntryIterator;

/**
 * A table containing all the entries of a directory.
 * 
 * This class and its childs have the responsability to identify
 * an entry by its name (case sensitivity, long file name, ...).
 * 
 * The class can limit the number of entries (for root directories ...)
 * if necessary.
 *   
 * @author Fabien DUMINY
 */
public class FSEntryTable extends AbstractFSObject {
	/**
	 * An empty table that's used as a default table (that can't be modified)
	 * for FSDirectory 
	 */
	static public final FSEntryTable EMPTY_TABLE = new FSEntryTable() {};

	/**
	 * Construct a FSEntryTable from a list of FSEntry 
	 * @param fs
	 * @param entryList
	 */
	public FSEntryTable(AbstractFileSystem fs, List/*<FSEntry>*/ entryList) {
		super(fs);
		// As a value may be null (a free entry) 
		// we must use HashMap and not Hashtable
		this.entries = new HashMap();
		this.entryNames = new ArrayList();
		
		int nbEntries = entryList.size();
		for(int i = 0 ; i < nbEntries ; i++)
		{
			FSEntry entry = (FSEntry) entryList.get(i);
			if(entry == null)
			{
				entries.put(null, null);
				entryNames.add(null);				
			}
			else
			{
				String name = normalizeName(entry.getName());
				log.debug("FSEntryTable: adding entry "+name+" (length=+"+name.length()+")");
				entries.put(name, entry);
				entryNames.add(name);
			}
		}
	}
	
	/**
	 * Iterator that returns all used entries (ie entries that aren't null)
	 * 
	 * @return
	 */
	final public FSEntryIterator iterator()
	{
		return new FSEntryIterator()
		{			
			private int index = 0;
			private List usedEntries = getUsedEntries();
	
			public boolean hasNext() {
				return index < usedEntries.size();
			}
	
			public FSEntry next() {
				FSEntry entry = (FSEntry) usedEntries.get(index);
				index++;
				
				return entry;
			}
		};
	}
	
	/**
	 * Find a free entry in the table and set it with newEntry.
	 * If the table is too small, it is resized.
	 * If the table can't be resized, an IOException is thrown 
	 * @param newEntry
	 * @return
	 * @throws IOException if directory is full (can't be resized) 
	 */
	public int setFreeEntry(FSEntry newEntry) throws IOException
	{
		String name = normalizeName(newEntry.getName());
		int index = findFreeEntry(newEntry);
		if(index < 0)
		{
			log.debug("setFreeEntry: ERROR: entry table is full");
			throw new IOException("Directory is full");			
		}
		
		/*Object oldN =*/ entryNames.set(index, name);
		/*Object oldE =*/ entries.put(name, newEntry);
		
		// entry added, so need to be flushed later
		setDirty();
		return index;
	}
	
	/**
	 * Find the index of free entry. If not found, resize the table is possible.
	 * If resize is impossible, an IOException is thrown. 
	 * 
	 * @param entry
	 * @return
	 */
	protected int findFreeEntry(FSEntry entry)
	{
		int size = entryNames.size();
		int freeIndex = -1;
		for (int i = 0; i < size; i++) {
			String n = (String) entryNames.get(i);
			if (n == null) {
				freeIndex = i;
			}
		}

		if(freeIndex < 0)
		{
			freeIndex = addEntry(null);
		}
		
		return freeIndex;
	}
	
	protected int addEntry(FSEntry entry)
	{
		// grow the entry table
		entryNames.add(entry);		
		return entryNames.size() - 1;
	}

	/**
	 * Get the entry given by its name. The result can be null.
	 * 
	 * @param name
	 * @return
	 */
	public FSEntry get(String name) {
		// name can't be null (it's reserved for free entries)
		if(name == null) return null;
		
		name = normalizeName(name);
		log.debug("get("+name+")");		
		return (FSEntry) entries.get(name);		
	}
	
	/**
	 * Get the entry given by its index. The result can be null.
	 * 
	 * @param index
	 * @return
	 */
	final public FSEntry get(int index)
	{
		return get((String) entryNames.get(index));
	}
	
	/**
	 * Get the actual size of the table : the number of free entries +
	 * the number of used entries.
	 * @return
	 */
	final public int size()
	{
		return entryNames.size();
	}
	
	/**
	 * Get the index of an entry given byt its name.
	 * If there is no entry with this name, return -1.
	 * 
	 * @param name
	 * @return
	 */
	protected int indexOfEntry(String name)
	{
		return entryNames.indexOf(normalizeName(name));
	}

	/**
	 * Remove an entry given by its name
	 * @param name
	 * @return
	 */
	public int remove(String name) {
		name = normalizeName(name);
		int index = entryNames.indexOf(name);
		if(index < 0)
			return -1;
		
		// in entries and entryNames, a free (deleted) entry
		// is represented by null
		entries.put(name, null);
		entryNames.set(index, null);
		
		return index;
	}

	/**
	 * Return a list of FSEntry representing the content of the table.
	 * @return
	 */
	public List/*<FSEntry>*/ toList()
	{
		//false means not compacted (ie can contain some null entries)
		return toList(false);
	}

	/**
	 * Return a list of FSEntry representing the content of the table.
	 * The table can be compacted (ie: without null entries) or
	 * uncompacted (with null entries if there are).
	 * @param compacted
	 * @return
	 */
	public List/*<FSEntry>*/ toList(boolean compacted)
	{
		ArrayList entryList = new ArrayList();
		
		int nbEntries = entryNames.size();		
		for(int i = 0 ; i < nbEntries ; i++)
		{
			FSEntry entry = get(i);
			if(!compacted || (compacted && (entry != null)))
					entryList.add(entry);
		}
		
		return entryList;
	}
	
	/**
	 * Rename an entry given by its oldName.
	 * 
	 * @param oldName
	 * @param newName
	 * @return
	 */
	public int rename(String oldName, String newName)
	{
		log.debug("<<< BEGIN rename oldName="+oldName+" newName="+newName+" >>>");
		log.debug("rename: table="+toString());
		oldName = normalizeName(oldName);
		newName = normalizeName(newName);
		log.debug("rename oldName="+oldName+" newName="+newName);
		if(!entryNames.contains(oldName))
		{
			log.debug("<<< END rename return false (oldName not found) >>>");		
			return -1;
		}
		
		int index = entryNames.indexOf(oldName);
		if(index < 0)
			return -1;
		
		entryNames.set(index, newName);
		
		FSEntry entry = (FSEntry) entries.remove(oldName);
		entries.put(newName, entry);		
		
		log.debug("<<< END rename return true >>>");		
		return index;
	}

	/**
	 * return a list of used FSEntry
	 * @return
	 */
	protected List getUsedEntries()
	{
		int nbEntries = entryNames.size();
		ArrayList used = new ArrayList(nbEntries / 2);
		
		for(int i = 0 ; i < nbEntries ; i++)
		{
			String name = (String) entryNames.get(i); 
			if(name != null)
			{
				used.add(entries.get(name));
			}
		}
		
		return used;
	}
	
	/**
	 * return a list of FSEntry names (ie a list of String) 
	 * @return
	 */
	protected List getEntryNames()
	{
		return entryNames;
	}
	
	/**
	 * Return a normalized entry name (for case insensitivity for example) 
	 * @param name
	 * @return
	 */
	protected String normalizeName(String name)
	{
		return name;
	}

	/**
	 * Indicate if the table need to be saved to the device.
	 */
	final public boolean isDirty() throws IOException
	{
		if(super.isDirty())
			return true;
		
		//int nbEntries = entries.size();
		for (Iterator it = entries.values().iterator() ; it.hasNext() ; ) {
			FSEntry entry = (FSEntry) it.next();
			if (entry != null) {
				if (entry.isDirty()) {
					return true;
				}
			}
		}		
		
		return false;
	}
	
	/**
	 * Return a string representation of the table. 
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		for(int i = 0 ; i < entryNames.size() ; i++)
		{
			sb.append("name:").append(entryNames.get(i));
			sb.append("->entry:").append(entries.get(entryNames.get(i)));
			sb.append("\n");
		}
		
		return sb.toString();
	}
	
	/**
	 * Private constuctor for EMPTY_TABLE
	 *
	 */
	private FSEntryTable()
	{
		super();
		entries = Collections.EMPTY_MAP;
		entryNames = Collections.EMPTY_LIST;
	}
	
	/**
	 * Map of entries (key=name, value=entry).
	 * As a value may be null (a free entry) we must use Hashtable and not Hashtable 
	 */
	private Map entries; // must be a HashMap
	
	/**
	 * Names of the entries (list of String or null) 
	 */
	private List entryNames;
	
	private static final Logger log = Logger.getLogger(FSEntryTable.class);	
}
