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

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A FSFile is a representation of a single block of bytes on a filesystem. It
 * is comparable to an inode in Unix.
 * 
 * An FSFile does not have any knowledge of who is using this file. It is also
 * possible that the system uses a single FSFile instance to create two
 * inputstream's for two different principals.
 * 
 * @author epr
 */
public interface FSFile extends FSObject {

    /**
     * Gets the length (in bytes) of this file
     * 
     * @return long
     */
    public long getLength();

    /**
     * Sets the length of this file.
     * 
     * @param length
     * @throws IOException
     */
    public void setLength(long length) throws IOException;

    /**
     * Read <code>len</code> bytes from the given position. The read data is
     * read fom this file starting at offset <code>fileOffset</code> and
     * stored in <code>dest</code> starting at offset <code>ofs</code>.
     * 
     * @param fileOffset
     * @param dest
     * @throws IOException
     */
    public void read(long fileOffset, ByteBuffer dest) throws IOException;

    /**
     * Write <code>len</code> bytes to the given position. The data is read
     * from <code>src</code> starting at offset <code>ofs</code> and written
     * to this file starting at offset <code>fileOffset</code>.
     * 
     * @param fileOffset
     * @param src
     * @throws IOException
     */
    public void write(long fileOffset, ByteBuffer src) throws IOException;

    /**
     * Flush any cached data to the disk.
     * 
     * @throws IOException
     */
    public void flush() throws IOException;
}
