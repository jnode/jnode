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
 
package org.jnode.shell.command.driver.system.acpi;

import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collection;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.system.acpi.AcpiAPI;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FlagArgument;

/**
 * ACPI display and management command.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author crawley@jnode.org
 */

public class AcpiCommand extends AbstractCommand {

    private final FlagArgument FLAG_DUMP =
        new FlagArgument("dump", Argument.OPTIONAL, 
                "lists all devices that can be discovered and controlled through ACPI");
    
    private final FlagArgument FLAG_BATTERY =
        new FlagArgument("battery", Argument.OPTIONAL, 
                    "displays information about installed batteries");
    
    public AcpiCommand() {
        super("display and (in the future) manage the ACPI system");
        registerArguments(FLAG_BATTERY, FLAG_DUMP);
    }

    public static void main(String[] args) throws Exception {
        new AcpiCommand().execute(args);
    }
    
    @Override
    public void execute(CommandLine commandLine, InputStream in,
            PrintStream out, PrintStream err) throws ApiNotFoundException {
        final Collection<Device> acpiDevs = DeviceUtils.getDevicesByAPI(AcpiAPI.class);
        if (acpiDevs.isEmpty()) {
           out.println("No ACPI devices are registered");
           exit(1);
        } 
        else {
            for (Device dev : acpiDevs) {
                final AcpiAPI api = (AcpiAPI) dev.getAPI(AcpiAPI.class);

                if (FLAG_DUMP.isSet()) {
                    api.dump(new PrintWriter(out));
                }
                else if (FLAG_BATTERY.isSet()) {
                    // TODO fix this
                    out.println("The '--battery' option is temporary disabled");
                    //api.dumpBattery();
                }
                else {
                    out.println(api.toDetailedString());
                } 
            }
        }
    }
}
