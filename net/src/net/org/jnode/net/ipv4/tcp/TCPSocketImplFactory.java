/*
 * $Id$
 */
package org.jnode.net.ipv4.tcp;

import java.net.SocketImpl;
import java.net.SocketImplFactory;

/**
 * @author epr
 */
public class TCPSocketImplFactory implements SocketImplFactory {

	/** The protocol I'm using */
	private final TCPProtocol protocol;
	
	/**
	 * Initialize a new instance
	 * @param protocol
	 */
	public TCPSocketImplFactory(TCPProtocol protocol) {
		this.protocol = protocol;
	}

	/**
	 * @see java.net.SocketImplFactory#createSocketImpl()
	 */
	public SocketImpl createSocketImpl() {
		return new TCPSocketImpl(protocol);
	}

}
