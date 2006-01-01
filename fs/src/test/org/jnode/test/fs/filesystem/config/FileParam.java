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

import java.io.File;
import java.io.IOException;

import org.jnode.driver.Device;
import org.jnode.driver.block.FileDevice;
import org.jnode.test.support.TestUtils;

/**
 * @author Fabien DUMINY 
 *
 */
public class FileParam extends DeviceParam 
{
	/**
	 * 
	 *
	 */
	public FileParam(String fileName, String fileSize)
	{		
        setFile(new File(fileName));
        setSize(fileSize);
	}
	
	/**
	 * 
	 */
	public Device getDevice() throws IOException
	{
		String mode = isInput() ? "r" : "rw";
		if(!isInput() && (fileSize > 0L) && (!file.exists() || (file.length() != fileSize)))
			TestUtils.makeFile(file.getAbsolutePath(), fileSize);
		
		return new FileDevice(file, mode);
	}
	
	/**
	 * 
	 */
	public long getDeviceSize()
	{
		return fileSize;
	}

	/**
	 * @param file The file to set.
	 */
	public void setFile(File file) {
		this.file = file;
	}
	/**
	 * @param size The fileSize to set.
	 */
	public void setSize(String size) {
		if((size == null) || size.trim().equals(""))
			this.fileSize = 0;
		
		int multiplier = 1;		
		if(size.endsWith("G"))
		{
			multiplier = 1024 * 1024 * 1024;
			size = size.substring(0, size.length() - 1);
		}
		else if(size.endsWith("M"))
		{
			multiplier = 1024 * 1024; 
			size = size.substring(0, size.length() - 1);
		}
		else if(size.endsWith("K"))
		{
			multiplier = 1024; 			
			size = size.substring(0, size.length() - 1);
		}
		
		this.fileSize = Long.parseLong(size) * multiplier;
	}

	/**
	 * 
	 */
	public void tearDown(Device device)
	{
		if(!isInput())
			file.delete();
	}
	
	/**
	 * 
	 */
	public String toString()
	{
		return "File[\""+file.getName() + "\" size=" + fileSize + "]";
	}
	
	private File file;
	private long fileSize;
}
