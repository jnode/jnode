/*
 * $Id$
 */
package org.jnode.driver.net;

import java.net.SocketException;

/**
 * @author epr
 */
public class NetworkException extends SocketException {

	/**
	 * 
	 */
	public NetworkException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public NetworkException(String message, Throwable cause) {
		super(message);
		initCause(cause);
	}

	/**
	 * @param cause
	 */
	public NetworkException(Throwable cause) {
		super();
		initCause(cause);
	}

	/**
	 * @param s
	 */
	public NetworkException(String s) {
		super(s);
	}
}
