/*
 * $Id$
 */
package org.jnode.driver.video.ddc;

import org.jnode.driver.DeviceAPI;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface DisplayDataChannelAPI extends DeviceAPI {
	
	/**
	 * Start a DDC1 readout
	 */
	public void setupDDC1();
	
	/**
	 * Terminate a DDC1 readout
	 */
	public void closeDDC1();
	
	/**
	 * Wait for the vsync signal and return the current ddc1 bit.
	 * @return True for '1', false for '0'
	 */
	public boolean getDDC1Bit();

}
