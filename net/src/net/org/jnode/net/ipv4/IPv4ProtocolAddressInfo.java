/*
 * $Id$
 */
package org.jnode.net.ipv4;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.jnode.net.ProtocolAddress;
import org.jnode.net.ProtocolAddressInfo;

/**
 * @author epr
 */
public class IPv4ProtocolAddressInfo implements ProtocolAddressInfo {

	/** Mapping between address and address&mask */
	private final HashMap addresses = new HashMap();
	/** The default address */
	private IPv4IfAddress defaultAddress;
	
	private static final IPv4Address DEFAULT_SUBNET_MASK = new IPv4Address("255.255.255.0");

	/**
	 * Create a new instance
	 * @param address
	 * @param mask subnetMask
	 */

	public IPv4ProtocolAddressInfo(IPv4Address address, IPv4Address mask)
  {
    this.defaultAddress = add(address, mask);
	}
	
	/**
	 * Add an IP address + subnet mask
	 * @param address
	 * @param mask subnetMask
	 */
	public synchronized IPv4IfAddress add(IPv4Address address, IPv4Address mask) {

		addresses.remove(address);
    IPv4IfAddress ifAddress = new IPv4IfAddress(address, mask);
		addresses.put(address,ifAddress);

    return ifAddress;
	}
	
	/**
	 * Is the given address one of the addresses of this object?
	 * @param address
	 */
	public boolean contains(IPv4Address address) {
    Collection c = addresses.values();
    IPv4IfAddress ipv4IfAddress;

    for (Iterator iterator = c.iterator(); iterator.hasNext();)
    {
      ipv4IfAddress = (IPv4IfAddress) iterator.next();
      if (ipv4IfAddress.matches(address))
      {
        return true;
      }
    }

		return false;
	}

	/**
	 * Is the given address one of the addresses of this object?
	 * @param address
	 */
	public boolean contains(InetAddress address) {
	    return contains(new IPv4Address(address));
	}
	
	/**
	 * Is the given address one of the addresses of this object?
	 * @param address
	 */
	public boolean contains(ProtocolAddress address) {
		if (address instanceof IPv4Address) {
			return contains((IPv4Address)address);
		} else {
			return false;
		}
	}
	
	/**
	 * Gets the subnet mask for a given address
	 * @param address
	 */
	public IPv4Address getSubnetMask(IPv4Address address) {
		IPv4IfAddress ifAddr = (IPv4IfAddress)addresses.get(address);
		return ifAddr.getSubnetMask();
	}
	
	/**
	 * Gets the default protocol address
	 */
	public ProtocolAddress getDefaultAddress() {
		return defaultAddress.getAddress();
	}
	
	/**
	 * Gets a collection of all IP address of this interface.
	 * @return A Set of IPv4Address instances
	 */
	public Set addresses() {
		return Collections.unmodifiableSet(addresses.keySet());
	}
	
	/**
	 * Sets the default address.
	 * @param address
	 */
//	public void setDefaultAddress(IPv4Address address) {
	public void setDefaultAddress(IPv4Address address, IPv4Address netmask) {
		defaultAddress = new IPv4IfAddress(address, netmask);
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		final StringBuffer b = new StringBuffer();
		for (Iterator i = addresses.values().iterator(); i.hasNext(); ) {
			b.append(i.next());
			if (i.hasNext()) {
				b.append('\n');
			}
		}
		return b.toString();
	}

}
