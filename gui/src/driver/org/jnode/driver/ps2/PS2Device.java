/*
 * $Id$
 */
package org.jnode.driver.ps2;

import org.jnode.driver.Device;

/**
 * @author qades
 */
public class PS2Device extends Device {

	public PS2Device(PS2Bus bus, String id) {
		super(bus, id);
	}
	
	/**
	 * Gets the PS2 bus to which this device is attached
	 * @return The bus
	 */
	public PS2Bus getPS2Bus() {
		return (PS2Bus)getBus();
	}

}
