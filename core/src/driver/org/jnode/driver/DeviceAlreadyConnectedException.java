/*
 * $Id$
 */
package org.jnode.driver;

/**
 * The device has already been connected to a driver exception.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class DeviceAlreadyConnectedException extends DriverException {

	/**
	 * 
	 */
	public DeviceAlreadyConnectedException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DeviceAlreadyConnectedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public DeviceAlreadyConnectedException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param s
	 */
	public DeviceAlreadyConnectedException(String s) {
		super(s);
	}
}
