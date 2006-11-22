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
	 * @param fs 
	 */
	public AbstractFSFile(AbstractFileSystem fs) {
		super(fs);
	}

	/**
	 * @return the length of the file
	 */
	public abstract long getLength();

	/**
	 * Read some data from the file
	 * 
	 * @param fileOffset offset to begin reading
	 * @param dest the ByteBuffer used to store the read data
	 * @throws IOException
	 */
    public abstract void read(long fileOffset, ByteBuffer dest) throws IOException;

	/**
	 * Write some data to the file
	 * 
	 * @param fileOffset offset to begin writing
	 * @param src buffer to be written the file
	 * @throws IOException
	 */
	public abstract void write(long fileOffset, ByteBuffer src) throws IOException;

	/**
	 * Flush all unsaved data to the device
	 * @throws IOException 
	 */
	public abstract void flush() throws IOException;
}
