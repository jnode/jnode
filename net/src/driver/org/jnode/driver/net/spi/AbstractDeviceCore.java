/*
 * $Id$
 */
package org.jnode.driver.net.spi;

import org.apache.log4j.Logger;
import org.jnode.net.HardwareAddress;
import org.jnode.net.SocketBuffer;
import org.jnode.util.TimeoutException;

/**
 * This abstract class is not intended for any external purpose. It only serves
 * as a voluntary guide for driver implementation of network cards.
 * 
 * @author epr
 */
public abstract class AbstractDeviceCore {
	
	/** My logger */
	protected final Logger log = Logger.getLogger(getClass());

	/**
	 * Gets the hardware address of this device
	 */
	public abstract HardwareAddress getHwAddress();
	
	/**
	 * Initialize the device
	 */
	public abstract void initialize();
	
	/**
	 * Disable the device
	 */
	public abstract void disable();

	/**
	 * Release all resources
	 */
	public abstract void release();

	/**
	 * Transmit the given buffer
	 * @param buf
	 * @param timeout
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	public abstract void transmit(SocketBuffer buf, long timeout)
	throws InterruptedException, TimeoutException;
}
