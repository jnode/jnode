/*
 * $Id$
 */
package org.jnode.driver.block.ramdisk;

import org.jnode.driver.Bus;
import org.jnode.driver.Device;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class RamDiskDevice extends Device {

	private final int size;
	
	public RamDiskDevice(Bus bus, String id, int size) {
		super(bus, id);
		this.size = size;
	}
	
	/**
	 * @return Returns the size.
	 */
	public final int getSize() {
		return this.size;
	}
}
