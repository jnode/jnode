/*
 * $Id$
 */
package org.jnode.net;

import java.net.UnknownHostException;

/**
 * @author epr
 */
public interface Resolver {
	
	/**
	 * Gets the address(es) of the given hostname.
	 * @param hostname
	 * @return All addresses of the given hostname. The returned array is at least 1 address long.
	 * @throws UnknownHostException
	 */
	public ProtocolAddress[] getByName(String hostname)
	throws UnknownHostException;

	/**
	 * Gets the hostname of the given address.
	 * @param address
	 * @return All hostnames of the given hostname. The returned array is at least 1 hostname long.
	 * @throws UnknownHostException
	 */
	public String[] getByAddress(ProtocolAddress address)
	throws UnknownHostException;	
	

}
