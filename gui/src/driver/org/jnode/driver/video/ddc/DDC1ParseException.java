/*
 * $Id$
 */
package org.jnode.driver.video.ddc;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class DDC1ParseException extends DDC1Exception {

	/**
	 * 
	 */
	public DDC1ParseException() {
		super();
	}

	/**
	 * @param s
	 */
	public DDC1ParseException(String s) {
		super(s);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DDC1ParseException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public DDC1ParseException(Throwable cause) {
		super(cause);
	}

}
