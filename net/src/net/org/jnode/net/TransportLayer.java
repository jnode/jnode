/*
 * $Id$
 */
package org.jnode.net;

import java.net.DatagramSocketImplFactory;
import java.net.SocketException;
import java.net.SocketImplFactory;

import org.jnode.util.Statistics;

/**
 * OSI transport layers must implement this interface.
 * 
 * @author epr
 */
public interface TransportLayer {

	/**
	 * Gets the name of this type
	 */
	public String getName();
	
	/**
	 * Gets the protocol ID this layer handles
	 */
	public int getProtocolID();
		
	/**
	 * Gets the statistics of this protocol
	 */
	public Statistics getStatistics();	
	
	/**
	 * Process a packet that has been received and matches getType()
	 * @param skbuf
	 * @throws SocketException
	 */
	public void receive(SocketBuffer skbuf)
	throws SocketException;

	/**
	 * Gets the SocketImplFactory of this protocol.
	 * @throws SocketException If this protocol is not Socket based.
	 */
	public SocketImplFactory getSocketImplFactory()
	throws SocketException;

	/**
	 * Gets the DatagramSocketImplFactory of this protocol.
	 * @throws SocketException If this protocol is not DatagramSocket based.
	 */
	public DatagramSocketImplFactory getDatagramSocketImplFactory()
	throws SocketException;
}
