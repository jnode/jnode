/*
 * $Id$
 */
package org.jnode.driver.video.ddc;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class DDC1Exception extends Exception {

	/**
	 * 
	 */
	public DDC1Exception() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DDC1Exception(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public DDC1Exception(Throwable cause) {
		super(cause);
	}

	/**
	 * @param s
	 */
	public DDC1Exception(String s) {
		super(s);
	}

}
