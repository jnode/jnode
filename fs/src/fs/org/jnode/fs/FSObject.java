/*
 * $Id$
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
