package org.jnode.fs.ext2;

import java.io.IOException;

import org.jnode.fs.*;

/**
 * @author Andras Nagy
 * 
 * This class reads the actual data from the disk.
 * In case of a directory, this data will be parsed to get the file-list by
 * Ext2Directory. In case of a regular file, no more processing is needed.
 * 
 * TODO: besides getFile() and getDirectory(), we will need 
 * 	getBlockDevice()
 * 	getCharacterDevice(), etc.
 */
public class Ext2Entry implements FSEntry{

	private INode iNode=null;
	private String name=null;
	private int type;
	private boolean valid;

	public Ext2Entry(INode iNode, String name, int type) {
		this.iNode = iNode;
		this.name  = name;
		this.type  = type;
		this.valid = true;
		
		Ext2Debugger.debug("Ext2Entry(iNode, name): name="+name+
			(isDirectory()?" is a directory ":"")+
			(isFile()?" is a file ":""), 2);
		
	}
	/**
	 * Will be implemented once JNode has a notion of users and groups
	 * @see org.jnode.fs.FSEntry#getAccessRights()
	 */
	public FSAccessRights getAccessRights() throws IOException {
		throw new IOException("Not implemented yet");
	}

	/**
	 * @see org.jnode.fs.FSEntry#getDirectory()
	 */
	public FSDirectory getDirectory() throws IOException {
		if(isDirectory())
			return new Ext2Directory( iNode );
		else
			throw new IOException("Not a directory");
	}

	/**
	 * @see org.jnode.fs.FSEntry#getFile()
	 */
	public FSFile getFile() throws IOException {
		if(isFile())
			return new Ext2File( iNode );
		else
			throw new IOException("Not a file");
	}

	/**
	 * @see org.jnode.fs.FSEntry#getLastModified()
	 */
	public long getLastModified() throws IOException {
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
		return ((iNode.getMode()&Ext2Constants.EXT2_S_IFMT) == Ext2Constants.EXT2_S_IFDIR)?true:false;
	}

	/**
	 * @see org.jnode.fs.FSEntry#isFile()
	 */
	public boolean isFile() {
		int mode=iNode.getMode()&Ext2Constants.EXT2_S_IFMT;
		return (mode == Ext2Constants.EXT2_S_IFREG 	||
				mode == Ext2Constants.EXT2_FT_SYMLINK)?true:false;
	}

	/**
	 * @see org.jnode.fs.FSEntry#setLastModified(long)
	 */
	public void setLastModified(long lastModified) throws IOException {
		throw new IOException("EXT2 implementation is currently readonly");
	}

	/**
	 * @see org.jnode.fs.FSEntry#setName(String)
	 */
	public void setName(String newName) throws IOException {
		throw new IOException("EXT2 implementation is currently readonly");
	}

	/**
	 * @see org.jnode.fs.FSObject#getFileSystem()
	 */
	public FileSystem getFileSystem() {
		return iNode.getExt2FileSystem();
	}

	/**
	 * Returns the type.
	 * @return int type. Valid types are Ext2Constants.EXT2_FT_*
	 */
	public int getType() {
		return type;
	}

	/**
	 * @see org.jnode.fs.FSObject#isValid()
	 */
	public boolean isValid() {
		return valid;
	}

	/**
	 * Sets the valid status.
	 * @param valid The valid status to set
	 */
	public void setValid(boolean valid) {
		this.valid = valid;
	}

}
