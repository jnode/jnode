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

package org.jnode.fs.ftpfs.command;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import javax.naming.NameNotFoundException;

import org.apache.log4j.Logger;
import org.jnode.driver.DeviceAlreadyRegisteredException;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.DriverException;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.ftpfs.FTPFSDevice;
import org.jnode.fs.ftpfs.FTPFSDriver;
import org.jnode.fs.ftpfs.FTPFileSystem;
import org.jnode.fs.ftpfs.FTPFileSystemType;
import org.jnode.fs.service.FileSystemService;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.HostNameArgument;
import org.jnode.shell.syntax.StringArgument;

/**
 * @author Levente S\u00e1ntha
 */
public class FTPMountCommand extends AbstractCommand {
    private final FileArgument MOUNTPOINT_ARG = 
        new FileArgument("directory", Argument.MANDATORY, "the mountpoint");
    
    private static final HostNameArgument HOST_ARG = 
        new HostNameArgument("host", Argument.MANDATORY, "FTP host");
    
    private static final StringArgument USERNAME_ARG = 
        new StringArgument("userName", Argument.MANDATORY, "FTP user");
    
    private static final StringArgument PASSWORD_ARG = 
        new StringArgument("password", Argument.OPTIONAL, "FTP password");
    
    public FTPMountCommand() {
        super("Mount an FTP filesystem");
        registerArguments(MOUNTPOINT_ARG, HOST_ARG, USERNAME_ARG, PASSWORD_ARG);
    }

    public static void main(String[] args) throws Exception {
        new FTPMountCommand().execute(args);
    }

    public void execute(CommandLine commandLine, InputStream in,
                        PrintStream out, PrintStream err) 
        throws DriverException, NameNotFoundException, DeviceAlreadyRegisteredException, 
            FileSystemException, IOException {
        final File mountPoint = MOUNTPOINT_ARG.getValue();
        final String host = HOST_ARG.getValue();
        final String user = USERNAME_ARG.getValue();
        final String password = PASSWORD_ARG.getValue();
        boolean ok = false;
        
        final FileSystemService fss = InitialNaming.lookup(FileSystemService.NAME);
        FTPFileSystemType type = fss.getFileSystemType(FTPFileSystemType.ID);
        final DeviceManager dm = DeviceUtils.getDeviceManager();
        final FTPFSDevice dev = new FTPFSDevice(host, user, password);
        dev.setDriver(new FTPFSDriver());
        FTPFileSystem fs = null;
        try {
            dm.register(dev);
            fs = type.create(dev, true);
            fss.registerFileSystem(fs);
            fss.mount(mountPoint.getAbsolutePath(), fs, null);
            ok = true;
        } finally {
            if (!ok) {
                try {
                    // If we failed, try to undo the changes that we managed to make
                    if (fs != null) {
                        fss.unregisterFileSystem(dev);
                    }
                    dm.unregister(dev);
                } catch (Exception ex) {
                    Logger log = Logger.getLogger(FTPMountCommand.class);
                    log.fatal("Cannot undo failed mount attempt", ex);
                }
            }
        }
    }
}
