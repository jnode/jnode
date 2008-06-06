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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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
