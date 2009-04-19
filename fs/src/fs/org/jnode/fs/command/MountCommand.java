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
 
package org.jnode.fs.command;

import java.io.File;
import java.io.PrintWriter;
import java.util.Map;

import org.jnode.driver.Device;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.fs.FileSystem;
import org.jnode.fs.service.FileSystemService;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.DeviceArgument;
import org.jnode.shell.syntax.FileArgument;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class MountCommand extends AbstractCommand {

    private static final String help_device = "the device to mount";
    private static final String help_dir = "the mount point";
    private static final String help_fspath = "the subdirectory within the filesystem to use as root";
    private static final String help_super = "Mount a filesystem";
    private static final String fmt_mount = "%s on %s type %s (%s)%n";
    private static final String fmt_err_nofs = "No filesystem found on %s%n";
    
    private final DeviceArgument argDevice 
        = new DeviceArgument("device", Argument.OPTIONAL, help_device, BlockDeviceAPI.class);
    private final FileArgument argDir = new FileArgument("directory", Argument.OPTIONAL, help_dir);
    private final FileArgument argFspath = new FileArgument("fsPath", Argument.OPTIONAL, help_fspath);

    public MountCommand() {
        super(help_super);
        registerArguments(argDevice, argDir, argFspath);
    }

    public static void main(String[] args) throws Exception {
        new MountCommand().execute(args);
    }

    public void execute() throws Exception {
        // Find the filesystem service
        final FileSystemService fss = InitialNaming.lookup(FileSystemService.NAME);
        PrintWriter out = getOutput().getPrintWriter();
        PrintWriter err = getError().getPrintWriter();
        if (!argDevice.isSet()) {
            // List all mounted file systems
            Map<String, FileSystem<?>> filesystems = fss.getMountPoints();
            for (String mountPoint : filesystems.keySet()) {
                FileSystem<?> fs = filesystems.get(mountPoint);
                Device device = fs.getDevice();
                String mode = fs.isReadOnly() ? "ro" : "rw";
                String type = fs.getType().getName();
                out.format(fmt_mount, device.getId(), mountPoint, type, mode);
            }
        } else {
            // Get the parameters
            final Device dev = argDevice.getValue();
            final File mountPoint = argDir.getValue();
            final File fsPath = argFspath.getValue();

            // Find the filesystem
            final FileSystem<?> fs = fss.getFileSystem(dev);
            if (fs == null) {
                err.format(fmt_err_nofs, dev.getId());
                exit(1);
            } else {
                // Mount it
                fss.mount(mountPoint.toString(), fs, fsPath.toString());
            }
        }
    }

}
