/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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

package org.jnode.fs;

import org.jnode.driver.Device;

/**
 * Descriptor and entry point for a class of file systems.
 * 
 * @param <T> {@link FileSystem}
 * 
 * @author epr
 * 
 */
public interface FileSystemType<T extends FileSystem<?>> {

    /**
     * Gets the unique name of this file system type.
     * 
     * @return name of the file system.
     */
    public String getName();

    /**
     * Create a file system from a given device.
     * 
     * @param device {@link Device} contains the file system.
     * @param readOnly set to <tt>true</tt> if the new file system must be read
     *            only.
     * @return a file system
     * @throws FileSystemException if error occurs during creation of the new
     *             file system.
     */
    public T create(Device device, boolean readOnly) throws FileSystemException;
}
