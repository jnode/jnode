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
 
package org.jnode.fs.spi;

import java.nio.ByteBuffer;
import java.io.IOException;

import org.jnode.fs.FSFile;

/**
 * An abstract implementation of FSFile that contains common things
 * among many FileSystems
 * @author Fabien DUMINY
 */
public abstract class AbstractFSFile extends AbstractFSObject 
					implements FSFile {
	/**
	 * Constructor for a new AbstractFSFile    
	 */
	public AbstractFSFile(AbstractFileSystem fs) {
		super(fs);
	}

	/**
	 * Return the length of the file
	 */
	public abstract long getLength();

	/**
	 * chanhge the length of the file
	 */
	public abstract void setLength(long length) throws IOException;

	/**
	 * Read some data from the file
	 */
	//public abstract void read(long fileOffset, byte[] dest, int off, int len)
    public abstract void read(long fileOffset, ByteBuffer dest)
			throws IOException;

	/**
	 * Write some data to the file
	 */
//  public abstract void write(long fileOffset, byte[] src, int off, int len)
	public abstract void write(long fileOffset, ByteBuffer src)
			throws IOException;

	/**
	 * Flush all unsaved data to the device
	 */
	public abstract void flush() throws IOException;
}
