/*
 * $Id$
 */
package org.jnode.fs.ntfs;

import java.io.IOException;
import java.util.Iterator;

import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSEntryIterator;
import org.jnode.fs.FileSystem;
import org.jnode.fs.ntfs.attributes.NTFSIndexEntry;

/**
 * @author vali
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class NTFSDirectory implements FSDirectory {

	/**
	 * 
	 */
	NTFSIndex index = null;

	/**
	 * 
	 * @param record
	 */
	public NTFSDirectory(NTFSFileRecord record) {
		this.index = new NTFSIndex(record);
	}

	/**
	 * 
	 */
	public FSEntryIterator iterator() {
		return new FSEntryIterator() {

			Iterator it = index.iterator();

			public boolean hasNext() {
				return it.hasNext();
			}

			public FSEntry next() {
				return new NTFSEntry((NTFSIndexEntry)it.next());
			}
		};
	}

	/**
	 * 
	 */
	public FSEntry getEntry(String name) {
		for (FSEntryIterator it = this.iterator(); it.hasNext();) {
			NTFSEntry entry = (NTFSEntry)it.next();
			if (entry.getName().equals(name))
				return entry;
		}

		return null;
	}

	/**
	 * 
	 */
	public FSEntry addFile(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 
	 */
	public FSEntry addDirectory(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 
	 */
	public void remove(String name) {
		// TODO Auto-generated method stub

	}

	/**
	 * 
	 */
	public boolean isValid() {
		return true;
	}

	/**
	 * 
	 */
	public FileSystem getFileSystem() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Save all dirty (unsaved) data to the device 
	 * @throws IOException
	 */
	public void flush() throws IOException
	{
		//TODO
	}
}
