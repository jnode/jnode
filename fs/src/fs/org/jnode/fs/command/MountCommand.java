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
import org.jnode.fs.FileSystem;
import org.jnode.fs.service.FileSystemService;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.argument.DeviceArgument;
import org.jnode.shell.help.argument.FileArgument;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class MountCommand extends AbstractCommand {

    private static final DeviceArgument ARG_DEV = new DeviceArgument("device",
            "the device to mount");

    private static final FileArgument ARG_DIR = new FileArgument("directory",
            "the mountpoint");

    private static final FileArgument ARG_FSPATH = new FileArgument("fspath",
    "the subdirectory within the filesystem to use as root");

    static Help.Info HELP_INFO = new Help.Info("mount", "Mount a filesystem",
            new Parameter[] { new Parameter(ARG_DEV, Parameter.MANDATORY),
                    new Parameter(ARG_DIR, Parameter.MANDATORY),
                    new Parameter(ARG_FSPATH, Parameter.OPTIONAL) });

    public static void main(String[] args) throws Exception {
        new MountCommand().execute(args);
    }

    public void execute(CommandLine commandLine, InputStream in,
            PrintStream out, PrintStream err) throws Exception {
        ParsedArguments cmdLine = HELP_INFO.parse(commandLine);

        // Get the parameters
        final Device dev = ARG_DEV.getDevice(cmdLine);
        final String mountPoint = ARG_DIR.getValue(cmdLine);
        final String fsPath = ARG_FSPATH.getValue(cmdLine);

        // Find the filesystem service
        final FileSystemService fss = InitialNaming.lookup(FileSystemService.NAME);

        // Find the filesystem
        final FileSystem fs = fss.getFileSystem(dev);
        if (fs == null) {
            err.println("No filesystem found on " + dev.getId());
            exit(1);
        } else {
            // Mount it
            fss.mount(mountPoint, fs, fsPath);
        }
    }

}
