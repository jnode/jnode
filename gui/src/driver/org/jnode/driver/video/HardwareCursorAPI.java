/*
 * $Id$
 */
package org.jnode.driver.video;

import org.jnode.driver.DeviceAPI;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface HardwareCursorAPI extends DeviceAPI {

	/**
	 * Show/Hide the cursor
	 * @param visible
	 */
	public void setCursorVisible(boolean visible);
	
	/**
	 * Set the cursor position
	 * @param x
	 * @param y
	 */
	public void setCursorPosition(int x, int y);
	
}
