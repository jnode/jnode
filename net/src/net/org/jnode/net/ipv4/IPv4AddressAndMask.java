/*
 * $Id$
 */
package org.jnode.net.ipv4;

/**
 * @author epr
 */
public class IPv4AddressAndMask {

	private IPv4Address address;
	private IPv4Address subnetMask;
	
	/**
	 * Create a new instance
	 */
	public IPv4AddressAndMask() {
	}

	/**
	 * Create a new instance
	 */
	public IPv4AddressAndMask(IPv4Address address, IPv4Address subnetMask) {
		this.address = address;
		this.subnetMask = subnetMask;
	}

	/**
	 * Gets the address
	 */
	public IPv4Address getAddress() {
		return address;
	}

	/**
	 * Gets the subnet mask
	 */
	public IPv4Address getSubnetMask() {
		return subnetMask;
	}

	/**
	 * @param address
	 */
	public void setAddress(IPv4Address address) {
		this.address = address;
	}

	/**
	 * @param address
	 */
	public void setSubnetMask(IPv4Address address) {
		subnetMask = address;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return address + " mask:" + subnetMask;
	}
}
