/*
 * $Id$
 *
 * JNode.org
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

import java.io.IOException;

import org.jnode.driver.Device;

/**
 * @author epr
 */
public interface FileSystem<T extends FSEntry> {

    public FileSystemType<? extends FileSystem<T>> getType();

    /**
     * Gets the device this FS driver operates on.
     */
    public Device getDevice();

    /**
     * Gets the root entry of this filesystem. This is usually a directory, but
     * this is not required.
     */
    public T getRootEntry() throws IOException;

    /**
     * Is the file system. mounted in read-only mode ?
     */
    public boolean isReadOnly();

    /**
     * Close this file system. After a close, all invocations of method of this
     * file system. or objects created by this file system. will throw an
     * IOException.
     * 
     * @throws IOException
     */
    public void close() throws IOException;

    /**
     * Is this file system. closed.
     */
    public boolean isClosed();

    /**
     * The total size of this file system.
     * @return if -1 this feature is unsupported
     * @throws IOException if an I/O error occurs
     */
    public long getTotalSpace() throws IOException;

    /**
     * The free space of this file system.
     * @return if -1 this feature is unsupported
     * @throws IOException if an I/O error occurs
     */
    public long getFreeSpace() throws IOException;

    /**
     * The usable space of this file system.
     * @return if -1 this feature is unsupported
     * @throws IOException if an I/O error occurs
     */
    public long getUsableSpace() throws IOException;
}
