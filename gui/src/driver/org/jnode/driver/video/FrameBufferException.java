/*
 * $Id$
 */
package org.jnode.driver.video;

/**
 * @author epr
 */
public class FrameBufferException extends Exception {

	/**
	 * 
	 */
	public FrameBufferException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public FrameBufferException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public FrameBufferException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param s
	 */
	public FrameBufferException(String s) {
		super(s);
	}
}
