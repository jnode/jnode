/*
 * $Id$
 */
package org.jnode.driver;

/**
 * The device has already been registered exception.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class DeviceAlreadyRegisteredException extends DeviceException {

	/**
	 * 
	 */
	public DeviceAlreadyRegisteredException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DeviceAlreadyRegisteredException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public DeviceAlreadyRegisteredException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param s
	 */
	public DeviceAlreadyRegisteredException(String s) {
		super(s);
	}
}
