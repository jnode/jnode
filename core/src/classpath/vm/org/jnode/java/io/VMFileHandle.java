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
 
package org.jnode.java.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;


/**
 * A FileHandle represents a single, opened file for a single principal.
 * @author epr
 */
public interface VMFileHandle {

    /**
     * Gets the length (in bytes) of this file
     * @return long
     */
    public long getLength();

    /**
     * Sets the length of this file.
     * @param length
     * @throws IOException
     */
    public void setLength(long length) throws IOException;

    /**
     * Gets the current position in the file
     * @return long
     */
    public long getPosition();

    /**
     * Sets the position in the file.
     * @param position
     * @throws IOException
     */
    public void setPosition(long position) throws IOException;

    /**
     * Read bytes into the {@code dest} buffer.
     * @param dest
     * @return the number of bytes read.
     * @throws IOException
     */	
    public int read(ByteBuffer dest) throws IOException;


    /**
     * Write bytes from the {@code src} buffer.
     * @param src
     * @throws IOException
     */	
    public void write(ByteBuffer src) throws IOException;

    /**
     * Close this file.
     */
    public void close() throws IOException;

    /**
     * Has this handle been closed?
     */
    public boolean isClosed();

    public int available();

    public void unlock(long pos, long len);

    public int read() throws IOException;

    public void write(int b) throws IOException;

    public boolean lock();

    public MappedByteBuffer mapImpl(char mode, long position, int size);
}
