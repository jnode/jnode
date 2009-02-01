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

import java.util.prefs.Preferences;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.net.NetDeviceAPI;
import org.jnode.driver.net.NetworkException;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.ipv4.IPv4ProtocolAddressInfo;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class NetStaticDeviceConfig extends NetDeviceConfig {

    private IPv4Address address;
    private IPv4Address netmask;

    private static final String ADDRESS_KEY = "address";
    private static final String NETMASK_KEY = "netmask";

    /**
     * Initialize this instance.
     */
    public NetStaticDeviceConfig() {
        this(null, null);
    }

    /**
     * Initialize this instance.
     * 
     * @param address
     * @param netmask
     */
    public NetStaticDeviceConfig(IPv4Address address, IPv4Address netmask) {
        this.address = address;
        this.netmask = netmask;
    }

    /**
     * @throws NetworkException
     * @see org.jnode.net.ipv4.config.impl.NetDeviceConfig#apply(Device)
     */
    public void doApply(Device device) throws NetworkException {
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
     * @see org.jnode.net.ipv4.config.impl.NetDeviceConfig#load(java.util.prefs.Preferences)
     */
    public void load(Preferences prefs) {
        this.address = loadAddress(prefs, ADDRESS_KEY);
        this.netmask = loadAddress(prefs, NETMASK_KEY);
    }

    /**
     * @see org.jnode.net.ipv4.config.impl.NetDeviceConfig#store(java.util.prefs.Preferences)
     */
    public void store(Preferences prefs) {
        storeAddress(prefs, ADDRESS_KEY, address);
        storeAddress(prefs, NETMASK_KEY, netmask);
    }

    /**
     * Load a single address from the given preferences.
     * 
     * @param prefs
     * @param key
     * @return
     */
    private final IPv4Address loadAddress(Preferences prefs, String key) {
        final String addrStr = prefs.get(key, null);
        if (addrStr == null) {
            return null;
        } else {
            return new IPv4Address(addrStr);
        }
    }

    /**
     * Store a single address in the given preferences.
     * 
     * @param prefs
     * @param key
     * @param address
     */
    private final void storeAddress(Preferences prefs, String key, IPv4Address address) {
        if (address != null) {
            prefs.put(key, address.toString());
        } else {
            prefs.remove(key);
        }
    }
}
