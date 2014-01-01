/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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
 
package org.jnode.command.net;

import java.io.PrintWriter;

import javax.naming.NameNotFoundException;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.net.NetDeviceAPI;
import org.jnode.driver.net.NetworkException;
import org.jnode.naming.InitialNaming;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.ipv4.config.IPv4ConfigurationService;
import org.jnode.net.syntax.IPv4AddressArgument;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.DeviceArgument;

/**
 * This command class binds IP addresses to network devices, and displays bindings.
 * 
 * @author epr
 * @author crawley@jnode.org
 */
public class IfconfigCommand extends AbstractCommand {
    // FIXME should support IPv6 and other address families.

    private static final String help_device = "the device";
    private static final String help_ip     = "the IPv4 address to bind the device to";
    private static final String help_subnet  = "the IPv4 subnet mask for the device";
    private static final String help_super  = "List or manage network interface bindings";
    private static final String fmt_devices = "%s: MAC-Address %s MTU %s%n    %s%n";
    private static final String fmt_ip      = "IP address(es) for %s %s%n";
    private static final String fmt_set_ip  = "IP Address for %s set to %s%n";
    
    private final DeviceArgument argDevice;
    private final IPv4AddressArgument argIPAddress;
    private final IPv4AddressArgument argSubnetMask;


    public IfconfigCommand() {
        super(help_super);
        argDevice     = new DeviceArgument("device", Argument.OPTIONAL, help_device, NetDeviceAPI.class);
        argIPAddress  = new IPv4AddressArgument("ipAddress", Argument.OPTIONAL, help_ip);
        argSubnetMask = new IPv4AddressArgument("subnetMask", Argument.OPTIONAL, help_subnet);
        registerArguments(argDevice, argIPAddress, argSubnetMask);
    }

    public static void main(String[] args) throws Exception {
        new IfconfigCommand().execute(args);
    }
    
    public void execute() throws NameNotFoundException, ApiNotFoundException, NetworkException {
        PrintWriter out = getOutput().getPrintWriter();
        if (!argDevice.isSet()) {
            // Print MAC address, MTU and IP address(es) for all network devices.
            final DeviceManager dm = InitialNaming.lookup(DeviceManager.NAME);
            for (Device dev : dm.getDevicesByAPI(NetDeviceAPI.class)) {
                final NetDeviceAPI api = dev.getAPI(NetDeviceAPI.class);
                out.format(fmt_devices, dev.getId(), api.getAddress(), api.getMTU(),
                           api.getProtocolAddressInfo(EthernetConstants.ETH_P_IP));
            }
        } else {
            final Device dev = argDevice.getValue();
            final NetDeviceAPI api = dev.getAPI(NetDeviceAPI.class);

            if (!argIPAddress.isSet()) {
                // Print IP address(es) for device
                out.format(fmt_ip, dev.getId(), api.getProtocolAddressInfo(EthernetConstants.ETH_P_IP));
            } else {
                // Set IP address for device
                final IPv4Address ip = argIPAddress.getValue();
                final IPv4Address mask = argSubnetMask.getValue();
                final IPv4ConfigurationService cfg = InitialNaming.lookup(IPv4ConfigurationService.NAME);
                cfg.configureDeviceStatic(dev, ip, mask, true);

                // FIXME ... this doesn't show the device's new address because the
                // IPv4 ConfigurationServiceImpl calls processor.apply with the 
                // waitUntilReady parameter == false.  (The comment in the code
                // talks about avoiding deadlocks.)
                out.format(fmt_set_ip, dev.getId(), api.getProtocolAddressInfo(EthernetConstants.ETH_P_IP));
            }
        }
    }
}
