/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.net.ipv4;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.jnode.net.ProtocolAddress;
import org.jnode.net.ProtocolAddressInfo;

/**
 * @author epr
 */
public class IPv4ProtocolAddressInfo implements ProtocolAddressInfo {

    /** Mapping between address and address&mask */
    private final HashMap<IPv4Address, IPv4IfAddress> addresses =
            new HashMap<IPv4Address, IPv4IfAddress>();

    /** The default address */
    private IPv4IfAddress defaultAddress;

    /**
     * Create a new instance
     * 
     * @param address
     * @param mask subnetMask
     */

    public IPv4ProtocolAddressInfo(IPv4Address address, IPv4Address mask) {
        this.defaultAddress = add(address, mask);
    }

    /**
     * Add an IP address + subnet mask
     * 
     * @param address
     * @param mask subnetMask
     */
    public synchronized IPv4IfAddress add(IPv4Address address, IPv4Address mask) {
        addresses.remove(address);
        IPv4IfAddress ifAddress = new IPv4IfAddress(address, mask);
        addresses.put(address, ifAddress);
        return ifAddress;
    }

    /**
     * Is the given address one of the addresses of this object?
     * 
     * @param address
     */
    public boolean contains(IPv4Address address) {
        for (IPv4IfAddress ipv4IfAddress : addresses.values()) {
            if (ipv4IfAddress.matches(address)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Is the given address one of the addresses of this object?
     * 
     * @param address
     */
    public boolean contains(InetAddress address) {
        return contains(new IPv4Address(address));
    }

    /**
     * Is the given address one of the addresses of this object?
     * 
     * @param address
     */
    public boolean contains(ProtocolAddress address) {
        if (address instanceof IPv4Address) {
            return contains((IPv4Address) address);
        } else {
            return false;
        }
    }

    /**
     * Gets the subnet mask for a given address
     * 
     * @param address
     */
    public IPv4Address getSubnetMask(IPv4Address address) {
        IPv4IfAddress ifAddr = (IPv4IfAddress) addresses.get(address);
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
     * 
     * @return A Set of IPv4Address instances
     */
    public Set<ProtocolAddress> addresses() {
        return new HashSet<ProtocolAddress>(addresses.keySet());
    }

    /**
     * Sets the default address.
     * 
     * @param address
     */
    // public void setDefaultAddress(IPv4Address address) {
    public void setDefaultAddress(IPv4Address address, IPv4Address netmask) {
        defaultAddress = new IPv4IfAddress(address, netmask);
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        final StringBuilder b = new StringBuilder();
        boolean first = true;
        for (IPv4IfAddress ifa : addresses.values()) {
            if (!first) {
                b.append('\n');
            }
            b.append(ifa);
            first = false;
        }
        return b.toString();
    }
}
