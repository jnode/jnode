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

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;

import org.jnode.driver.Device;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.fs.FileSystem;
import org.jnode.fs.service.FileSystemService;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.DeviceArgument;
import org.jnode.shell.syntax.FileArgument;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class MountCommand extends AbstractCommand {

    private final DeviceArgument ARG_DEV = new DeviceArgument("device",
            Argument.OPTIONAL, "the device to mount", BlockDeviceAPI.class);

    private final FileArgument ARG_DIR = new FileArgument("directory",
            Argument.OPTIONAL, "the mount point");

    private final FileArgument ARG_FSPATH = new FileArgument("fsPath",
            Argument.OPTIONAL, "the subdirectory within the filesystem to use as root");

    public MountCommand() {
        super("Mount a filesystem");
        registerArguments(ARG_DEV, ARG_DIR, ARG_FSPATH);
    }

    public static void main(String[] args) throws Exception {
        new MountCommand().execute(args);
    }

    public void execute(CommandLine commandLine, InputStream in,
            PrintStream out, PrintStream err) throws Exception {
        // Find the filesystem service
        final FileSystemService fss = InitialNaming.lookup(FileSystemService.NAME);

        if (!ARG_DEV.isSet()) {
            // List all mounted file systems
            Map<String, FileSystem<?>> filesystems = fss.getMountPoints();
            for (String mountPoint : filesystems.keySet()) {
                FileSystem<?> fs = filesystems.get(mountPoint);
                Device device = fs.getDevice();
                String mode = fs.isReadOnly() ? "ro" : "rw";
                String type = fs.getType().getName();
                out.println(device.getId() + " on " + mountPoint + " type " + type + " (" + mode + ')');
            }
        } else {
            // Get the parameters
            final Device dev = ARG_DEV.getValue();
            final File mountPoint = ARG_DIR.getValue();
            final File fsPath = ARG_FSPATH.getValue();

            // Find the filesystem
            final FileSystem<?> fs = fss.getFileSystem(dev);
            if (fs == null) {
                err.println("No filesystem found on " + dev.getId());
                exit(1);
            } else {
                // Mount it
                fss.mount(mountPoint.toString(), fs, fsPath.toString());
            }
        }
    }

}
