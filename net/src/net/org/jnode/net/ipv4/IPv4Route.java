/*
 * $Id$
 *
 * JNode.org
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

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.net.NetDeviceAPI;

/**
 * @author epr
 */
public class IPv4Route implements IPv4Constants {

    private final IPv4Address destination;
    private final IPv4Address subnetmask;
    private final IPv4Address gateway;
    private final Device device;
    private int flags;
    private final NetDeviceAPI deviceAPI;
    private int useCount;

    /**
     * Create a new instance
     * 
     * @param destination
     * @param device
     * @throws IllegalArgumentException If the device is not a network device.
     */
    public IPv4Route(IPv4Address destination, Device device) throws IllegalArgumentException {
        this(destination, null, null, device);
    }

    /**
     * Create a new instance
     * 
     * @param destination
     * @param device
     * @throws IllegalArgumentException If the device is not a network device.
     */
    public IPv4Route(IPv4Address destination, IPv4Address subnetmask, Device device)
        throws IllegalArgumentException {
        this(destination, subnetmask, null, device);
    }

    /**
     * Create a new instance
     * 
     * @param destination
     * @param gateway
     * @param device
     * @throws IllegalArgumentException If the device is not a network device.
     */
    public IPv4Route(IPv4Address destination, IPv4Address subnetmask, IPv4Address gateway,
            Device device) throws IllegalArgumentException {
        this.destination = destination;
        if (subnetmask == null) {
            this.subnetmask = destination.getDefaultSubnetmask();
        } else {
            this.subnetmask = subnetmask;
        }
        this.gateway = gateway;
        this.device = device;
        this.flags = RTF_UP;
        if (gateway != null) {
            this.flags |= RTF_GATEWAY;
        }
        try {
            this.deviceAPI = device.getAPI(NetDeviceAPI.class);
        } catch (ApiNotFoundException ex) {
            throw new IllegalArgumentException("Device " + device.getId() +
                    " is not a network device");
        }
    }

    /**
     * Is this route up?
     * 
     * @return True if this route is up, false otherwise
     */
    public boolean isUp() {
        return ((flags & RTF_UP) != 0);
    }

    /**
     * Is the destination address a host address?
     * 
     * @return True if the destination is a host address, false otherwise
     * @see #isNetwork()
     */
    public boolean isHost() {
        return destination.isHost();
    }

    /**
     * Is the destination address a network address?
     * 
     * @return True if the destination is a network address, false otherwise
     * @see #isHost()
     */
    public boolean isNetwork() {
        return destination.isNetwork();
    }

    /**
     * Is this an indirect route?
     */
    public boolean isGateway() {
        return ((flags & RTF_GATEWAY) != 0);
    }

    /**
     * Set the gateway flag
     * 
     * @param on
     * @throws IllegalArgumentException If on is true and no gateway address is
     *             set.
     */
    public void setGateway(boolean on) throws IllegalArgumentException {
        if (on) {
            if (gateway == null) {
                throw new IllegalArgumentException("Cannot set gateway without a gateway address");
            } else {
                flags |= RTF_GATEWAY;
            }
        } else {
            flags &= ~RTF_GATEWAY;
        }
    }

    /**
     * Set the up flag
     * 
     * @param on
     */
    public void setUp(boolean on) {
        if (on) {
            flags |= RTF_UP;
        } else {
            flags &= ~RTF_UP;
        }
    }

    /**
     * Gets the destination address of this route
     */
    public IPv4Address getDestination() {
        return destination;
    }

    /**
     * Gets the device of this route
     */
    public Device getDevice() {
        return device;
    }

    /**
     * Gets the NetDeviceAPI of the device of this route
     */
    public NetDeviceAPI getDeviceAPI() {
        return deviceAPI;
    }

    /**
     * Gets the netmask of the destination address
     */
    public IPv4Address getSubnetmask() {
        return subnetmask;
    }

    /**
     * Gets the gateway address.
     * 
     * @return The gateway address, or null is <code>isGateway</code> returns
     *         false
     */
    public IPv4Address getGateway() {
        return gateway;
    }

    /**
     * Convert to a String representation
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        final StringBuilder b = new StringBuilder();
        b.append(destination);
        b.append(" - ");
        b.append(subnetmask);
        b.append(" - ");
        b.append(gateway);
        b.append(" - ");
        if (isUp()) {
            b.append('U');
        }
        if (isGateway()) {
            b.append('G');
        }
        if (isHost()) {
            b.append('H');
        }
        b.append(" - ");
        b.append(useCount);
        b.append(" - ");
        b.append(device.getId());
        return b.toString();
    }

    /**
     * Gets the number of times this route has been used
     */
    public int getUseCount() {
        return useCount;
    }

    /**
     * Increment the number of times this route has been used
     */
    public void incUseCount() {
        useCount++;
    }

}
