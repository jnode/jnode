/*
 * $Id$
 */
package org.jnode.driver.video;

import javax.naming.NameNotFoundException;

import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceAlreadyRegisteredException;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;
import org.jnode.naming.InitialNaming;

/**
 * @author epr
 */
public abstract class AbstractFrameBufferDriver extends Driver implements FrameBufferAPI {

	/** My logger */
	private final Logger log = Logger.getLogger(getClass());

	/**Device name prefix of framebuffer devices */
	public static final String FB_DEVICE_PREFIX = "fb";

	/**
	 * @see org.jnode.driver.Driver#startDevice()
	 */
	protected void startDevice() throws DriverException {
		final Device device = getDevice();
		try {
			final DeviceManager dm = (DeviceManager)InitialNaming.lookup(DeviceManager.NAME);
			dm.rename(device, getDevicePrefix(), true);
		} catch (DeviceAlreadyRegisteredException ex) {
			log.error("Cannot rename device", ex);
		} catch (NameNotFoundException ex) {
			throw new DriverException("Cannot find DeviceManager", ex);
		}
		device.registerAPI(FrameBufferAPI.class, this);
	}

	/**
	 * @see org.jnode.driver.Driver#stopDevice()
	 */
	protected void stopDevice() throws DriverException {
		final Device dev = getDevice();
		dev.unregisterAPI(FrameBufferAPI.class);
	}

	/**
	 * Gets the prefix for the device name
	 * @see #FB_DEVICE_PREFIX
	 */
	protected String getDevicePrefix() {
		return FB_DEVICE_PREFIX;
	}
	
}
