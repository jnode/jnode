/*
 * $Id$
 */
package org.jnode.net.command;

import java.util.Iterator;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.net.NetDeviceAPI;
import org.jnode.naming.InitialNaming;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.ipv4.util.Ifconfig;
import org.jnode.shell.help.DeviceArgument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.Syntax;

/**
 * @author epr
 */
public class IfconfigCommand {

	static final DeviceArgument ARG_DEVICE = new DeviceArgument("device", "the device");
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
		ParsedArguments cmdLine = HELP_INFO.parse(args);

		if( cmdLine.size() == 0 ) {
			DeviceManager dm = (DeviceManager)InitialNaming.lookup(DeviceManager.NAME);
			for (Iterator i = dm.getDevicesByAPI(NetDeviceAPI.class).iterator(); i.hasNext(); ) {
				Device dev = (Device)i.next();
				NetDeviceAPI api = (NetDeviceAPI)dev.getAPI(NetDeviceAPI.class);
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
				Ifconfig.setDefault(dev, ip, mask);
				System.out.println("IP address for " + dev.getId() + " set to " + api.getProtocolAddressInfo(EthernetConstants.ETH_P_IP));
			}
		}
		System.out.println();
	}

}
