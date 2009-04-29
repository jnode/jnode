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
 
package org.jnode.command.net;

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

    private static final String help_set = "if set, set the ESSID";
    private static final String help_dev = "the device to be operated on";
    private static final String help_value = "the value to use in the operations";
    private static final String help_super = "Manage a WLan device";
    private static final String fmt_set = "Setting ESSID on %s to %s%n";
    
    private final FlagArgument argSetEssid;
    private final DeviceArgument argDevice;
    private final StringArgument argValue;

    public WLanCtlCommand() {
        super(help_super);
        argSetEssid = new FlagArgument("setEssid", Argument.OPTIONAL, help_set);
        argDevice = new DeviceArgument("device", Argument.MANDATORY, help_dev, WirelessNetDeviceAPI.class);
        argValue = new StringArgument("value", Argument.OPTIONAL, help_value);
        registerArguments(argSetEssid, argDevice, argValue);
    }

    public static void main(String[] args) throws Exception {
        new WLanCtlCommand().execute(args);
    }
    
    public void execute() throws ApiNotFoundException, NetworkException {
        final Device dev = argDevice.getValue();
        final WirelessNetDeviceAPI api;
        api = dev.getAPI(WirelessNetDeviceAPI.class);

        // Perform the selected operation
        if (argSetEssid.isSet()) {
            final String essid = argValue.getValue();
            getOutput().getPrintWriter().format(fmt_set, dev.getId(), essid);
            api.setESSID(essid);
        }
    }
}
