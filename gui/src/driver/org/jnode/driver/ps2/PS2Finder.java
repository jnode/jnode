/*
 * $Id$
 */
package org.jnode.driver.ps2;

import org.apache.log4j.Logger;
import org.jnode.driver.Bus;
import org.jnode.driver.DeviceException;
import org.jnode.driver.DeviceFinder;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DriverException;

/**
 * PS2 device finder.
 * 
 * @author qades
 */
public class PS2Finder implements DeviceFinder, PS2Constants {

	/** My logger */
	private final Logger log = Logger.getLogger(getClass());

	/**
	 * @see org.jnode.driver.DeviceFinder#findDevices(org.jnode.driver.DeviceManager,
	 *      org.jnode.driver.Bus)
	 */
	public void findDevices(DeviceManager devMan, Bus bus) throws DeviceException {
		try {
			log.debug("Searching for PS2 devices");
			final PS2Bus ps2 = new PS2Bus(bus);

			// register the keyboard device
			PS2Device kbDev = new PS2Device(ps2, PS2_KEYBOARD_DEV);
			kbDev.setDriver(new PS2KeyboardDriver(ps2));
			devMan.register(kbDev);

			// register the keyboard device
			PS2Device pDev = new PS2Device(ps2, PS2_POINTER_DEV);
			pDev.setDriver(new PS2PointerDriver(ps2));
			devMan.register(pDev);
		} catch (DriverException ex) {
			log.debug("Error searching for PS2 devices: " + ex);
			throw new DeviceException(ex);
		}
	}

}
