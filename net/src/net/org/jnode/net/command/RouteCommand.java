/*
 * $Id$
 */
package org.jnode.net.command;

import org.jnode.driver.Device;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.ipv4.layer.IPv4NetworkLayer;
import org.jnode.net.ipv4.util.Route;
import org.jnode.net.util.NetUtils;
import org.jnode.shell.help.DeviceArgument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.OptionArgument;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.Syntax;

/**
 * @author epr
 */
public class RouteCommand implements EthernetConstants {

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
						new Parameter("gw", "the gateway to access the target network", ARG_GATEWAY, Parameter.OPTIONAL),
						new Parameter(ARG_DEVICE, Parameter.MANDATORY)})
	});

	public static void main(String[] args) throws Exception {
		ParsedArguments cmdLine = HELP_INFO.parse(args);

		final IPv4NetworkLayer ipNL = (IPv4NetworkLayer) NetUtils.getNLM().getNetworkLayer(ETH_P_IP);

		if (cmdLine.size() == 0) {
			System.out.println("Routing table");
			System.out.println(ipNL.getRoutingTable());
		} else {
			String func = ARG_FUNCTION.getValue(cmdLine);
			IPv4Address target = ARG_TARGET.getAddress(cmdLine);
			IPv4Address gateway = ARG_GATEWAY.getAddress(cmdLine);
			Device device = ARG_DEVICE.getDevice(cmdLine);

			if (FUNC_ADD.equals(func)) {
				Route.addRoute(target, gateway, device);
			} else if (FUNC_DEL.equals(func)) {
				Route.delRoute(target, gateway, device);
			}
		}

		System.out.println();
	}

}
