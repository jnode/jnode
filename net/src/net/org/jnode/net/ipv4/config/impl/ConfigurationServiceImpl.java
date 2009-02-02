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
 
package org.jnode.net.ipv4.config.impl;

import org.jnode.driver.Device;
import org.jnode.driver.net.NetworkException;
import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.ipv4.config.IPv4ConfigurationService;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ConfigurationServiceImpl implements IPv4ConfigurationService {

    private final NetConfigurationData config;
    private final ConfigurationProcessor processor;

    /**
     * Initialize this instance.
     * @param config
     */
    public ConfigurationServiceImpl(ConfigurationProcessor processor, NetConfigurationData config) {
        this.processor = processor;
        this.config = config;
    }

    /**
     * Set a static configuration for the given device.
     * @param device
     * @param address
     * @param netmask
     */
    public void configureDeviceStatic(Device device, IPv4Address address, IPv4Address netmask,
            boolean persistent) throws NetworkException {
        final NetStaticDeviceConfig cfg = new NetStaticDeviceConfig(address, netmask);
        if (persistent) {
            config.setConfiguration(device, cfg);
        }
        processor.apply(device, cfg, false /* no bug, done to avoid deadlocks */);
    }

    /**
     * Configure the device using BOOTP.
     * @param device
     * @param persistent
     * @throws NetworkException
     */
    public void configureDeviceBootp(Device device, boolean persistent) throws NetworkException {
        final NetBootpDeviceConfig cfg = new NetBootpDeviceConfig();
        if (persistent) {
            config.setConfiguration(device, cfg);
        }
        processor.apply(device, cfg, true);
    }

    /**
     * Configure the device using DHCP.
     * @param device
     * @param persistent
     * @throws NetworkException
     */
    public void configureDeviceDhcp(Device device, boolean persistent) throws NetworkException {
        final NetDhcpConfig cfg = new NetDhcpConfig();
        if (persistent) {
            config.setConfiguration(device, cfg);
        }
        processor.apply(device, cfg, true);
    }

    /**
     * @see org.jnode.net.ipv4.config.IPv4ConfigurationService#addRoute(org.jnode.net.ipv4.IPv4Address, 
     *      org.jnode.net.ipv4.IPv4Address, org.jnode.driver.Device, boolean)
     */
    public void addRoute(IPv4Address target, IPv4Address gateway, Device device, boolean persistent)
        throws NetworkException {
        Route.addRoute(target, gateway, device);
    }

    /**
     * @see org.jnode.net.ipv4.config.IPv4ConfigurationService#deleteRoute(org.jnode.net.ipv4.IPv4Address,
     *      org.jnode.net.ipv4.IPv4Address, org.jnode.driver.Device)
     */
    public void deleteRoute(IPv4Address target, IPv4Address gateway, Device device)
        throws NetworkException {
        Route.delRoute(target, gateway, device);
    }
}
