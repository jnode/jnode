/*
 * $Id$
 */
package org.jnode.driver.net.loopback;

import org.jnode.driver.Bus;
import org.jnode.driver.Device;
import org.jnode.driver.DriverException;

/**
 * @author epr
 */
public class LoopbackDevice extends Device {
	
	/**
	 * @param bus
	 */
	public LoopbackDevice(Bus bus)
	throws DriverException {
		super(bus, "loopback");
		setDriver(new LoopbackDriver());
	}
}
