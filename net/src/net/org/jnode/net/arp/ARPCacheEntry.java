/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

    private final long creationTime;
    private final HardwareAddress hwAddress;
    private final ProtocolAddress pAddress;
    private final boolean dynamic;

    /**
     * Create a new instance
     * 
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
        return (age >= 10 * 60 * 1000);
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
