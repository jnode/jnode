/*
 * $Id$
 */
package org.jnode.driver.chipset.via;

import org.apache.log4j.Logger;
import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;
import org.jnode.driver.pci.PCIDevice;
import org.jnode.util.NumberUtils;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class Via82C686 extends Driver {

	/** My logger */
	private final Logger log = Logger.getLogger(getClass());
	
	/**
	 * Start the device
	 * @throws DriverException
	 */
	protected void startDevice() throws DriverException {
		// TODO apply io-apic quirk
		
		final PCIDevice dev = (PCIDevice)getDevice();
		for (int i = 0x55; i <= 0x58; i++) {
			final int v = dev.readConfigByte(i);
			log.debug("PCI[" + NumberUtils.hex(i, 2) + "] " + NumberUtils.hex(v, 2));
		}
		
	}

	/**
	 * Stop the device
	 * @throws DriverException
	 */
	protected void stopDevice() throws DriverException {
		// Nothing to do here
	}
}
