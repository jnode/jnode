/*
 * $Id$
 */
package org.jnode.driver.video.vga;

import org.jnode.driver.Bus;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceException;
import org.jnode.driver.DeviceFinder;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DriverException;

/**
 * @author epr
 */
public class VGAFinder implements DeviceFinder {

	/**
	 * @see org.jnode.driver.DeviceFinder#findDevices(org.jnode.driver.DeviceManager, org.jnode.driver.Bus)
	 */
	public void findDevices(DeviceManager devMan, Bus bus) throws DeviceException {
		/*try {
			devMan.register(new VGADevice(bus));
		} catch (DriverException ex) {
			throw new DeviceException(ex);
		}*/
	}

	public static class VGADevice extends Device {
				
		/**
		 * @param bus
		 */
		public VGADevice(Bus bus) 
		throws DriverException {
			super(bus, "Standard VGA");
			this.setDriver(new VGADriver());
		}
	}
}
