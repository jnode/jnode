/**
 * $Id$
 */
package org.jnode.fs.spi;

import java.io.IOException;

import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystem;

/**
 * An abstract implementation of FSFile that contains common things
 * among many FileSystems
 * @author Fabien DUMINY
 */
public abstract class AbstractFSFile extends AbstractFSObject 
					implements FSFile {
	/**
	 * Constructor for a new AbstractFSFile    
	 */
	public AbstractFSFile(AbstractFileSystem fs) {
		super(fs);
	}

	/**
	 * Return the length of the file
	 */
	public abstract long getLength();

	/**
	 * chanhge the length of the file
	 */
	public abstract void setLength(long length) throws IOException;

	/**
	 * Read some data from the file
	 */
	public abstract void read(long fileOffset, byte[] dest, int off, int len)
			throws IOException;

	/**
	 * Write some data to the file
	 */
	public abstract void write(long fileOffset, byte[] src, int off, int len)
			throws IOException;

	/**
	 * Flush all unsaved data to the device
	 */
	public abstract void flush() throws IOException;
}
