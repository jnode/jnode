/*
 * $Id$
 */
package org.jnode.net.ipv4.udp;

import java.net.DatagramSocketImpl;
import java.net.DatagramSocketImplFactory;

/**
 * @author epr
 */
public class UDPDatagramSocketImplFactory implements DatagramSocketImplFactory {

	private final UDPProtocol protocol;
	
	/**
	 * Create a new instance
	 * @param protocol
	 */
	public UDPDatagramSocketImplFactory(UDPProtocol protocol) {
		this.protocol = protocol;
	}

	/**
	 * @see java.net.DatagramSocketImplFactory#createDatagramSocketImpl()
	 */
	public DatagramSocketImpl createDatagramSocketImpl() {
		return new UDPDatagramSocketImpl(protocol);
	}

}
