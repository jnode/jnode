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
 * A FSFile is a representation of a single block of bytes on a file system. It
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
     * Gets the length in bytes of this file.
     * 
     * @return the number of byte in this file.
     */
    public long getLength();

    /**
     * Sets the length in bytes of this file.
     * 
     * @param length the number of byte in this file.
     *  
     * @throws IOException if error occurs during set of file's length.
     */
    public void setLength(long length) throws IOException;

    /**
     * Read from this file starting at offset <code>fileOffset</code> and
     * stored in <code>dest</code> byte buffer.
     * 
     * @param fileOffset position in the file  where the read begins.
     * @param dest {@link ByteBuffer} receive contains of the file.
     * 
     * @throws IOException if error occurs during reading of the data.
     */
    public void read(long fileOffset, ByteBuffer dest) throws IOException;

    /**
     * Read bytes from  <code>src</code> byte buffer and written
     * to this file starting at offset <code>fileOffset</code>
     * 
     * @param fileOffset position in the file where datas are written.
     * @param src {@link ByteBuffer} contains datas to write.
     * 
     * @throws IOException if error occurs during write of the datas.
     */
    public void write(long fileOffset, ByteBuffer src) throws IOException;

    /**
     * Save all unsaved datas from the cache to the device.
     * 
     * @throws IOException if error occurs during flush of datas.
     */
    public void flush() throws IOException;
}
