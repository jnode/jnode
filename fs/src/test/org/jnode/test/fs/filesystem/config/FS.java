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
 
package org.jnode.test.fs.filesystem.config;

import java.io.IOException;

import org.jnode.driver.Device;
import org.jnode.fs.FileSystemException;

/**
 * 
 * @author Fabien DUMINY
 */
public class FS {
	private FSType type;
	private boolean readOnly;
	private boolean format;
	private String options;
			
	public FS(FSType type, boolean readOnly, String options, boolean format)
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
	 * @param type The type to set.
	 */
	public void setType(FSType type) {
		this.type = type;
	}
	/**
	 * @return Returns the readOnly.
	 */
	public boolean isReadOnly() {
		return readOnly;
	}
	/**
	 * @param readOnly The readOnly to set.
	 */
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
    
	public void format(Device device) throws FileSystemException, IOException
	{
		if(format)
			type.format(device, options);
	}
	
	/**
	 * @param format The format to set.
	 */
	public void setFormat(boolean format) {
		this.format = format;
	}
	
	public String toString()
	{
		return type + (readOnly ? " ro" : " rw") + ",format=" + format;
	}

	/**
	 * @param options The options to set.
	 */
	public void setOptions(String options) {
		this.options = options;
	}
}
