/*
 * $Id$
 */
package org.jnode.fs.fat;

import org.jnode.fs.FSObject;
import org.jnode.fs.FileSystem;

/**
 * @author epr
 */
public abstract class FatObject implements FSObject {
	
	/** The filesystem I'm a part of */
	private final FatFileSystem fs;
	/** Is this object still valid? */
	private boolean valid;
	
	public FatObject(FatFileSystem fs) {
		this.fs = fs;
		this.valid = true;
	}

	/**
	 * Is this object still valid. 
	 * 
	 * An object is not valid anymore if it has been removed from the filesystem.
	 * All invocations on methods (exception this method) of invalid objects 
	 * must throw an IOException.
	 */
	public final boolean isValid() {
		return valid;
	}
	
	/**
	 * Mark this object as invalid.
	 */
	protected void invalidate() {
		valid = false;
	}
	
	/**
	 * Gets the filesystem I'm a part of.
	 */
	public final FileSystem getFileSystem() {
		return fs;
	}

	/**
	 * Gets the filesystem I'm a part of.
	 */
	public final FatFileSystem getFatFileSystem() {
		return fs;
	}
}
