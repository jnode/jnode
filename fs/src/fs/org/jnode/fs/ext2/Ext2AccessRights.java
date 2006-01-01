/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 * You should have received a copy of the GNU General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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
