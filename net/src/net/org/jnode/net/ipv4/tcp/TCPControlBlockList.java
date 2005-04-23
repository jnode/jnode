/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
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
		for (Iterator<IPv4ControlBlock> i = iterator(); i.hasNext(); ) {
			final TCPControlBlock cb = (TCPControlBlock)i.next();
			cb.timeout();
		}
	}
}
