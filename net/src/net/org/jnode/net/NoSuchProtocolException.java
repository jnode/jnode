/*
 * $Id$
 */
package org.jnode.net;

import java.net.SocketException;

/**
 * @author epr
 */
public class NoSuchProtocolException extends SocketException {

	/**
	 * 
	 */
	public NoSuchProtocolException() {
		super();
	}

	/**
	 * @param message
	 */
	public NoSuchProtocolException(String message) {
		super(message);
	}
}
