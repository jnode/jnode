/*
 * $Id$
 */
package org.jnode.driver.net.lance;

import org.jnode.system.IOResource;

/**
 * @author Chris Cole
 *
 */
public abstract class IOAccess {
	protected IOResource io;
	protected int iobase;
	
	public IOAccess(IOResource io, int iobase) {
		this.io = io;
		this.iobase = iobase;
	}
	
	public abstract String getType();

	/**
	 * Reset the device.
	 * 
	 */
	public abstract void reset();

	/**
	 * Gets the contents of a Control and Status Register.
	 * 
	 * @param csrnr
	 */
	public abstract int getCSR(int csrnr);

	/**
	 * Sets the contents of a Control and Status Register.
	 * 
	 * @param csrnr
	 */
	public abstract void setCSR(int csrnr, int value);

	/**
	 * Gets the contents of a Bus Configuration Register.
	 * 
	 * @param bcrnr
	 */
	public abstract int getBCR(int bcrnr);

	/**
	 * Sets the contents of a Bus Configuration Register.
	 * 
	 * @param bcrnr
	 * @param value
	 */
	public abstract void setBCR(int bcrnr, int value);

}
