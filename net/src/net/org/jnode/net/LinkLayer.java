/*
 * $Id$
 */
package org.jnode.net;

import java.net.SocketException;

/**
 * OSI datalink layers must implement this interface.
 *  
 * @author epr
 */
public interface LinkLayer {
	
	/**
	 * Gets the name of this layer
	 */
	public String getName();
	
	/**
	 * Gets the type of frames this layer handles
	 */
	public int getType();
	
	/**
	 * Process a packet that has been received and matches the type of this
	 * layer.
	 * @param skbuf
	 * @throws SocketException
	 */
	public void receive(SocketBuffer skbuf)
	throws SocketException;

}
