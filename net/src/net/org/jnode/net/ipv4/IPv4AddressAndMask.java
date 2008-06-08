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
