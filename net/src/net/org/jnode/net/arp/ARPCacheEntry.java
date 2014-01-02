/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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

import org.jnode.net.HardwareAddress;
import org.jnode.net.ProtocolAddress;

/**
 * Entry of the ARP cache
 *
 * @author epr
 */
public class ARPCacheEntry {

    private static final long ARP_CACHE_LIFETIME = 10 * 60 * 1000;

    private final long creationTime;
    private long lifeTime;
    private final HardwareAddress hardwareAddress;
    private final ProtocolAddress protocolAddress;
    private final boolean dynamic;

    /**
     * Create a new instance
     *
     * @param hardwareAddress
     * @param protocolAddress
     * @param dynamic
     */
    public ARPCacheEntry(HardwareAddress hardwareAddress, ProtocolAddress protocolAddress, boolean dynamic) {
        this.hardwareAddress = hardwareAddress;
        this.protocolAddress = protocolAddress;
        this.creationTime = System.currentTimeMillis();
        // TODO make ARP cache lifetime configurable
        this.lifeTime = ARP_CACHE_LIFETIME;
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
        return ((System.currentTimeMillis() - creationTime) >= lifeTime);
    }

    /**
     * Gets the network address of this entry
     */
    public HardwareAddress getHwAddress() {
        return hardwareAddress;
    }

    /**
     * Gets the protocol address of this entry
     */
    public ProtocolAddress getPAddress() {
        return protocolAddress;
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
     *
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return protocolAddress + " " + hardwareAddress + ' ' + ((dynamic) ? "dynamic" : "static");
    }
}
