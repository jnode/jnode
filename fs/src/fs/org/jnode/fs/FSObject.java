/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.fs;

/**
 * This interface is the base interface objects that are part of a FileSystem.
 *
 * @author epr
 */
public interface FSObject {
	
	/**
	 * Is this object still valid.
	 * 
	 * An object is not valid anymore if it has been removed from the filesystem.
	 * All invocations on methods (exception this method) of invalid objects 
	 * must throw an IOException.
	 */
	public boolean isValid();
	
	/**
	 * Gets the filesystem to which this object belongs.
	 */
	public FileSystem getFileSystem();

}
