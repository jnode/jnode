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

import org.jnode.util.NumberUtils;


/**
 * @author Fabien DUMINY 
 *
 */
public class RamDiskParam extends JNodeDeviceParam 
{
	/**
	 * 
	 *
	 */
	public RamDiskParam()
	{
	}
	
	/**
	 * 
	 * @param size
	 */
	public void setSize(String size)
	{
		ramdiskSize = NumberUtils.getSize(size);
	}
	
	/**
	 * 
	 */
	public long getSize()
	{
		return ramdiskSize;
	}

	/**
	 * 
	 */
	public String toString()
	{
		return "Ramdisk[name="+getDeviceName()+" size=" + ramdiskSize + "]";
	}
			
	private long ramdiskSize;
}
