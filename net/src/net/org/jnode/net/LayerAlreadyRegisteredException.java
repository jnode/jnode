/*
 * $Id$
 */
package org.jnode.net;

import java.net.SocketException;

/**
 * @author epr
 */
public class LayerAlreadyRegisteredException extends SocketException {

	/**
	 * 
	 */
	public LayerAlreadyRegisteredException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public LayerAlreadyRegisteredException(
		String message,
		Throwable cause) {
		super(message);
		initCause(cause);
	}

	/**
	 * @param cause
	 */
	public LayerAlreadyRegisteredException(Throwable cause) {
		super();
		initCause(cause);
	}

	/**
	 * @param s
	 */
	public LayerAlreadyRegisteredException(String s) {
		super(s);
	}
}
