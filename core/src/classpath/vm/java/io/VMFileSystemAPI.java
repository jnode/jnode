/*
 * $Id$
 */
package java.io;

/**
 * The implementation of this interface is used to connect the java.io package with the JNode
 * filesystem services.
 * 
 * @modif  add mkDir mkFile   Yves Galante (yves.galante@jmob.net) 01.04.2004
 * @author epr
 */
public interface VMFileSystemAPI {

	/**
	 * Does a given file exist?
	 */
	public boolean fileExists(File file);

	/**
	 * Is the given File a plain file?
	 */
	public boolean isFile(File file);

	/**
	 * Is the given File a directory?
	 */
	public boolean isDirectory(File file);

	/**
	 * Can the given file be read?
	 * 
	 * @param file
	 */
	public boolean canRead(File file);

	/**
	 * Can the given file be written to?
	 * 
	 * @param file
	 */
	public boolean canWrite(File file);

	/**
	 * Gets the length in bytes of the given file or 0 if the file does not exist.
	 * 
	 * @param file
	 */
	public long getLength(File file);

	/**
	 * Gets the last modification date of the given file.
	 * 
	 * @param file
	 */
	public long getLastModified(File file);

	/**
	 * Sets the last modification date of the given file.
	 * 
	 * @param file
	 */
	public void setLastModified(File file, long time) throws IOException;

	/**
	 * Mark the given file as readonly.
	 * 
	 * @param file
	 * @throws IOException
	 */
	public void setReadOnly(File file) throws IOException;

	/**
	 * Delete the given file.
	 * 
	 * @param file
	 * @throws IOException
	 */
	public void delete(File file) throws IOException;

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
	public String[] list(File directory, FilenameFilter filter) throws IOException;

	/**
	 * Open a given file
	 * 
	 * @param file
	 * @throws IOException
	 */
	public VMFileHandle open(File file, VMOpenMode mode) throws IOException;

	/**
	 * Make a directory
	 * 
	 * @param file
	 * @throws IOException
	 */
	public boolean mkDir(File file, VMOpenMode mode) throws IOException;
	
	/**
	 * Make a file
	 * 
	 * @param file
	 * @throws IOException
	 */
	public boolean mkFile(File file, VMOpenMode mode) throws IOException;
}
