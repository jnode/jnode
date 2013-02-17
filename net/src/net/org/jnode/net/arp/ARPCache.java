/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.net.arp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jnode.net.HardwareAddress;
import org.jnode.net.ProtocolAddress;

/**
 * Cache of ARP entries
 * 
 * @author epr
 */
public class ARPCache {

    private static final Logger log = Logger.getLogger(ARPCache.class);
    
    private final Map<HardwareAddress, ARPCacheEntry> networkToProtocolAddresses =
            new HashMap<HardwareAddress, ARPCacheEntry>();
    
    private final Map<ProtocolAddress, ARPCacheEntry> protocolToNetworkAddresses =
            new HashMap<ProtocolAddress, ARPCacheEntry>();

    /**
     * Remove all cached entries
     */
    public synchronized void clear() {
        networkToProtocolAddresses.clear();
        protocolToNetworkAddresses.clear();
    }

    /**
     * Update/Add an entry to the cache
     * 
     * @param hardwareAddress Network address
     * @param protocolAddress Protocol address
     */
    public synchronized void set(HardwareAddress hardwareAddress, ProtocolAddress protocolAddress,
            boolean dynamic) {
        final ARPCacheEntry entry = new ARPCacheEntry(hardwareAddress, protocolAddress, dynamic);
        networkToProtocolAddresses.put(hardwareAddress, entry);
        protocolToNetworkAddresses.put(protocolAddress, entry);
        notifyAll();
    }

    /**
     * Gets the cached netword address for the given protocol address, or null
     * if not found.
     * 
     * @param protocolAddress
     */
    public synchronized HardwareAddress get(ProtocolAddress protocolAddress) {
        final ARPCacheEntry entry = protocolToNetworkAddresses.get(protocolAddress);
        if (entry == null) {
            return null;
        }
        if (entry.isExpired()) {
            log.debug("Removing expired ARP entry " + entry);
            protocolToNetworkAddresses.remove(protocolAddress);
            if (networkToProtocolAddresses.get(entry.getHwAddress()) == entry) {
                networkToProtocolAddresses.remove(entry.getHwAddress());
            }
            return null;
        }
        return entry.getHwAddress();
    }

    /**
     * Gets the cached protocol address for the given netword address, or null
     * if not found.
     * 
     * @param hardwareAddress
     */
    public synchronized ProtocolAddress get(HardwareAddress hardwareAddress) {
        final ARPCacheEntry entry = networkToProtocolAddresses.get(hardwareAddress);
        if (entry == null) {
            return null;
        }
        if (entry.isExpired()) {
            networkToProtocolAddresses.remove(hardwareAddress);
            if (protocolToNetworkAddresses.get(entry.getPAddress()) == entry) {
                protocolToNetworkAddresses.remove(entry.getPAddress());
            }
            return null;
        }
        return entry.getPAddress();
    }

    /**
     * Return all cache-entries.
     */
    public synchronized Collection<ARPCacheEntry> entries() {
        return new ArrayList<ARPCacheEntry>(networkToProtocolAddresses.values());
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
