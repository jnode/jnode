/*
 * $Id$
 */
package org.jnode.net.ipv4;

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
	private IPv4Address defaultAddress;
	
	private static final IPv4Address DEFAULT_SUBNET_MASK = new IPv4Address("255.255.255.0");

	/**
	 * Create a new instance
	 * @param address
	 * @param subnetMask
	 */	
	public IPv4ProtocolAddressInfo(IPv4Address address, IPv4Address subnetMask) {
		add(address, subnetMask);
		this.defaultAddress = address;
	}
	
	/**
	 * Add an IP address + subnet mask
	 * @param address
	 * @param subnetMask
	 */
	public synchronized void add(IPv4Address address, IPv4Address subnetMask) {
		final IPv4AddressAndMask aam = new IPv4AddressAndMask(address, subnetMask);
		addresses.remove(address);
		addresses.put(address, aam);
	}
	
	/**
	 * Is the given address one of the addresses of this object?
	 * @param address
	 */
	public boolean contains(IPv4Address address) {
		return addresses.containsKey(address);
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
		final IPv4AddressAndMask aam = (IPv4AddressAndMask)addresses.get(address);
		if (aam != null) {
			return aam.getSubnetMask();
		} else {
			return DEFAULT_SUBNET_MASK;
		}
	}
	
	/**
	 * Gets the default protocol address
	 */
	public ProtocolAddress getDefaultAddress() {
		return defaultAddress;
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
	public void setDefaultAddress(IPv4Address address) {
		defaultAddress = address;
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
