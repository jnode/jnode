/*
 * $Id$
 */
package org.jnode.driver;

/**
 * A software representation of a hardware bus that has 
 * devices connected to it.
 *
 * @see org.jnode.driver.Device 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class Bus {

	/** My parent, or null if I'm the system bus */
	private final Bus parent;
	/** The device that is connected to the parent bus and "provides" this bus. This can be null. */
	private final Device parentDevice;

	/**
	 * Package protected initializer for the system bus.
	 */
	Bus() {
		this.parent = null;	
		this.parentDevice = null;		
	}

	/**
	 * Initialize a new bus.
	 * @param parent
	 */
	public Bus(Bus parent) {
		this.parent = parent;	
		this.parentDevice = null;
	}
	
	/**
	 * Initialize a new bus.
	 * @param parent
	 */
	public Bus(Device parent) {
		this.parent = parent.getBus();	
		this.parentDevice = parent;
	}
	
	/**
	 * Gets the parent of this bus, or null if this is the system bus.
	 * @return The parent
	 */
	public final Bus getParent() {
		return this.parent;
	}

	/**
	 * Gets the device that is connected to the parent bus 
	 * and "provides" this bus. 
	 * This can be null.
	 * @return The parent device
	 */
	public final Device getParentDevice() {
		return this.parentDevice;
	}

}
