/*
 * $Id$
 */
package org.jnode.net.util;

import javax.naming.NameNotFoundException;

import org.jnode.driver.net.NetworkException;
import org.jnode.naming.InitialNaming;
import org.jnode.net.NetworkLayerManager;
import org.jnode.net.SocketBuffer;

/**
 * Utility class for network devices
 * @author epr
 */
public class NetUtils {
	
	/**
	 * A packet has just been received, send it to the packet-type-manager.
	 * @param skbuf
	 */
	public static void sendToPTM(SocketBuffer skbuf)
	throws NetworkException {
		final NetworkLayerManager ptm = getNLM();
		ptm.receive(skbuf);
	}
	
	/**
	 * Gets the packet-type-manager
	 */
	public static NetworkLayerManager getNLM() 
	throws NetworkException {
		try {
			return (NetworkLayerManager)InitialNaming.lookup(NetworkLayerManager.NAME);
		} catch (NameNotFoundException ex) {
			throw new NetworkException("Cannot find NetworkLayerManager", ex);
		}
	}

}
