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
 
package org.jnode.test.fs.filesystem.config;

import java.io.IOException;

import javax.naming.NameNotFoundException;

import org.jnode.driver.Device;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;

/**
 * 
 * @author Fabien DUMINY
 */
public class FS {
	final private FSType type;
	final private boolean readOnly;
	final private boolean format;
	final private Object options;
			
	public FS(FSType type, boolean readOnly, Object options, boolean format)
	{
		this.type = type;
        this.readOnly = readOnly;
        this.format = format;
        this.options = options;
	}
	
	/**
	 * @return Returns the type.
	 */
	public FSType getType() {
		return type;
	}
	/**
	 * @return Returns the readOnly.
	 */
	public boolean isReadOnly() {
		return readOnly;
	}
    
	public FileSystem mount(Device device) throws FileSystemException, IOException, InstantiationException, IllegalAccessException, NameNotFoundException
	{
		if(format)
			type.format(device, options);
		
		return type.mount(device, readOnly);
	}
	
	public String toString()
	{
		return type.toString() + '(' + String.valueOf(options) +')' + 
			   (readOnly ? " ro" : " rw") + 
			   (format ? "" : "not") + " formatted";
	}
}
