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
 
package org.jnode.driver.video;

import org.jnode.driver.DeviceAPI;
import org.jnode.driver.DeviceException;


/**
 * This API must be implemented by all FrameBuffer devices.
 * Is is used to retrieve all configurations of the device and
 * open a graphics object for a specific configuration.
 * 
 * @author epr
 */
public interface FrameBufferAPI extends DeviceAPI {
	
	/**
	 * Gets all configurations supported by this framebuffer device.
	 */
	public FrameBufferConfiguration[] getConfigurations();
	
	/**
	 * Gets the current configuration of this framebuffer.
	 */
	public FrameBufferConfiguration getCurrentConfiguration();
	
	/**
	 * Open a specific framebuffer configuration
	 * @param config
	 */
	public Surface open(FrameBufferConfiguration config)
	throws UnknownConfigurationException, AlreadyOpenException, DeviceException;
}
