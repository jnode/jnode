/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
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
	abstract public Device getDevice() throws Exception;
		
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
