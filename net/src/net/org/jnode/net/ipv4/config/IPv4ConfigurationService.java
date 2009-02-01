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
 
package org.jnode.net.ipv4.config;

import org.jnode.driver.Device;
import org.jnode.driver.net.NetworkException;
import org.jnode.net.ipv4.IPv4Address;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface IPv4ConfigurationService {

    /** Name used to register this service in the initial naming namespace */
    public static final Class<IPv4ConfigurationService> NAME = IPv4ConfigurationService.class;

    /**
     * Configure the device using BOOTP.
     * 
     * @param device
     * @param persistent
     * @throws NetworkException
     */
    public void configureDeviceBootp(Device device, boolean persistent) throws NetworkException;

    /**
     * Configure the device using DHCP.
     * 
     * @param device
     * @param persistent
     * @throws NetworkException
     */
    public void configureDeviceDhcp(Device device, boolean persistent) throws NetworkException;

    /**
     * Set a static configuration for the given device.
     * 
     * @param device
     * @param address
     * @param netmask
     * @param persistent
     * @throws NetworkException
     */
    public void configureDeviceStatic(Device device, IPv4Address address, IPv4Address netmask,
            boolean persistent) throws NetworkException;

    /**
     * Add a route
     * 
     * @param target
     * @param gateway
     * @param device
     * @param persistent
     * @throws NetworkException
     */
    public void addRoute(IPv4Address target, IPv4Address gateway, Device device, boolean persistent)
        throws NetworkException;

    /**
     * Delete a route
     * 
     * @param target
     * @param gateway
     * @param device
     * @throws NetworkException
     */
    public void deleteRoute(IPv4Address target, IPv4Address gateway, Device device)
        throws NetworkException;
}
