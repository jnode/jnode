/*
 * $Id$
 */
package org.jnode.driver;

/**
 * Generic exception of devices in the device framework.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class DeviceException extends Exception {

	/**
	 * 
	 */
	public DeviceException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DeviceException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public DeviceException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param s
	 */
	public DeviceException(String s) {
		super(s);
	}
}
