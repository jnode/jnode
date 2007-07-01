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

import java.io.*;
import javax.naming.NameNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceNotFoundException;
import org.jnode.driver.DriverException;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.Command;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.argument.DeviceArgument;
import org.jnode.shell.help.argument.FileArgument;
import org.jnode.shell.help.argument.OptionArgument;

/**
 * <p/>
 * The Grub Installer command for the JNODE.
 * jnode/>grub hdb /devices/hdb0
 *
 * @HDA_TARGET /dev/hda0 or /dev/fd0
 * <p/>
 * TODO: Adding more options for supporting JGRUB with user specified File
 * System wise.
 * Adding more command support for grub insallation.
 * @author Tango Devian
 */
public class JGrubInstallCommand implements Command {
    static final DeviceArgument ARG_DEVICE = new DeviceArgument("device", "device where grub will be installed");
    static final FileArgument ARG_DIR = new FileArgument("directory", "the directory for stage2 and menu.lst");
    static final OptionArgument TYPE = new OptionArgument("action","Type parameter",
            new OptionArgument.Option("-p","Set the partition point for installing Stage2,menu.lst"));
    static final OptionArgument PS_VAL = new OptionArgument("Partition Value",
            "Setting The Partition value like (-p 1) for the hdx1",
         new OptionArgument.Option("0",   "hdX0"),
         new OptionArgument.Option("1",   "hdX1"),
         new OptionArgument.Option("2",   "hdX2"),
         new OptionArgument.Option("3",   "hdX3"));




 static final Help.Info      HELP_INFO  =  new Help.Info("grub",
         "Install the grub to the specified location.",
         new Parameter(ARG_DEVICE, Parameter.MANDATORY),
         new Parameter(TYPE,Parameter.MANDATORY),
         new Parameter(PS_VAL,Parameter.MANDATORY),
         new Parameter(ARG_DIR,Parameter.MANDATORY));
    /**
     * @param args
     * @throws Exception
     */
    public static void main(String... args) throws Exception {
        new JGrubInstallCommand().execute(new CommandLine(args), System.in, System.out, System.err);
    }

    /**
     *
     */
    public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) throws Exception {
        try {
            ParsedArguments cmdLine = HELP_INFO.parse(commandLine.toStringArray());
            String device = ARG_DEVICE.getValue(cmdLine);
            File destDir = ARG_DIR.getFile(cmdLine);

            Integer bsize=null;
   		    try {
               bsize = Integer.valueOf(PS_VAL.getValue(cmdLine));
            } catch (NumberFormatException nfe) {
               System.out.println("ERROR: give the partition value as the 0 ,1,2,3"+"\n" +"This grub installer not support the Extended partition.");
            }
            DeviceManager dm = InitialNaming.lookup(DeviceManager.NAME);
            Device dev = dm.getDevice(device);
            String destDirName = destDir.getAbsolutePath();
            out.println("Installing GRUB to: " + device + ", " + destDirName);
            try {
                new MBRFormatter().format(dev,bsize);
                new GrubJFatFormatter().format(destDirName);
                out.println("Restarting device: " + device);
                dm.stop(dev);
                dm.start(dev);
                out.println("GRUB has been successflly installed to " + device + ".");
            } catch (FileNotFoundException e) {
                err.println("File not found: " + e.getMessage());
            } catch (IOException e) {
                err.println("I/O exception: " + e.getMessage());
            }
        } catch (NameNotFoundException e) {
            err.println("Name not found: " + e.getMessage());
        } catch (DeviceNotFoundException e) {
            err.println("Device not found: " + e.getMessage());
        } catch (DriverException e) {
            err.println("The DriverException Occuered ..." + e.getMessage());
        }
    }
}
