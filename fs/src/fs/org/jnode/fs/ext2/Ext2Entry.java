package org.jnode.fs.ext2;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.jnode.fs.FSAccessRights;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystem;

/**
 * @author Andras Nagy
 * 
 * This class reads the actual data from the disk. In case of a directory, this
 * data will be parsed to get the file-list by Ext2Directory. In case of a
 * regular file, no more processing is needed.
 */
public class Ext2Entry implements FSEntry {

	private static final Logger log = Logger.getLogger(Ext2Entry.class);
	INode iNode = null;
	String name = null;

	public Ext2Entry(INode iNode, String name) {
		this.iNode = iNode;
		this.name = name;

		log.debug(
			"Ext2Entry(iNode, name): name="
				+ name
				+ (isDirectory() ? " is a directory " : "")
				+ (isFile() ? " is a file " : ""));

	}
	/**
	 * @see org.jnode.fs.FSEntry#getAccessRights()
	 */
	public FSAccessRights getAccessRights() {
		return null;
	}

	/**
	 * @see org.jnode.fs.FSEntry#getDirectory()
	 */
	public FSDirectory getDirectory() throws IOException {
		if (isDirectory())
			return new Ext2Directory(iNode);
		else
			throw new IOException("Not a directory");
	}

	/**
	 * @see org.jnode.fs.FSEntry#getFile()
	 */
	public FSFile getFile() throws IOException {
		if (isFile())
			return new Ext2File(iNode);
		else
			throw new IOException("Not a file");
	}

	/**
	 * @see org.jnode.fs.FSEntry#getLastModified()
	 */
	public long getLastModified() {
		return 0;
	}

	/**
	 * @see org.jnode.fs.FSEntry#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see org.jnode.fs.FSEntry#getParent()
	 */
	public FSDirectory getParent() {
		return null;
	}

	/**
	 * @see org.jnode.fs.FSEntry#isDirectory()
	 */
	public boolean isDirectory() {
		return ((iNode.getIMode() & INode.EXT2_S_IFMT) == INode.EXT2_S_IFDIR) ? true : false;
	}

	/**
	 * @see org.jnode.fs.FSEntry#isFile() XXX what about the other ones?
	 *      symlink, block device, character device?
	 */
	public boolean isFile() {
		return ((iNode.getIMode() & INode.EXT2_S_IFMT) == INode.EXT2_S_IFREG) ? true : false;
	}

	/**
	 * @see org.jnode.fs.FSEntry#setLastModified(long)
	 */
	public void setLastModified(long lastModified) {
		// empty
	}

	/**
	 * @see org.jnode.fs.FSEntry#setName(String)
	 */
	public void setName(String newName) {
		// empty
	}

	/**
	 * @see org.jnode.fs.FSObject#getFileSystem()
	 */
	public FileSystem getFileSystem() {
		return null;
	}

	/**
	 * @see org.jnode.fs.FSObject#isValid()
	 */
	public boolean isValid() {
		return false;
	}

}
