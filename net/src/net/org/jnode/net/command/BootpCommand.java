/*
 * $Id$
 */
package org.jnode.net.command;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

import org.jnode.driver.Device;
import org.jnode.net.ipv4.bootp.BOOTPClient;
import org.jnode.shell.help.DeviceArgument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;

/**
 * @author epr
 */
public class BootpCommand {

        static final DeviceArgument ARG_DEVICE = new DeviceArgument("device", "the device to boot from");

	public static Help.Info HELP_INFO = new Help.Info(
		"bootp",
		"Try to configure the given device using BOOTP",
		new Parameter[]{
			new Parameter(ARG_DEVICE, Parameter.MANDATORY)
		}
	);

	public static void main(String[] args)
	throws Exception {
		ParsedArguments cmdLine = HELP_INFO.parse(args);

		final Device dev = ARG_DEVICE.getDevice(cmdLine);

		System.out.println("Trying to configure " + dev.getId() + "...");
		final BOOTPClient client = new BOOTPClient();
		AccessController.doPrivileged(new PrivilegedExceptionAction() {
		    public Object run() throws IOException {
				client.configureDevice(dev);
				return null;
		    }
		});
	}

}
