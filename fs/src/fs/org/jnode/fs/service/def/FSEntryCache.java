/*
 * $Id$
 */
package org.jnode.fs.service.def;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.jnode.fs.FSEntry;

/**
 * @author epr
 */
final class FSEntryCache {

	/** My filesystem manager */
	//private final FileSystemManager fsm;
	/** The actual cache */
	private final HashMap entries = new HashMap();
	
	/**
	 * Create a new instance
	 * @param fsm
	 */
	public FSEntryCache(FileSystemManager fsm) {
		//this.fsm = fsm;
	}
	
	/**
	 * Gets a cached entry for a given path.
	 * @param path
	 */
	public synchronized FSEntry getEntry(File path) {
		FSEntry entry = (FSEntry)entries.get(path);
		if (entry != null) {
			if (entry.isValid()) {
				return entry; 
			} else {
				entries.remove(path);
				return null;
			}
		} else {
			return null;
		}
	}
	
	/**
	 * Puts an entry in the cache. Any existing entry for the given path
	 * will be removed.
	 * @param path
	 * @param entry
	 */
	public synchronized void setEntry(File path, FSEntry entry) {
		entries.put(path, entry);
	}
	
	/**
	 * Remove any entry bound to the given path or a path below the given
	 * path.
	 * @param rootPath
	 */
	public synchronized void removeEntries(File rootPath) {
		entries.remove(rootPath);
		final String rootPathStr = rootPath.getAbsolutePath();
		final ArrayList removePathList = new ArrayList();
		for (Iterator i = entries.keySet().iterator(); i.hasNext(); ) {
			final File path = (File)i.next();
			final String pathStr = path.getAbsolutePath();
			if (pathStr.startsWith(rootPathStr)) {
				removePathList.add(path);
			}
		}
		for (Iterator i = removePathList.iterator(); i.hasNext(); ) {
			final File path = (File)i.next();
			entries.remove(path);			
		}
	}
}
