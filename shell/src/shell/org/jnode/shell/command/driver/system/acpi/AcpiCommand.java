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

import java.io.PrintWriter;
import java.util.Collection;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.system.acpi.AcpiAPI;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.Syntax;
import org.jnode.shell.help.argument.OptionArgument;

/**
 * ACPI command.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */

public class AcpiCommand {

    public AcpiCommand() {
    }

    static final String FUNC_DUMP = "dump";

    static final String FUNC_BATTERY = "battery";

    static final OptionArgument ARG_FUNCTION = new OptionArgument(
            "function",
            "the function to perform",
            new OptionArgument.Option[] {
                    new OptionArgument.Option(FUNC_DUMP,
                            "lists all devices that can be discovered and controlled through ACPI"),
                    new OptionArgument.Option(FUNC_BATTERY,
                            "displays information about installed batteries") });

    public static Help.Info HELP_INFO = new Help.Info("acpi", new Syntax[] {
            new Syntax("displays ACPI details"),
            new Syntax("manage ACPI system", new Parameter[] { new Parameter(
                    ARG_FUNCTION, Parameter.OPTIONAL) }) });

    public static void main(String[] args) throws Exception {

        ParsedArguments cmdLine = HELP_INFO.parse(args);

        final Collection<Device> acpiDevs = DeviceUtils.getDevicesByAPI(AcpiAPI.class);
        if (acpiDevs.isEmpty()) {
            System.out.println("Could not connect to ACPI");
        } else {
            final Device dev = (Device)acpiDevs.iterator().next();
            final AcpiAPI api = (AcpiAPI)dev.getAPI(AcpiAPI.class);

            if (cmdLine.size() == 0) {
                System.out.println(api.toDetailedString());
            } else {
                String func = ARG_FUNCTION.getValue(cmdLine);
                if (func.equalsIgnoreCase(FUNC_DUMP)) {
                    api.dump(new PrintWriter(System.out));
                } else if (func.equalsIgnoreCase(FUNC_BATTERY)) {
                    System.out.println("Temporary disabled; TODO fix me");
                    //api.dumpBattery();
                }
            }
        }
    }
}
