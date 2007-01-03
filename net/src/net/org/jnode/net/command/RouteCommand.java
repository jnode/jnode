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
import org.jnode.naming.InitialNaming;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.net.help.argument.HostArgument;
import org.jnode.net.help.argument.NetworkArgument;
import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.ipv4.config.IPv4ConfigurationService;
import org.jnode.net.ipv4.layer.IPv4NetworkLayer;
import org.jnode.net.util.NetUtils;
import org.jnode.shell.Command;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.Syntax;
import org.jnode.shell.help.argument.DeviceArgument;
import org.jnode.shell.help.argument.OptionArgument;

/**
 * @author epr
 */
public class RouteCommand implements EthernetConstants, Command {

	static final String FUNC_ADD = "add";
	static final String FUNC_DEL = "del";

	static final OptionArgument ARG_FUNCTION =
		new OptionArgument(
			"function",
			"the function to perform",
			new OptionArgument.Option[] { new OptionArgument.Option(FUNC_ADD, "add a route"), new OptionArgument.Option(FUNC_DEL, "delete a route")});
	static final NetworkArgument ARG_TARGET = new NetworkArgument("target", "the target network");
	static final HostArgument ARG_GATEWAY = new HostArgument("gateway", "the gateway name or IP address");
	static final DeviceArgument ARG_DEVICE = new DeviceArgument("device", "the device to connect to the foreign network");

	public static Help.Info HELP_INFO =
		new Help.Info(
			"route",
			new Syntax[] {
				new Syntax("Print the routing table"),
				new Syntax(
					"Add or remove a route",
					new Parameter[] {
						new Parameter(ARG_FUNCTION, Parameter.MANDATORY),
						new Parameter(ARG_TARGET, Parameter.MANDATORY),
						//new Parameter("gw", "the gateway to access the target network", ARG_GATEWAY, Parameter.OPTIONAL),
						new Parameter(ARG_DEVICE, Parameter.MANDATORY),
                        new Parameter(ARG_GATEWAY, Parameter.OPTIONAL)})
	});

	public static void main(String[] args) throws Exception {
		new RouteCommand().execute(new CommandLine(args), System.in, System.out, System.err);
	}

	public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) throws Exception {
		ParsedArguments cmdLine = HELP_INFO.parse(commandLine.toStringArray());

		final IPv4NetworkLayer ipNL = (IPv4NetworkLayer) NetUtils.getNLM().getNetworkLayer(ETH_P_IP);

		if (cmdLine.size() == 0) {
			System.out.println("Routing table");
			System.out.println(ipNL.getRoutingTable());
		} else {
			String func = ARG_FUNCTION.getValue(cmdLine);
			IPv4Address target = ARG_TARGET.getAddress(cmdLine);
			IPv4Address gateway = ARG_GATEWAY.getAddress(cmdLine);
			Device device = ARG_DEVICE.getDevice(cmdLine);

			final IPv4ConfigurationService cfg = (IPv4ConfigurationService)InitialNaming.lookup(IPv4ConfigurationService.NAME);
			
			if (FUNC_ADD.equals(func)) {
				cfg.addRoute(target, gateway, device, true);
			} else if (FUNC_DEL.equals(func)) {
				cfg.deleteRoute(target, gateway, device);
			}
		}
		System.out.println();
	}

}
