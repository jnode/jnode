/*
 * $Id$
 */
package org.jnode.fs.service.def;

import java.io.File;
import java.io.VMFileHandle;
import java.io.FileNotFoundException;
import java.io.VMFileSystemAPI;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.VMOpenMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FileSystem;

/**
 * @author epr
 */
public class FileSystemAPIImpl implements VMFileSystemAPI {

	/** My logger */
	private final Logger log = Logger.getLogger(getClass());
	/** My filesystem manager */
	private final FileSystemManager fsm;
	private final FSEntryCache entryCache;
	private final FileHandleManager fhm;

	/**
	 * Create a new instance
	 * @param fsm
	 */
	public FileSystemAPIImpl(FileSystemManager fsm) {
		this.fsm = fsm;
		this.entryCache = new FSEntryCache(fsm);
		this.fhm = new FileHandleManager();
	}

	/** 
	 * Does the given file exist?
	 */
	public boolean fileExists(File file) {
		final FSEntry entry = getEntry(file);
		return (entry != null);
	}

	/** 
	 * Is the given File a plain file?
	 */
	public boolean isFile(File file) {
		final FSEntry entry = getEntry(file);
		return (entry != null) && (entry.isFile());
	}

	/** 
	 * Is the given File a directory?
	 */
	public boolean isDirectory(File file) {
		final FSEntry entry = getEntry(file);
		return (entry != null) && (entry.isDirectory());
	}

	/**
	 * Can the given file be read?
	 * @param file
	 */
	public boolean canRead(File file) {
		// TODO implement me
		return true;
	}

	/**
	 * Can the given file be written to?
	 * @param file
	 */
	public boolean canWrite(File file) {
		// TODO implement me
		return false;
	}

	/**
	 * Gets the length in bytes of the given file or 0 if the file does not exist.
	 * @param file
	 */
	public long getLength(File file) {
		final FSEntry entry = getEntry(file);
		if (entry != null) {
			if (entry.isFile()) {
				try {
					return entry.getFile().getLength();
				} catch (IOException ex) {
					log.debug("Error in getLength", ex);
					return 0;
				}
			} else {
				log.debug("Not a file in getLength");
				return 0;
			}
		} else {
			log.debug("File not found in getLength (" + file.getAbsolutePath() + ")");
			return 0;
		}

	}

	/**
	 * Gets the last modification date of the given file.
	 * @param file
	 */
	public long getLastModified(File file) {
		final FSEntry entry = getEntry(file);
		if (entry != null) {
			try {
				return entry.getLastModified();
			} catch (IOException ex) {
				return 0;
			}
		} else {
			return 0;
		}
	}

	/**
	 * Sets the last modification date of the given file.
	 * @param file
	 */
	public void setLastModified(File file, long time) throws IOException {
		final FSEntry entry = getEntry(file);
		if (entry != null) {
			entry.setLastModified(time);
		} else {
			throw new FileNotFoundException(file.getAbsolutePath());
		}
	}

	/**
	 * Mark the given file as readonly.
	 * @param file
	 * @throws IOException
	 */
	public void setReadOnly(File file) throws IOException {
		throw new IOException("Not implemented yet");
		// TODO implement me
	}

	/**
	 * Delete the given file.
	 * @param file
	 * @throws IOException
	 */
	public void delete(File file) throws IOException {
		final File parent = file.getParentFile();
		if (parent == null) {
			throw new IOException("There is no parent of " + file);
		}
		final FSEntry parentEntry = getEntry(parent);
		if (parentEntry == null) {
			throw new IOException("Parent of " + file + " not found");
		}
		if (!parentEntry.isDirectory()) {
			throw new IOException("Parent of " + file + " is not a directory");
		}
		parentEntry.getDirectory().remove(file.getName());
		entryCache.removeEntries(file);
	}

	/**
	 * This method returns an array of filesystem roots.
	 */
	public File[] getRoots() {
		final Set rootSet = fsm.fileSystemRoots();
		final File[] list = new File[rootSet.size()];
		final Iterator i = rootSet.iterator();
		for (int j = 0; j < list.length; j++) {
			list[j] = new File((String)i.next());
		}
		return list;
	}

	/**
	 * Gets an array of names of all entries of the given directory.
	 * All names are relative to the given directory.
	 * @param directory
	 * @param filter
	 */
	public String[] list(File directory, FilenameFilter filter)
	throws IOException {
		final FSEntry entry = getEntry(directory);
		if (entry == null) {
			throw new FileNotFoundException(directory.getAbsolutePath());
		}
		if (!entry.isDirectory()) {
			throw new IOException(
				"Cannot list on non-directories " + directory);
		}
		final ArrayList list = new ArrayList();
		for (Iterator i = entry.getDirectory().iterator(); i.hasNext();) {
			final FSEntry child = (FSEntry)i.next();
			final String name = child.getName();
			if ((filter == null) || (filter.accept(directory, name))) {
				list.add(name);
			}
		}
		return (String[])list.toArray(new String[list.size()]);
	}

	/**
	 * Gets the FSEntry for the given path, or null if not found.
	 * @param path
	 */
	private FSEntry getEntry(File path) {
		FSEntry entry = entryCache.getEntry(path);
		if (entry != null) {
			return entry;
		}
		final File parent = path.getParentFile();
		if (parent != null) {
			final FSEntry parentEntry = getEntry(parent);
			if (parentEntry == null) {
				log.debug("Parent (" + parent + ") not found");
				return null;
			}
			if (!parentEntry.isDirectory()) {
				log.debug("Parent (" + parent + ") not a directory");
				return null;
			}
			try {
				entry = parentEntry.getDirectory().getEntry(path.getName());
				entryCache.setEntry(path, entry);
				return entry;
			} catch (IOException ex) {
				// Not found
				log.debug("parent.getEntry failed", ex);
				return null;
			}
		} else {
			// Root name
			final FileSystem fs = fsm.getFileSystem(path.getName());
			if (fs == null) {
				log.debug("Filesystem (" + path.getName() + ") not found");
				return null;
			}
			try {
				entry = fs.getRootEntry();
				entryCache.setEntry(path, entry);
				return entry;
			} catch (IOException ex) {
				log.debug("Filesystem.getRootEntry failed", ex);
				return null;
			}
		}
	}

	/**
	 * Open a given file
	 * @param file
	 * @throws IOException
	 */
	public VMFileHandle open(File file, VMOpenMode mode) throws IOException {
		FSEntry entry = getEntry(file);
		if ((entry != null) && !entry.isFile()) {
			throw new IOException("Not a file " + file);
		}
		if (entry == null) {
			if (mode.canWrite()) {
				// Try to create the file
				final FSEntry parent = getEntry(file.getParentFile());
				if (parent == null) {
					throw new IOException(
						"Cannot create "
							+ file.getAbsolutePath()
							+ ", parent directory does not exist");
				}
				if (!parent.isDirectory()) {
					throw new IOException(
						"Cannot create "
							+ file.getAbsolutePath()
							+ ", parent is not a directory");
				}
				// Ok, add the file
				entry = parent.getDirectory().addFile(file.getName());
			} else {
				throw new FileNotFoundException(file.getAbsolutePath());
			}
		}
		return fhm.open(entry.getFile(), mode);
	}
}
