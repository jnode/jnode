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
import java.net.InetAddress;
import java.net.NoRouteToHostException;

import javax.naming.NameNotFoundException;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceNotFoundException;
import org.jnode.driver.net.NetDeviceAPI;
import org.jnode.naming.InitialNaming;
import org.jnode.net.ProtocolAddressInfo;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.ipv4.IPv4RoutingTable;
import org.jnode.net.ipv4.config.IPv4ConfigurationService;
import org.jnode.net.ipv4.layer.IPv4NetworkLayer;
import org.jnode.net.util.NetUtils;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.SyntaxErrorException;
import org.jnode.shell.help.argument.DeviceArgument;

/**
 * @author markhale
 */
public class DhcpCommand extends AbstractCommand {

        static final DeviceArgument ARG_DEVICE = new DeviceArgument("device", "the device to boot from", NetDeviceAPI.class);

	public static Help.Info HELP_INFO = new Help.Info(
		"dhcp",
		"Try to configure the given device using DHCP",
		new Parameter[]{
			new Parameter(ARG_DEVICE, Parameter.MANDATORY)
		}
	);

	public static void main(String[] args)
	throws Exception {
		new DhcpCommand().execute(args);
	}

	public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) throws Exception {
		ParsedArguments cmdLine = HELP_INFO.parse(commandLine);

		final Device dev = ARG_DEVICE.getDevice(cmdLine);
		
		// The DHCP network configuration process will attempt to configure the DNS.  This will only work if
		// the IP address 127.0.0.1 is bound to the loopback network interface.  And if there isn't the network
		// is left in a state that will require a reboot to unjam.  Check that we have bound it ...
		Device loopback = ((DeviceManager)InitialNaming.lookup(DeviceManager.NAME)).getDevice("loopback");
		NetDeviceAPI api = (NetDeviceAPI)loopback.getAPI(NetDeviceAPI.class);
		ProtocolAddressInfo info = api.getProtocolAddressInfo(EthernetConstants.ETH_P_IP);
		if (info == null || !info.contains(InetAddress.getByAddress(new byte[]{127, 0, 0, 1}))) {
		        System.err.println("The loopback network device is not bound to IP address 127.0.0.1");
		        System.err.println("Run 'ifconfig loopback 127.0.0.1 255.255.255.255' to fix this.");
		        exit(1);
		}
 
		// Now it should be safe to do the DHCP configuration ...
		System.out.println("Trying to configure " + dev.getId() + "...");
		final IPv4ConfigurationService cfg = (IPv4ConfigurationService)InitialNaming.lookup(IPv4ConfigurationService.NAME);
		cfg.configureDeviceDhcp(dev, true);
	}

}
