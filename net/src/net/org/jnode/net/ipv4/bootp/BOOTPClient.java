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
 
package org.jnode.net.ipv4.bootp;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.naming.NameNotFoundException;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.net.NetDeviceAPI;
import org.jnode.driver.net.NetworkException;
import org.jnode.naming.InitialNaming;
import org.jnode.net.NetPermission;
import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.ipv4.config.IPv4ConfigurationService;

/**
 * @author epr
 * @author markhale
 * @author Martin Husted Hartvig (hagar@jnode.org)
 */
public class BOOTPClient extends AbstractBOOTPClient {

    private Device device;
    private NetDeviceAPI api;

    /**
     * Configure the given device using BOOTP
     *
     * @param device
     */
    public final void configureDevice(final Device device) throws IOException {
        this.device = device;

        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new NetPermission("bootpClient"));
        }

        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                public Object run() throws IOException {
                    // Get the API.
                    try {
                        api = device.getAPI(NetDeviceAPI.class);
                    } catch (ApiNotFoundException ex) {
                        throw new NetworkException("Device is not a network device", ex);
                    }

                    configureDevice(device.getId(), api.getAddress());

                    return null;
                }
            });
        } catch (PrivilegedActionException ex) {
            throw (IOException) ex.getException();
        }

        this.api = null;
        this.device = null;
    }

    /**
     * Performs the actual configuration of a network device based on the
     * settings in a BOOTP header.
     */
    protected void doConfigure(BOOTPHeader hdr) throws IOException {
        super.doConfigure(hdr);

        final IPv4ConfigurationService cfg;
        try {
            cfg = InitialNaming.lookup(IPv4ConfigurationService.NAME);
        } catch (NameNotFoundException ex) {
            throw new NetworkException(ex);
        }
        cfg.configureDeviceStatic(device, new IPv4Address(hdr.getYourIPAddress()), null, false);

        final IPv4Address serverAddr = new IPv4Address(hdr.getServerIPAddress());
        final IPv4Address networkAddress = serverAddr.and(serverAddr.getDefaultSubnetmask());

        if (hdr.getGatewayIPAddress().isAnyLocalAddress()) {
            cfg.addRoute(serverAddr, null, device, false);
            cfg.addRoute(networkAddress, null, device, false);
        } else {
            cfg.addRoute(networkAddress, new IPv4Address(hdr.getGatewayIPAddress()), device, false);
        }
    }
}
