/**
 * $Id$
 */
package org.jnode.fs.fat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;

/**
 * @author epr
 */
public abstract class AbstractDirectory
	extends FatObject
	implements FSDirectory {

	protected FatDirEntry[] entries;
	private boolean _dirty;
	private final FatFile myFile;

	public AbstractDirectory(FatFileSystem fs, int nrEntries, FatFile myFile) {
		super(fs);
		entries = new FatDirEntry[nrEntries];
		_dirty = false;
		this.myFile = myFile;
	}

	/**
	 * Gets an iterator to iterate over all entries. The iterated objects
	 * are all instance DirEntry.
	 * @return Iterator
	 */
	public Iterator iterator() {
		return new DirIterator();
	}

	/**
	 * Add a directory entry.
	 * @param nameExt
	 * @throws IOException
	 */
	public synchronized FatDirEntry addFatFile(String nameExt)
		throws IOException {
		if (getFatEntry(nameExt) != null) {
			throw new IOException("File already exists" + nameExt);
		}
		final FatDirEntry newEntry =
			new FatDirEntry(this, splitName(nameExt), splitExt(nameExt));
		for (int i = 0; i < entries.length; i++) {
			FatDirEntry e = entries[i];
			if (e == null) {
				entries[i] = newEntry;
				setDirty();
				flush();
				return newEntry;
			}
		}
		int newSize = entries.length + 512 / 32;
		if (canChangeSize(newSize)) {
			FatDirEntry[] newEntries = new FatDirEntry[newSize];
			int idx = entries.length;
			System.arraycopy(entries, 0, newEntries, 0, entries.length);
			entries = newEntries;
			entries[idx] = newEntry;
			setDirty();
			flush();
			return newEntry;
		}
		throw new IOException("Directory is full (" + entries.length + ")");
	}

	/**
	 * Add a new file with a given name to this directory.
	 * @param name
	 * @throws IOException
	 */
	public FSEntry addFile(String name) throws IOException {
		return addFatFile(name);
	}

	/**
	 * Add a directory entry of the type directory.
	 * @param nameExt
	 * @param parentCluster
	 * @throws IOException
	 */
	public synchronized FatDirEntry addFatDirectory(
		String nameExt,
		long parentCluster)
		throws IOException {
		final FatDirEntry entry = addFatFile(nameExt);
		final int clusterSize = getFatFileSystem().getClusterSize();
		entry.setFlags(FatConstants.F_DIRECTORY);
		final FatFile file = entry.getFatFile();
		file.setLength(clusterSize);
		final byte[] buf = new byte[clusterSize];
		// Clean the contents of this cluster to avoid reading strange data
		// in the directory.
		file.write(0, buf, 0, buf.length);
		file.getDirectory().initialize(file.getStartCluster(), parentCluster);
		flush();
		return entry;
	}

	/**
	 * Add a new (sub-)directory with a given name to this directory.
	 * @param name
	 * @throws IOException
	 */
	public FSEntry addDirectory(String name) throws IOException {
		final long parentCluster;
		if (myFile == null) {
			parentCluster = 0;
		} else {
			parentCluster = myFile.getStartCluster();
		}
		return addFatDirectory(name, parentCluster);
	}

	/**
	 * Gets the number of directory entries in this directory
	 * @return int
	 */
	public int getSize() {
		return entries.length;
	}

	/**
	 * Search for an entry with a given name.ext
	 * @param nameExt
	 * @return FatDirEntry null == not found
	 */
	public FatDirEntry getFatEntry(String nameExt) {

		final String name = splitName(nameExt);
		final String ext = splitExt(nameExt);

		for (int i = 0; i < entries.length; i++) {
			final FatDirEntry entry = entries[i];
			if (entry != null) {
				if (name.equalsIgnoreCase(entry.getNameOnly())
					&& ext.equalsIgnoreCase(entry.getExt())) {
					return entry;
				}
			}
		}
		return null;
	}

	/**
	 * Gets the entry with the given name.
	 * @param name
	 * @throws IOException
	 */
	public FSEntry getEntry(String name) throws IOException {
		final FatDirEntry entry = getFatEntry(name);
		if (entry == null) {
			throw new FileNotFoundException(name);
		} else {
			return entry;
		}
	}

	/**
	 * Remove a file or directory with a given name
	 * @param nameExt
	 */
	public synchronized void remove(String nameExt) throws IOException {
		FatDirEntry entry = getFatEntry(nameExt);
		if (entry == null) {
			throw new FileNotFoundException(nameExt);
		}
		for (int i = 0; i < entries.length; i++) {
			if (entries[i] == entry) {
				entries[i] = null;
				setDirty();
				flush();
				return;
			}
		}
	}

	/**
	 * Print the contents of this directory to the given writer. Used for
	 * debugging purposes.
	 * @param out
	 */
	public void printTo(PrintWriter out) {
		int freeCount = 0;
		for (int i = 0; i < entries.length; i++) {
			FatDirEntry entry = entries[i];
			if (entry != null) {
				out.println("0x" + Integer.toHexString(i) + " " + entries[i]);
			} else {
				freeCount++;
			}
		}
		out.println("Unused entries " + freeCount);
	}

	class DirIterator implements Iterator {

		private int offset = 0;

		/**
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			while (offset < entries.length) {
				FatDirEntry e = entries[offset];
				if ((e != null) && !e.isDeleted()) {
					return true;
				} else {
					offset++;
				}
			}
			return false;
		}

		/**
		 * @see java.util.Iterator#next()
		 */
		public Object next() {
			while (offset < entries.length) {
				FatDirEntry e = entries[offset];
				if ((e != null) && !e.isDeleted()) {
					offset++;
					return e;
				} else {
					offset++;
				}
			}
			throw new NoSuchElementException();
		}

		/**
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			throw new UnsupportedOperationException("remove");
		}

	}

	/**
	 * Returns the dirty.
	 * @return boolean
	 */
	public boolean isDirty() {
		if (_dirty) {
			return true;
		}
		for (int i = 0; i < entries.length; i++) {
			FatDirEntry entry = entries[i];
			if (entry != null) {
				if (entry.isDirty()) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Mark this directory as dirty.
	 */
	protected final void setDirty() {
		this._dirty = true;
	}

	/**
	 * Mark this directory as not dirty.
	 */
	protected final void resetDirty() {
		this._dirty = false;
	}

	/**
	 * Can this directory change size of <code>newSize</code> directory entries?
	 * @param newSize
	 * @return boolean
	 */
	protected abstract boolean canChangeSize(int newSize);

	private String splitName(String nameExt) {
		int i = nameExt.indexOf('.');
		if (i < 0) {
			return nameExt;
		} else {
			return nameExt.substring(0, i);
		}
	}

	private String splitExt(String nameExt) {
		int i = nameExt.indexOf('.');
		if (i < 0) {
			return "";
		} else {
			return nameExt.substring(i + 1);
		}
	}

	/**
	 * Sets the first two entries '.' and '..' in the directory
	 * @param parentCluster
	 */
	protected void initialize(long myCluster, long parentCluster) {
		FatDirEntry e = entries[0] = new FatDirEntry(this, ".", "");
		e.setFlags(FatConstants.F_DIRECTORY);
		e.setStartCluster((int)myCluster);

		e = entries[1] = new FatDirEntry(this, "..", "");
		e.setFlags(FatConstants.F_DIRECTORY);
		e.setStartCluster((int)parentCluster);
	}

	/** 
	 * Flush the contents of this directory to the persistent storage 
	 */
	protected abstract void flush() throws IOException;

	/** 
	 * Read the contents of this directory from the given byte array
	 * @param src
	 */
	protected synchronized void read(byte[] src) throws IOException {
		for (int i = 0; i < entries.length; i++) {
			if (src[i * 32] != 0) {
				FatDirEntry entry = new FatDirEntry(this, src, i * 32);
				entries[i] = entry;
			} else {
				break;
			}
		}
	}

	/** 
	 * Write the contents of this directory to the given device at the given
	 * offset.
	 * @param dest
	 */
	protected synchronized void write(byte[] dest) throws IOException {
		for (int i = 0; i < entries.length; i++) {
			FatDirEntry entry = entries[i];
			if (entry != null) {
				entry.write(dest, i * 32);
			} else {
				break;
			}
		}
	}
}
