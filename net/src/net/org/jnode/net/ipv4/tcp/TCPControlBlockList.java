/*
 * $Id$
 */
package org.jnode.net.ipv4.tcp;

import java.util.Iterator;

import org.jnode.net.ipv4.IPv4ControlBlock;
import org.jnode.net.ipv4.IPv4ControlBlockList;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class TCPControlBlockList extends IPv4ControlBlockList {

	/** The protocol implementation */
	private final TCPProtocol protocol;
	
	/** Last initial sequence number */
	private int isn;
	
	/**
	 * Create a new instance
	 * @param protocol
	 */
	public TCPControlBlockList(TCPProtocol protocol) {
		this.protocol = protocol;
	}

	/**
	 * @see org.jnode.net.ipv4.IPv4ControlBlockList#createControlBlock(org.jnode.net.ipv4.IPv4ControlBlock)
	 */
	protected IPv4ControlBlock createControlBlock(IPv4ControlBlock parent) {
		return new TCPControlBlock(this, (TCPControlBlock)parent, protocol, isn++);
	}
	
	/**
	 * Process timeout handling
	 */
	public void timeout() {
		for (Iterator i = iterator(); i.hasNext(); ) {
			final TCPControlBlock cb = (TCPControlBlock)i.next();
			cb.timeout();
		}
	}
}
