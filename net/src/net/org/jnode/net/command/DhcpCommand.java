/*
 * $Id$
 */
package org.jnode.net.command;

import org.jnode.driver.Device;
import org.jnode.naming.InitialNaming;
import org.jnode.net.ipv4.config.IPv4ConfigurationService;
import org.jnode.shell.help.DeviceArgument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;

/**
 * @author markhale
 */
public class DhcpCommand {

        static final DeviceArgument ARG_DEVICE = new DeviceArgument("device", "the device to boot from");

	public static Help.Info HELP_INFO = new Help.Info(
		"dhcp",
		"Try to configure the given device using DHCP",
		new Parameter[]{
			new Parameter(ARG_DEVICE, Parameter.MANDATORY)
		}
	);

	public static void main(String[] args)
	throws Exception {
		ParsedArguments cmdLine = HELP_INFO.parse(args);

		final Device dev = ARG_DEVICE.getDevice(cmdLine);
		System.out.println("Trying to configure " + dev.getId() + "...");
		final IPv4ConfigurationService cfg = (IPv4ConfigurationService)InitialNaming.lookup(IPv4ConfigurationService.NAME);
		cfg.configureDeviceDhcp(dev, true);
	}

}
