/*
 * $Id$
 */
package org.jnode.driver;

/**
 * Interface used to discover devices on a given bus.
 *
 * @see org.jnode.driver.Device 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface DeviceFinder {

	/**
	 * Find all devices that are connected to the given bus and register 
	 * them with the given device manager.
	 * @param devMan
	 * @param bus
	 * @throws DeviceException
	 */
	public void findDevices(DeviceManager devMan, Bus bus)
	throws DeviceException;

}
