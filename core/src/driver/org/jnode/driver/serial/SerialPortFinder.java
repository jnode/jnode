/*
 * $Id$
 * 
 * Serial port device finder Oct 15 2003, mgeisse
 */
package org.jnode.driver.serial;

import org.apache.log4j.Logger;
import org.jnode.driver.Bus;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceException;
import org.jnode.driver.DeviceFinder;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DriverException;

/**
 * @author mgeisse
 */
public class SerialPortFinder implements DeviceFinder {

	/** My logger */
	private final Logger log = Logger.getLogger(getClass());

	/**
	 * @see org.jnode.driver.DeviceFinder#findDevices(org.jnode.driver.DeviceManager,
	 *      org.jnode.driver.Bus)
	 */
	public void findDevices(DeviceManager devMan, Bus bus) throws DeviceException {
		try {
			log.debug("Starting serial port drivers");

			Device dev = new Device(bus, "serial0");
			dev.setDriver(new SerialPortDriver(0x3f8));
			devMan.register(dev);

			dev = new Device(bus, "serial1");
			dev.setDriver(new SerialPortDriver(0x2f8));
			devMan.register(dev);

		} catch (DriverException ex) {
			throw new DeviceException(ex);
		}
	}

}
