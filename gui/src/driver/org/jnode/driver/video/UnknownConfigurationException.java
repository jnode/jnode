/*
 * $Id$
 */
package org.jnode.driver.video;

/**
 * @author epr
 */
public class UnknownConfigurationException extends FrameBufferException {

	/**
	 * 
	 */
	public UnknownConfigurationException() {
		super();
	}

	/**
	 * @param s
	 */
	public UnknownConfigurationException(String s) {
		super(s);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public UnknownConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public UnknownConfigurationException(Throwable cause) {
		super(cause);
	}
}
