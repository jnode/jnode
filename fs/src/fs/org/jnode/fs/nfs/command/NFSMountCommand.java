/*
 * $Id: NFSMountCommand.java 2945 2006-12-20 08:51:17Z qades $
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

package org.jnode.fs.nfs.command;

import java.io.InputStream;
import java.io.PrintStream;

import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceUtils;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemType;
import org.jnode.fs.nfs.nfs2.NFS2Device;
import org.jnode.fs.nfs.nfs2.NFS2Driver;
import org.jnode.fs.nfs.nfs2.NFS2FileSystemType;
import org.jnode.fs.service.FileSystemService;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.Command;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.Argument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.argument.FileArgument;

/**
 * @author Andrei Dore
 */
public class NFSMountCommand implements Command {
    private static final FileArgument MOUNTPOINT_ARG = new FileArgument("directory", "the mountpoint");
    private static final Argument HOST_ARG = new Argument("host", "NFS host");
    private static final Argument REMOTE_DIRECTORY_ARG = new Argument("remoteDir", "remote directory");
    static Help.Info HELP_INFO = new Help.Info("mount", "Mount an NFS filesystem",
            new Parameter[]{new Parameter(MOUNTPOINT_ARG, Parameter.MANDATORY),
                    new Parameter(HOST_ARG, Parameter.MANDATORY), new Parameter(REMOTE_DIRECTORY_ARG, Parameter.MANDATORY)
            });

    public static void main(String[] args) throws Exception {
        new NFSMountCommand().execute(new CommandLine(args), System.in,
                System.out, System.err);
    }

    public void execute(CommandLine commandLine, InputStream in,
                        PrintStream out, PrintStream err) throws Exception {
        ParsedArguments cmdLine = HELP_INFO.parse(commandLine.toStringArray());

        final String mount_point = MOUNTPOINT_ARG.getValue(cmdLine);
        final String host = HOST_ARG.getValue(cmdLine);
        final String remoteDirectory = REMOTE_DIRECTORY_ARG.getValue(cmdLine);


        final NFS2Device dev = new NFS2Device(host, remoteDirectory);
        dev.setDriver(new NFS2Driver());
        final DeviceManager dm = DeviceUtils.getDeviceManager();
        dm.register(dev);
        final FileSystemService fss = InitialNaming.lookup(FileSystemService.NAME);
        FileSystemType type = fss.getFileSystemTypeForNameSystemTypes(NFS2FileSystemType.NAME);
        final FileSystem fs = type.create(dev, true);
        fss.registerFileSystem(fs);
        fss.mount(mount_point, fs, null);


    }
}
