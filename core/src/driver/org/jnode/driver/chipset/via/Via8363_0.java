/*
 * $Id$
 */
package org.jnode.driver.chipset.via;

import org.apache.log4j.Logger;
import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;
import org.jnode.driver.pci.PCIDevice;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class Via8363_0 extends Driver {

	/** My logger */
	private final Logger log = Logger.getLogger(getClass());
	
	/**
	 * Start the device
	 * @throws DriverException
	 */
	protected void startDevice() throws DriverException {
		ViaQuirks.applyLatencyFix((PCIDevice)getDevice(), log);
	}

	/**
	 * Stop the device
	 * @throws DriverException
	 */
	protected void stopDevice() throws DriverException {
		// Nothing to do here
	}
}
