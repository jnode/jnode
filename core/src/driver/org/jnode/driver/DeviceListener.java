/*
 * $Id$
 */
package org.jnode.driver;

/**
 * Listener interface for device events.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface DeviceListener {

	/**
	 * The given device has been started
	 * 
	 * @param device
	 */
	public void deviceStarted(Device device);

	/**
	 * The given device will be stopped.
	 * 
	 * @param device
	 */
	public void deviceStop(Device device);

}
