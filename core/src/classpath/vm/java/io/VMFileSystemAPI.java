/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package java.io;

import org.jnode.java.io.VMFileHandle;

/**
 * The implementation of this interface is used to connect the java.io package with the JNode
 * filesystem services.
 * The file paths given to the various methods are absolute and canonical.
 * 
 * @modif  add mkDir mkFile   Yves Galante (yves.galante@jmob.net) 01.04.2004
 * @author epr
 */
public interface VMFileSystemAPI {

	/**
	 * Does a given file exist?
	 */
	public boolean fileExists(String file);

	/**
	 * Is the given File a plain file?
	 */
	public boolean isFile(String file);

	/**
	 * Is the given File a directory?
	 */
	public boolean isDirectory(String file);

	/**
	 * Can the given file be read?
	 * 
	 * @param file
	 */
	public boolean canRead(String file) throws IOException;

	/**
	 * Can the given file be written to?
	 * 
	 * @param file
	 */
	public boolean canWrite(String file) throws IOException;

	public boolean canExecute(String file) throws IOException;
	
	/**
	 * Gets the length in bytes of the given file or 0 if the file does not exist.
	 * 
	 * @param file
	 */
	public long getLength(String file);

	/**
	 * Gets the last modification date of the given file.
	 * 
	 * @param file
	 */
	public long getLastModified(String file);

	/**
	 * Sets the last modification date of the given file.
	 * 
	 * @param file
	 */
	public void setLastModified(String file, long time) throws IOException;

	/**
	 * Mark the given file as readonly.
	 * 
	 * @param file
	 * @throws IOException
	 */
	public void setReadOnly(String file) throws IOException;

	public boolean setReadable(String normalizedPath, boolean enable,
			boolean owneronly) throws IOException;

	public boolean setWritable(String normalizedPath, boolean enable,
			boolean owneronly) throws IOException;

	public boolean setExecutable(String normalizedPath, boolean enable,
			boolean owneronly) throws IOException;

	/**
	 * Delete the given file.
	 * 
	 * @param file
	 * @throws IOException
	 */
	public void delete(String file) throws IOException;

	/**
	 * This method returns an array of filesystem roots.
	 */
	public File[] getRoots();
	
	/**
	 * Gets an array of names of all entries of the given directory. All names are relative to the
	 * given directory.
	 * 
	 * @param directory
	 * @param filter
	 */
	public String[] list(String directory) throws IOException;

	/**
	 * Open a given file
	 * 
	 * @param file
	 * @throws IOException
	 */
	public VMFileHandle open(String file, VMOpenMode mode) throws IOException;

	/**
	 * Make a directory
	 * 
	 * @param file
	 * @throws IOException
	 */
	public boolean mkDir(String file) throws IOException;
	
	/**
	 * Make a file
	 * 
	 * @param file
	 * @throws IOException
	 */
	public boolean mkFile(String file, VMOpenMode mode) throws IOException;

	public long getTotalSpace(String normalizedPath) throws IOException;

	public long getFreeSpace(String normalizedPath) throws IOException;

	public long getUsableSpace(String normalizedPath) throws IOException;
}
