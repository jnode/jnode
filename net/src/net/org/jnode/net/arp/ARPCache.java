/*
 * $Id$
 */
package org.jnode.net.arp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.jnode.net.HardwareAddress;
import org.jnode.net.ProtocolAddress;

/**
 * Cache of ARP entries
 * @author epr
 */
public class ARPCache {

	/** My logger */
	private final Logger log = Logger.getLogger(getClass());
	private final HashMap hw2p = new HashMap();
	private final HashMap p2hw = new HashMap();
	
	/**
	 * Remove all cached entries
	 */
	public synchronized void clear() {
		hw2p.clear();
		p2hw.clear();
	}
	
	/**
	 * Update/Add an extry to the cache 
	 * @param hwAddress
	 * @param pAddress
	 */
	public synchronized void set(HardwareAddress hwAddress, ProtocolAddress pAddress, boolean dynamic) {
		final ARPCacheEntry entry = new ARPCacheEntry(hwAddress, pAddress, dynamic);
		hw2p.put(hwAddress, entry);
		p2hw.put(pAddress, entry);
		//log.debug("Adding ARP cache " + hwAddress + " - " + pAddress);
		notifyAll();
	}
	
	/**
	 * Gets the cached netword address for the given protocol address, or null
	 * if not found.
	 * @param pAddress
	 */
	public synchronized HardwareAddress get(ProtocolAddress pAddress) {
		final ARPCacheEntry entry = (ARPCacheEntry)p2hw.get(pAddress);
		if (entry == null) {
			return null;
		} 
		if (entry.isExpired()) {
			log.debug("Removing expired ARP entry " + entry);
			p2hw.remove(pAddress);
			if (hw2p.get(entry.getHwAddress()) == entry) {
				hw2p.remove(entry.getHwAddress());
			}
			return null;
		}
		return entry.getHwAddress();
	}

	/**
	 * Gets the cached protocol address for the given netword address, or null
	 * if not found.
	 * @param hwAddress
	 */
	public synchronized ProtocolAddress  get(HardwareAddress hwAddress) {
		final ARPCacheEntry entry = (ARPCacheEntry)hw2p.get(hwAddress);
		if (entry == null) {
			return null;
		} 
		if (entry.isExpired()) {
			hw2p.remove(hwAddress);
			if (p2hw.get(entry.getPAddress()) == entry) {
				p2hw.remove(entry.getPAddress());
			}
			return null;
		}
		return entry.getPAddress();
	}

	/**
	 * Return all cache-entries.
	 */
	public synchronized Collection entries() {
		return new ArrayList(hw2p.values());
	}
	
	/**
	 * Wait for any change in the cache
	 */
	public synchronized void waitForChanges(long timeout) {
		try {
			wait(timeout);
		} catch (InterruptedException ex) {
			// Ignore
		}
	}
}
