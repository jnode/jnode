/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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

import java.io.IOException;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.RemovableDeviceAPI;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.DeviceArgument;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class EjectCommand extends AbstractCommand {

    private static final String help_device = "Device to eject the medium from";
    private static final String fmt_failed = "Eject failed for %s: %s";
    private final DeviceArgument argDevice 
        = new DeviceArgument("device", Argument.MANDATORY | Argument.EXISTING, help_device, RemovableDeviceAPI.class);

    public EjectCommand() {
        super("Eject the medium from a given device");
        registerArguments(argDevice);
    }

    public static void main(String[] args) throws Exception {
        new EjectCommand().execute(args);
    }

    public void execute() 
        throws ApiNotFoundException, IOException {
        final Device dev = argDevice.getValue();
        final RemovableDeviceAPI api = dev.getAPI(RemovableDeviceAPI.class);
        try {
            api.eject();
        } catch (IOException ex) {
            getError().getPrintWriter().format(fmt_failed, dev.getId(), ex.getLocalizedMessage());
            exit(1);
        }
    }
}
