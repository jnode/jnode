/*
 * $Id$
 */
package org.jnode.driver.video;

/**
 * @author epr
 */
public class AlreadyOpenException extends FrameBufferException {

	/**
	 * 
	 */
	public AlreadyOpenException() {
		super();
	}

	/**
	 * @param s
	 */
	public AlreadyOpenException(String s) {
		super(s);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public AlreadyOpenException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public AlreadyOpenException(Throwable cause) {
		super(cause);
	}
}
