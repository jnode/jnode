/*
 * $Id$
 */
package org.jnode.fs.command;

import org.jnode.driver.Device;
import org.jnode.driver.RemovableDeviceAPI;
import org.jnode.shell.help.DeviceArgument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class EjectCommand {

	static final DeviceArgument ARG_DEVICE = new DeviceArgument("device", "device to eject the medium from");
	public static Help.Info HELP_INFO = new Help.Info("eject", "Eject the medium from a given device", new Parameter[] { new Parameter(ARG_DEVICE, Parameter.MANDATORY)});

	public static void main(String[] args) throws Exception {
		ParsedArguments cmdLine = HELP_INFO.parse(args);

		final Device dev = ARG_DEVICE.getDevice(cmdLine);
		final RemovableDeviceAPI api = (RemovableDeviceAPI)dev.getAPI(RemovableDeviceAPI.class);
		api.eject();
	}
}
