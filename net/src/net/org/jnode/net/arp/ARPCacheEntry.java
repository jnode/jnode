/*
 * $Id$
 */
package org.jnode.net.arp;

import org.jnode.net.HardwareAddress;
import org.jnode.net.ProtocolAddress;

/**
 * Entry of the ARP cache
 * @author epr
 */
public class ARPCacheEntry {
	
	private final long creationTime;
	private final HardwareAddress hwAddress;
	private final ProtocolAddress pAddress;
	private final boolean dynamic;
	
	/**
	 * Create a new instance
	 * @param hwAddress
	 * @param pAddress
	 * @param dynamic
	 */
	public ARPCacheEntry(HardwareAddress hwAddress, ProtocolAddress pAddress, boolean dynamic) {
		this.hwAddress = hwAddress;
		this.pAddress = pAddress;
		this.creationTime = System.currentTimeMillis();
		this.dynamic = dynamic;
	}

	/**
	 * Gets the creation time of this entry
	 */
	public long getCreationTime() {
		return creationTime;
	}
	
	/**
	 * Is this entry expired?
	 */
	public boolean isExpired() {
		final long age = (System.currentTimeMillis() - creationTime);
		// TODO make ARP cache lifetime configurable
		return (age >= 10*60*1000);
	}

	/**
	 * Gets the network address of this entry
	 */
	public HardwareAddress getHwAddress() {
		return hwAddress;
	}

	/**
	 * Gets the protocol address of this entry
	 */
	public ProtocolAddress getPAddress() {
		return pAddress;
	}

	/**
	 * Is this a dynamic entry?
	 */
	public boolean isDynamic() {
		return dynamic;
	}

	/**
	 * Is this a static entry?
	 */
	public boolean isStatic() {
		return !dynamic;
	}

	/**
	 * Convert to a String representation
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return pAddress + " " + hwAddress + " " + ((dynamic) ? "dynamic" : "static");
	}
}
