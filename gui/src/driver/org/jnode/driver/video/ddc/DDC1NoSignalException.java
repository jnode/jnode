/*
 * $Id$
 */
package org.jnode.driver.video.ddc;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class DDC1NoSignalException extends DDC1Exception {

	/**
	 * 
	 */
	public DDC1NoSignalException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DDC1NoSignalException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public DDC1NoSignalException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param s
	 */
	public DDC1NoSignalException(String s) {
		super(s);
	}

}
