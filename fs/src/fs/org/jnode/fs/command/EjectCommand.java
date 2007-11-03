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
 
package org.jnode.fs.command;

import java.io.InputStream;
import java.io.PrintStream;

import org.jnode.driver.Device;
import org.jnode.driver.RemovableDeviceAPI;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.Command;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.argument.DeviceArgument;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class EjectCommand extends AbstractCommand {

	static final DeviceArgument ARG_DEVICE = new DeviceArgument("device", "device to eject the medium from");
	public static Help.Info HELP_INFO = new Help.Info("eject", "Eject the medium from a given device", new Parameter[] { new Parameter(ARG_DEVICE, Parameter.MANDATORY)});

	public static void main(String[] args) throws Exception {
		new EjectCommand().execute(args);
	}

	public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) throws Exception {
		ParsedArguments cmdLine = HELP_INFO.parse(commandLine);

		final Device dev = ARG_DEVICE.getDevice(cmdLine);
		final RemovableDeviceAPI api = dev.getAPI(RemovableDeviceAPI.class);
		api.eject();
	}
}
