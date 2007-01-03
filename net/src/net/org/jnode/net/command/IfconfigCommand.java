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

import org.jnode.driver.Device;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.net.NetDeviceAPI;
import org.jnode.naming.InitialNaming;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.net.help.argument.HostArgument;
import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.ipv4.config.IPv4ConfigurationService;
import org.jnode.shell.Command;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.Syntax;
import org.jnode.shell.help.argument.DeviceArgument;

/**
 * @author epr
 */
public class IfconfigCommand implements Command {

	static final DeviceArgument ARG_DEVICE = new DeviceArgument("device", "the device", NetDeviceAPI.class);
	static final HostArgument ARG_IP_ADDRESS = new HostArgument("ip-address", "the IP address to bind the device to");
	static final HostArgument ARG_SUBNET_MASK = new HostArgument("subnet-mask", "if given, specifies the range of reachable subnets");

	public static Help.Info HELP_INFO = new Help.Info(
		"ifconfig", new Syntax[]{
			new Syntax("Print status of all network devices"),
			new Syntax("Print status of a single network device",
				new Parameter[]{
					new Parameter(ARG_DEVICE, Parameter.MANDATORY),
				}
			),
			new Syntax("Bind a device to an IP address",
				new Parameter[]{
					new Parameter(ARG_DEVICE, Parameter.MANDATORY),
					new Parameter(ARG_IP_ADDRESS, Parameter.MANDATORY),
					new Parameter(ARG_SUBNET_MASK, Parameter.OPTIONAL)
				}
			)
		}
	);

	public static void main(String[] args)
	throws Exception {
		new IfconfigCommand().execute(new CommandLine(args), System.in, System.out, System.err);
	}

	public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) throws Exception {
		ParsedArguments cmdLine = HELP_INFO.parse(commandLine.toStringArray());

		if( cmdLine.size() == 0 ) {
			final DeviceManager dm = (DeviceManager)InitialNaming.lookup(DeviceManager.NAME);
			for (Device dev : dm.getDevicesByAPI(NetDeviceAPI.class)) {
				final NetDeviceAPI api = (NetDeviceAPI)dev.getAPI(NetDeviceAPI.class);
				System.out.println(dev.getId() + ": MAC-Address " + api.getAddress() + " MTU " + api.getMTU());
				System.out.println("    " + api.getProtocolAddressInfo(EthernetConstants.ETH_P_IP));
				System.out.println();
			}
		} else {

			Device dev = ARG_DEVICE.getDevice(cmdLine);
			NetDeviceAPI api = (NetDeviceAPI)dev.getAPI(NetDeviceAPI.class);

			if( cmdLine.size() == 1 ) {
				// Print address
				System.out.println("IP address(es) for " + dev.getId() + " " + api.getProtocolAddressInfo(EthernetConstants.ETH_P_IP));
			} else {
				// Set IP address
				IPv4Address ip = ARG_IP_ADDRESS.getAddress(cmdLine);
				IPv4Address mask = ARG_SUBNET_MASK.getAddress(cmdLine);
				final IPv4ConfigurationService cfg = (IPv4ConfigurationService)InitialNaming.lookup(IPv4ConfigurationService.NAME);
				cfg.configureDeviceStatic(dev, ip, mask, true);
				System.out.println("IP address for " + dev.getId() + " set to " + api.getProtocolAddressInfo(EthernetConstants.ETH_P_IP));
			}
		}
		System.out.println();
	}

}
