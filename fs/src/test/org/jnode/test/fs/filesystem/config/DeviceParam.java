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

import org.jnode.driver.Device;

/**
 * @author Fabien DUMINY
 */
public abstract class DeviceParam {
	private boolean input;
	
	/**
	 * 
	 *  
	 */
	protected DeviceParam()
	{
		
	}
	
	/**
	 * @return @throws
	 *         Exception
	 */
	abstract public Device createDevice() throws Exception;
		
	/**
	 * @param device
	 * @throws Exception
	 */
	abstract public void tearDown(Device device) throws Exception;
	
	/**
	 * @return Returns the input.
	 */
	public boolean isInput() {
		return input;
	}
	/**
	 * @param input The input to set.
	 */
	final public void setInput(boolean input) {
		this.input = input;
	}
}
