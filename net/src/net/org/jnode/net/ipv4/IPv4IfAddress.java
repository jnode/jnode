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

/**
 * @author JPG
 */
public class IPv4IfAddress {
    private IPv4Address net;
    private IPv4Address address;
    private IPv4Address mask;
    private IPv4Address broadcast;

    public IPv4IfAddress(IPv4Address address, IPv4Address mask) {
        byte[] ip = address.getBytes();
        byte[] subnet_mask = mask.getBytes();

        byte[] bcast = new byte[4];
        byte[] net = new byte[4];

        for (int i = 0; i <= 3; i++) {
            int a = ip[i] & subnet_mask[i];
            int b = ip[i] ^ subnet_mask[i];
            int c = ~b;
            net[i] = (byte) a;
            bcast[i] = (byte) c;
        }

        this.net = new IPv4Address(net, 0);
        this.address = address;
        this.mask = mask;
        this.broadcast = new IPv4Address(bcast, 0);
    }

    /**
     * Tests if the given IP matches this interface
     */
    public boolean matches(IPv4Address other) {
        return (address.equals(other) || broadcast.equals(other));
    }

    /**
     * Returns the network address of this subnet
     */
    public IPv4Address getNetworkAddress() {
        return this.net;
    }

    /**
     * Returns the IP address of this interface
     */
    public IPv4Address getAddress() {
        return this.address;
    }

    /**
     * Returns the subnet mask
     */
    public IPv4Address getSubnetMask() {
        return this.mask;
    }

    /**
     * Returns the broadcast address of this subnet
     */
    public IPv4Address getBroadcast() {
        return this.broadcast;
    }

    public String toString() {
        return address.toString();
    }
}
