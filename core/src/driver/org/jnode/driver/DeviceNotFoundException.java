/*
 * $Id$
 */
package org.jnode.driver;

/**
 * Device with specific ID has not been found exception.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class DeviceNotFoundException extends DeviceException {

	/**
	 * Initialize this instance. 
	 */
	public DeviceNotFoundException() {
		super();
	}

	/**
	 * Initialize this instance. 
	 * @param message
	 * @param cause
	 */
	public DeviceNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Initialize this instance. 
	 * @param cause
	 */
	public DeviceNotFoundException(Throwable cause) {
		super(cause);
	}

	/**
	 * Initialize this instance. 
	 * @param s
	 */
	public DeviceNotFoundException(String s) {
		super(s);
	}
}
