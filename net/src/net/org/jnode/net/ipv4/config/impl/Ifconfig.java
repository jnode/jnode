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
 
package org.jnode.net.ipv4.config.impl;

import java.net.UnknownHostException;
import java.util.Collection;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.net.NetDeviceAPI;
import org.jnode.driver.net.NetworkException;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.ipv4.IPv4ProtocolAddressInfo;

/**
 * Utility class for implementing ifconfig.
 * 
 * @author epr
 */
final class Ifconfig {

    /**
     * Sets the default IP address of a network device
     * 
     * @param device
     * @param address
     * @param netmask
     */
    public static void setDefault(Device device, IPv4Address address, IPv4Address netmask)
        throws NetworkException {
        final NetDeviceAPI api;
        try {
            api = device.getAPI(NetDeviceAPI.class);
        } catch (ApiNotFoundException ex) {
            throw new NetworkException("Device is not a network device", ex);
        }

        if (netmask == null) {
            netmask = address.getDefaultSubnetmask();
        }
        IPv4ProtocolAddressInfo addrInfo =
                (IPv4ProtocolAddressInfo) api.getProtocolAddressInfo(EthernetConstants.ETH_P_IP);
        if (addrInfo == null) {
            addrInfo = new IPv4ProtocolAddressInfo(address, netmask);
            api.setProtocolAddressInfo(EthernetConstants.ETH_P_IP, addrInfo);
        } else {
            addrInfo.add(address, netmask);
            addrInfo.setDefaultAddress(address, netmask);
        }
    }

    /**
     * Gets the default address of the first configured network device.
     * 
     * @return The local address
     * @throws UnknownHostException No local address could be found
     */
    public static IPv4Address getLocalAddress() throws UnknownHostException {
        final Collection<Device> devices = DeviceUtils.getDevicesByAPI(NetDeviceAPI.class);
        for (Device dev : devices) {
            try {
                final NetDeviceAPI api = dev.getAPI(NetDeviceAPI.class);
                final IPv4ProtocolAddressInfo addrInfo =
                        (IPv4ProtocolAddressInfo) api.getProtocolAddressInfo(EthernetConstants.ETH_P_IP);
                if (addrInfo != null) {
                    final IPv4Address addr = (IPv4Address) addrInfo.getDefaultAddress();
                    if (addr != null) {
                        return addr;
                    }
                }
            } catch (ApiNotFoundException ex) {
                // Strange, but ignore
            }
        }
        throw new UnknownHostException("No configured address found");
    }

}
