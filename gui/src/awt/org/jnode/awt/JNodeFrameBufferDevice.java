/*
 * $Id$
 */
package org.jnode.awt;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.video.FrameBufferAPI;
import org.jnode.driver.video.FrameBufferConfiguration;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class JNodeFrameBufferDevice extends GraphicsDevice {

	private final FrameBufferAPI api;
	private final Device device;
	private final JNodeGraphicsConfiguration[] configs;

	public JNodeFrameBufferDevice(Device device) {
		this.device = device;
		try {
			this.api = (FrameBufferAPI)device.getAPI(FrameBufferAPI.class);
		} catch (ApiNotFoundException ex) {
			throw new IllegalArgumentException("Not a FrameBuffer device", ex);
		}
		final FrameBufferConfiguration[] fbConfigs = api.getConfigurations();
		configs = new JNodeGraphicsConfiguration[fbConfigs.length];
		for (int i = 0; i < fbConfigs.length; i++) {
			configs[i] = new JNodeGraphicsConfiguration(this, fbConfigs[i]);
		} 
	}

	/**
	 * @see java.awt.GraphicsDevice#getConfigurations()
	 * @return The configurations
	 */
	public GraphicsConfiguration[] getConfigurations() {
		return configs;
	}

	/**
	 * @see java.awt.GraphicsDevice#getDefaultConfiguration()
	 * @return The default configuration
	 */
	public GraphicsConfiguration getDefaultConfiguration() {
		return configs[0];
	}

	/**
	 * @see java.awt.GraphicsDevice#getIDstring()
	 * @return The id string
	 */
	public String getIDstring() {
		return device.getId();
	}

	/**
	 * @see java.awt.GraphicsDevice#getType()
	 * @return The type
	 */
	public int getType() {
		return TYPE_RASTER_SCREEN;
	}
	
	public FrameBufferAPI getAPI() {
		return api;
	}
	
	public Device getDevice() {
		return device;
	}
}
