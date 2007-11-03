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
 
package org.jnode.fs.smbfs.command;

import org.jnode.shell.help.Argument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.argument.FileArgument;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.fs.service.FileSystemService;
import org.jnode.fs.FileSystemType;
import org.jnode.fs.FileSystem;
import org.jnode.fs.smbfs.SMBFSDevice;
import org.jnode.fs.smbfs.SMBFSDriver;
import org.jnode.fs.smbfs.SMBFileSystemType;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceUtils;
import org.jnode.naming.InitialNaming;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * @author Levente S\u00e1ntha
 */
public class SMBMountCommand extends AbstractCommand {
    private static final FileArgument MOUNTPOINT_ARG = new FileArgument("directory", "the mountpoint");
    private static final Argument HOST_ARG = new Argument("host", "Samba host");
    private static final Argument PATH_ARG = new Argument("path", "Samba path");
    private static final Argument USERNAME_ARG = new Argument("username", "Samba user");
    private static final Argument PASSWORD_ARG = new Argument("password", "Samba password");
    static Help.Info HELP_INFO = new Help.Info("mount", "Mount a Samba filesystem",
            new Parameter[]{new Parameter(MOUNTPOINT_ARG, Parameter.MANDATORY),
                    new Parameter(HOST_ARG, Parameter.MANDATORY),
                    new Parameter(PATH_ARG, Parameter.MANDATORY),
                    new Parameter(USERNAME_ARG, Parameter.MANDATORY),
                    new Parameter(PASSWORD_ARG, Parameter.OPTIONAL)});

    public static void main(String[] args) throws Exception {
        new SMBMountCommand().execute(args);
    }

    public void execute(CommandLine commandLine, InputStream in,
                        PrintStream out, PrintStream err) throws Exception {
        ParsedArguments cmdLine = HELP_INFO.parse(commandLine);

        final String mount_point = MOUNTPOINT_ARG.getValue(cmdLine);
        final String host = HOST_ARG.getValue(cmdLine);
        final String path = PATH_ARG.getValue(cmdLine);
        final String user = USERNAME_ARG.getValue(cmdLine);
        final String password = PASSWORD_ARG.getValue(cmdLine);
        final SMBFSDevice dev = new SMBFSDevice(host, path, user, password);
        dev.setDriver(new SMBFSDriver());
        final DeviceManager dm = DeviceUtils.getDeviceManager();
        dm.register(dev);
        final FileSystemService fss = InitialNaming.lookup(FileSystemService.NAME);
        FileSystemType type = fss.getFileSystemTypeForNameSystemTypes(SMBFileSystemType.NAME);
        final FileSystem fs = type.create(dev, true);
        fss.registerFileSystem(fs);
        fss.mount(mount_point, fs, null);
    }
}
