/*
 * $Id$
 */
package org.jnode.net.arp;

import org.jnode.net.ethernet.EthernetConstants;

/**
 * @author epr
 */
public class RARPNetworkLayer extends ARPNetworkLayer {

	/**
	 * Create a new instance
	 */
	public RARPNetworkLayer() {
	}

	/**
	 * @see org.jnode.net.NetworkLayer#getName()
	 */
	public String getName() {
		return "rarp";
	}

	/**
	 * @see org.jnode.net.NetworkLayer#getProtocolID()
	 */
	public int getProtocolID() {
		return EthernetConstants.ETH_P_RARP;
	}

}
