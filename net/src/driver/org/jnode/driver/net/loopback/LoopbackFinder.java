/*
 * $Id$
 */
package org.jnode.driver.net.loopback;

import org.jnode.driver.Bus;
import org.jnode.driver.DeviceException;
import org.jnode.driver.DeviceFinder;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DriverException;

/**
 * @author epr
 */
public class LoopbackFinder implements DeviceFinder {

	/**
	 * @see org.jnode.driver.DeviceFinder#findDevices(org.jnode.driver.DeviceManager, org.jnode.driver.Bus)
	 */
	public void findDevices(DeviceManager devMan, Bus bus) throws DeviceException {
		try {
			devMan.register(new LoopbackDevice(bus));
		} catch (DriverException ex) {
			throw new DeviceException(ex);
		}
	}

}
