/*
 * $Id$
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
