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

import org.apache.log4j.Logger;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.net.NetworkException;
import org.jnode.driver.net.WirelessNetDeviceAPI;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.Argument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.SyntaxErrorException;
import org.jnode.shell.help.argument.DeviceArgument;
import org.jnode.shell.help.argument.OptionArgument;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class WLanCtlCommand extends AbstractCommand {

    private static final String FUNC_SETESSID = "setessid";

    private static final OptionArgument ARG_FUNCTION = new OptionArgument(
            "function", "the function to perform",
            new OptionArgument.Option[] { new OptionArgument.Option(
                    FUNC_SETESSID, "Set the ESSID"), });

    private static final DeviceArgument ARG_DEVICE = new DeviceArgument(
            "device", "the device to control", WirelessNetDeviceAPI.class);

    private static final Argument ARG_VALUE = new Argument("value",
            "Value of the function");

    public static Help.Info HELP_INFO = new Help.Info("wlanctl",
            "Try to configure the given device using BOOTP", new Parameter[] {
                    new Parameter(ARG_FUNCTION, Parameter.MANDATORY),
                    new Parameter(ARG_DEVICE, Parameter.MANDATORY),
                    new Parameter(ARG_VALUE, Parameter.OPTIONAL) });

    private static final Logger log = Logger.getLogger(HELP_INFO.getName());

    public static void main(String[] args) throws Exception, SyntaxErrorException {
    	new WLanCtlCommand().execute(args);
    }

    private static void setESSID(Device dev, WirelessNetDeviceAPI api,
            ParsedArguments cmdLine) throws NetworkException {
        final String essid = ARG_VALUE.getValue(cmdLine);
        System.out.println("Setting ESSID on " + dev.getId() + " to " + essid);
        api.setESSID(essid);
    }

	public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) throws Exception {
		ParsedArguments cmdLine = HELP_INFO.parse(commandLine);

        final Device dev = ARG_DEVICE.getDevice(cmdLine);
        final WirelessNetDeviceAPI api;
        try {
            api = (WirelessNetDeviceAPI) dev.getAPI(WirelessNetDeviceAPI.class);
        } catch (ApiNotFoundException e) {
            System.err.println("Device " + dev.getId()
                    + " is not a wireless network device");
            exit(2);
            return;  // not reached
        }

        // Get the function
        final String function = ARG_FUNCTION.getValue(cmdLine);
        try {
            if (function.equals(FUNC_SETESSID)) {
                setESSID(dev, api, cmdLine);
            } else {
                System.err.println("Unknown function " + function);
                exit(3);
            }
        } catch (NetworkException ex) {
            System.err.println("Function " + function + " failed: "
                    + ex.getMessage());
            log.debug("Function " + function + " failed", ex);
            exit(1);
        }
	}
}
