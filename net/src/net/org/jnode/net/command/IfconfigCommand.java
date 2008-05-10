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
 
package org.jnode.net.command;

import java.io.InputStream;
import java.io.PrintStream;

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
import org.jnode.shell.CommandLine;
import org.jnode.shell.syntax.*;

/**
 * This command class binds IP addresses to network devices, and displays bindings.
 * 
 * @author epr
 * @author crawley@jnode.org
 */
public class IfconfigCommand extends AbstractCommand {
    // FIXME should support IPv6 and other address families.

	private final DeviceArgument ARG_DEVICE = 
	    new DeviceArgument("device", Argument.OPTIONAL, "the device", NetDeviceAPI.class);
	
	private final IPv4AddressArgument ARG_IP_ADDRESS = 
	    new IPv4AddressArgument("ipAddress", Argument.OPTIONAL, "the IPv4 address to bind the device to");
	
	private final IPv4AddressArgument ARG_SUBNET_MASK =
	    new IPv4AddressArgument("subnetMask", Argument.OPTIONAL, "the IPv4 subnet mask for the device");

	
	public IfconfigCommand() {
	    super("List or manage network interface bindings");
	    registerArguments(ARG_DEVICE, ARG_IP_ADDRESS, ARG_SUBNET_MASK);
	}

	public static void main(String[] args) throws Exception {
		new IfconfigCommand().execute(args);
	}

	public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) 
	throws NameNotFoundException, ApiNotFoundException, NetworkException {
		if (!ARG_DEVICE.isSet()) {
		    // Print MAC address, MTU and IP address(es) for all network devices.
			final DeviceManager dm = (DeviceManager) InitialNaming.lookup(DeviceManager.NAME);
			for (Device dev : dm.getDevicesByAPI(NetDeviceAPI.class)) {
				final NetDeviceAPI api = (NetDeviceAPI) dev.getAPI(NetDeviceAPI.class);
				out.println(dev.getId() + ": MAC-Address " + api.getAddress() + " MTU " + api.getMTU());
				out.println("    " + api.getProtocolAddressInfo(EthernetConstants.ETH_P_IP));
			}
		} 
		else {
			final Device dev = ARG_DEVICE.getValue();
			final NetDeviceAPI api = (NetDeviceAPI) dev.getAPI(NetDeviceAPI.class);

			if (!ARG_IP_ADDRESS.isSet()) {
				// Print IP address(es) for device
				out.println("IP address(es) for " + dev.getId() + 
				        " " + api.getProtocolAddressInfo(EthernetConstants.ETH_P_IP));
			} 
			else {
				// Set IP address for device
				final IPv4Address ip = ARG_IP_ADDRESS.getValue();
				final IPv4Address mask = ARG_SUBNET_MASK.getValue();
				final IPv4ConfigurationService cfg = (IPv4ConfigurationService) 
				        InitialNaming.lookup(IPv4ConfigurationService.NAME);
				cfg.configureDeviceStatic(dev, ip, mask, true);
				
				// FIXME ... this doesn't show the device's new address because the
				// IPv4 ConfigurationServiceImpl calls processor.apply with the 
				// waitUntilReady parameter == false.  (The comment in the code
				// talks about avoiding deadlocks.)
				out.println("IP address for " + dev.getId() + " set to " + 
				        api.getProtocolAddressInfo(EthernetConstants.ETH_P_IP));
			}
		}
	}
}
