/*
 * $Id$
 */
package org.jnode.fs.ext2;

import java.security.Principal;

import org.jnode.fs.FSAccessRights;
import org.jnode.fs.FileSystem;

/**
 * @author Andras Nagy
 */
public class Ext2AccessRights implements FSAccessRights {

	/**
	 * @see org.jnode.fs.FSAccessRights#getOwner()
	 */
	public Principal getOwner() {
		return null;
	}

	/**
	 * @see org.jnode.fs.FSObject#isValid()
	 */
	public boolean isValid() {
		return false;
	}

	/**
	 * @see org.jnode.fs.FSObject#getFileSystem()
	 */
	public FileSystem getFileSystem() {
		return null;
	}

}
