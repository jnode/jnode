/*
 * $Id$
 */
package org.jnode.driver;

/**
 * Listener interface for DeviceManager events.
 *  
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface DeviceManagerListener {
	
	/**
	 * The given device has been registered.
	 * @param device
	 */
	public void deviceRegistered(Device device);

	/**
	 * The given device is about to be unregistered.
	 * @param device
	 */
	public void deviceUnregister(Device device);
}
