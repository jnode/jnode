/*
 * $Id$
 */
package org.jnode.net;

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
	 * Gets the default protocol address
	 */
	public ProtocolAddress getDefaultAddress();
}
