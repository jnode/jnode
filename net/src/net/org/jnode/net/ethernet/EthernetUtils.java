/*
 * $Id$
 */
package org.jnode.net.ethernet;

import org.jnode.driver.net.NetworkException;

/**
 * @author epr
 */
public class EthernetUtils implements EthernetConstants {
	
	/**
	 * Gets the procotol info from the ethernet frame in skbuf
	 * @param hdr
	 * @throws NetworkException The protocol cannot be found
	 */
	public static int getProtocol(EthernetHeader hdr) 
	throws NetworkException {
		final int length = hdr.getLengthType();
		if (length < ETH_FRAME_LEN) {
			// It is a length field
			return EthernetConstants.ETH_P_802_2;
		} else {
			// It is a protocol ID
			return length;
		}
	}
}
