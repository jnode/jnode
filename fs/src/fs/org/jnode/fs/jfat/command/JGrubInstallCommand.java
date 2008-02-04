/*
 * $Id: FatConstants.java 2224  Tanmoy $
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


package org.jnode.fs.jfat.command;

import java.io.InputStream;
import java.io.PrintStream;

import org.jnode.driver.Device;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.argument.DeviceArgument;

/**
 * Grub Installer command for JNode.
 * example of usage : grub hdb0
 *
 * TODO: Add more options for supporting Grub with user specified file
 * TODO: Add more command support for grub installation.
 * @author Tango Devian
 */
public class JGrubInstallCommand extends AbstractCommand {
    static final DeviceArgument ARG_DEVICE = new DeviceArgument("device partition", "device where grub stage 2 will be installed", BlockDeviceAPI.class);


 static final Help.Info      HELP_INFO  =  new Help.Info("grub",
         "Install the grub to the specified location.",
         new Parameter(ARG_DEVICE, Parameter.MANDATORY));
    /**
     * @param args
     * @throws Exception
     */
    public static void main(String... args) throws Exception {
        new JGrubInstallCommand().execute(args);
    }

    /**
     *
     */
    public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) throws Exception {
        ParsedArguments cmdLine = HELP_INFO.parse(commandLine);
        Device device = ARG_DEVICE.getDevice(cmdLine);

        JGrub jgrub = new JGrub(out, err, device);
        jgrub.install();
    }
}
