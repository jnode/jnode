/*
 * $Id$
 */
package org.jnode.net;

import java.net.SocketException;

/**
 * @author epr
 */
public class InvalidLayerException extends SocketException {

	/**
	 * 
	 */
	public InvalidLayerException() {
		super();
	}

	/**
	 * @param message
	 */
	public InvalidLayerException(String message) {
		super(message);
	}

}
