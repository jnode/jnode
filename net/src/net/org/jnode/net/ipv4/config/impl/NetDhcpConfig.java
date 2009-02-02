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

import java.io.IOException;
import java.util.prefs.Preferences;

import org.jnode.driver.Device;
import org.jnode.driver.net.NetworkException;
import org.jnode.net.ipv4.dhcp.DHCPClient;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class NetDhcpConfig extends NetDeviceConfig {

    /**
     * @see org.jnode.net.ipv4.config.impl.NetDeviceConfig#apply(org.jnode.driver.Device)
     */
    public void doApply(Device device) throws NetworkException {
        final DHCPClient dhcp = new DHCPClient();
        try {
            dhcp.configureDevice(device);
        } catch (IOException ex) {
            throw new NetworkException(ex);
        }
    }

    /**
     * @see org.jnode.net.ipv4.config.impl.NetDeviceConfig#load(java.util.prefs.Preferences)
     */
    public void load(Preferences prefs) {
        // Do nothing
    }

    /**
     * @see org.jnode.net.ipv4.config.impl.NetDeviceConfig#store(java.util.prefs.Preferences)
     */
    public void store(Preferences prefs) {
        // Do nothing
    }
}
