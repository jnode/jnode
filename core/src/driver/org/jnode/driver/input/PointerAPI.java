/*
 * $Id$
 */
package org.jnode.driver.input;

import org.jnode.driver.DeviceAPI;

/**
 * Device API implemented by Pointer devices.
 * 
 * @author qades
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface PointerAPI extends DeviceAPI {

	/**
	 * Add a pointer listener
	 * 
	 * @param l
	 */
	public void addPointerListener(PointerListener l);

	/**
	 * Remove a pointer listener
	 * 
	 * @param l
	 */
	public void removePointerListener(PointerListener l);
}
