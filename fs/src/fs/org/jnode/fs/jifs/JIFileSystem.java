/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.fs.jifs;

import java.io.IOException;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.spi.AbstractFileSystem;
import org.jnode.fs.FileSystem;
import org.jnode.driver.Device;
import org.jnode.fs.jifs.*;

/**
 * @author Trickkiste
 */
public class JIFileSystem implements FileSystem {
	
	private JIFSDirectory rootDir = null;
	private Device device;
	
	/**
	 * Constructor for JIFileSystem in specified readOnly mode
	 */
	public JIFileSystem(Device device, boolean readOnly) throws FileSystemException {
		if (readOnly==false){
			throw new FileSystemException("JIFS can not be created as writable...");
		}
		this.device = device;
		try {
			rootDir = new JIFSDirectory(this.device.getId(), true);
		} catch ( IOException e){
			e.printStackTrace();
		}
	}
	
	public void close(){
		return;
	}
	
	public Device getDevice(){
		return device;
	}

	/**
	 * Flush all changed structures to the device. Since JIFS is readonly, this method does nothing.
	 * 
	 * @throws IOException
	 */
	public void flush() throws IOException {
		return;
	}

	/**
	 * Gets the root entry of this filesystem. This is usually a director, but
	 * this is not required.
	 * 
	 * @return rootDir
	 */
	public FSEntry getRootEntry() {
		return rootDir;
	}

	/**
	 * Gets the file for the given entry.
	 *  
	 * @return null
	 * @param entry
	 */
	public synchronized JIFSFile getFile(FSEntry entry) {
		return null;		
	}


	protected FSFile createFile(FSEntry entry) throws IOException {
		return null;
	}

	protected FSDirectory createDirectory(FSEntry entry) throws IOException {
		return null;
	}

	protected FSEntry createRootEntry() throws IOException {
		return null;
	}
}
