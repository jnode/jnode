/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.net.NetworkException;
import org.jnode.driver.net.WirelessNetDeviceAPI;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.DeviceArgument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.StringArgument;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author crawley@jnode.org
 */
public class WLanCtlCommand extends AbstractCommand {

    private final FlagArgument FLAG_SET_ESSID = new FlagArgument(
            "setEssid", Argument.OPTIONAL, "if set, set the ESSID");

    private final DeviceArgument ARG_DEVICE = new DeviceArgument(
            "device", Argument.MANDATORY, "the device to be operated on", WirelessNetDeviceAPI.class);

    private final StringArgument ARG_VALUE = new StringArgument(
            "value", Argument.OPTIONAL, "the value to use in the operation");


    public WLanCtlCommand() {
        super("Manage a WLan device");
        registerArguments(FLAG_SET_ESSID, ARG_DEVICE, ARG_VALUE);
    }

    public static void main(String[] args) throws Exception {
        new WLanCtlCommand().execute(args);
    }

    public void execute() throws ApiNotFoundException, NetworkException {
        final Device dev = ARG_DEVICE.getValue();
        final WirelessNetDeviceAPI api;
        api = dev.getAPI(WirelessNetDeviceAPI.class);

        // Perform the selected operation
        if (FLAG_SET_ESSID.isSet()) {
            final String essid = ARG_VALUE.getValue();
            getOutput().getPrintWriter().println("Setting ESSID on " + dev.getId() + " to " + essid);
            api.setESSID(essid);
        }
    }
}
