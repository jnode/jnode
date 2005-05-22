/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
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
		final RemovableDeviceAPI api = dev.getAPI(RemovableDeviceAPI.class);
		api.eject();
	}
}
