/*
 * $Id$
 */
package org.jnode.net;

import java.net.InetAddress;
import java.util.Set;

/**
 * Each network device can be configured to hold address information for a
 * specific protocol. Objects that hold this information, must implement this
 * interface.
 * 
 * @author epr
 */
public interface ProtocolAddressInfo {

	/**
	 * Is the given address one of the addresses of this object?
	 * @param address
	 */
	public boolean contains(ProtocolAddress address);
	
	/**
	 * Is the given address one of the addresses of this object?
	 * @param address
	 */
	public boolean contains(InetAddress address);
	
	/**
	 * Gets the default protocol address
	 */
	public ProtocolAddress getDefaultAddress();

	/**
	 * Gets a collection of all protocol addresses of this interface.
	 * @return A Set of ProtocolAddress instances
	 */
	public Set addresses();
}
