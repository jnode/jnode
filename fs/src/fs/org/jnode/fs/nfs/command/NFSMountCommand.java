/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
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
 
package org.jnode.fs.nfs.command;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

import javax.naming.NameNotFoundException;

import org.jnode.driver.DeviceAlreadyRegisteredException;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.DriverException;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.nfs.nfs2.NFS2Device;
import org.jnode.fs.nfs.nfs2.NFS2Driver;
import org.jnode.fs.nfs.nfs2.NFS2FileSystem;
import org.jnode.fs.nfs.nfs2.NFS2FileSystemType;
import org.jnode.fs.service.FileSystemService;
import org.jnode.naming.InitialNaming;
import org.jnode.net.nfs.Protocol;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.IntegerArgument;

/**
 * @author Andrei Dore
 * @author crawley@jnode.org
 */
public class NFSMountCommand extends AbstractCommand {
    private final FileArgument MOUNTPOINT_ARG =
            new FileArgument("directory", Argument.MANDATORY, "the mountpoint");

    private final NFSHostNameArgument HOST_ARG =
            new NFSHostNameArgument("nfsFileSystem", Argument.MANDATORY,
                    "remote NFS host and exported directory (host:dir)");

    private final FlagArgument READ_ONLY_FLAG =
            new FlagArgument("readOnly", Argument.OPTIONAL,
                    "if set, mount the file system read-only");

    private final FlagArgument READ_WRITE_FLAG =
            new FlagArgument("readWrite", Argument.OPTIONAL,
                    "if set, mount the file system read-write");

    private final FlagArgument TCP_FLAG =
            new FlagArgument("tcp", Argument.OPTIONAL, "if set, use tcp protocol");

    private final FlagArgument UDP_FLAG =
            new FlagArgument("udp", Argument.OPTIONAL, "if set, use udp protocol (default)");

    private final IntegerArgument USER_ID_ARG =
            new IntegerArgument("uid", Argument.OPTIONAL, "remote user id (default -1)");

    private final IntegerArgument GROUP_ID_ARG =
            new IntegerArgument("gid", Argument.OPTIONAL, "remote group id (default -1)");

    public NFSMountCommand() {
        super("mount an NFS filesystem");
        registerArguments(MOUNTPOINT_ARG, HOST_ARG, READ_ONLY_FLAG, READ_WRITE_FLAG, TCP_FLAG,
                UDP_FLAG, USER_ID_ARG, GROUP_ID_ARG);
    }

    public static void main(String[] args) throws Exception {
        new NFSMountCommand().execute(args);
    }

    public void execute() throws NameNotFoundException, DriverException, DeviceAlreadyRegisteredException,
        FileSystemException, IOException {
        final File mountPoint = MOUNTPOINT_ARG.getValue();
        final InetAddress host = HOST_ARG.getAddress();
        final String remoteDirectory = HOST_ARG.getRemoteDirectory();

        // Choose the protocol (udp or tcp) the default value it is udp.
        final Protocol protocol =
                UDP_FLAG.isSet() ? Protocol.UDP : TCP_FLAG.isSet() ? Protocol.TCP : Protocol.UDP;

        int uid = USER_ID_ARG.isSet() ? USER_ID_ARG.getValue() : -1;
        int gid = GROUP_ID_ARG.isSet() ? GROUP_ID_ARG.getValue() : -1;

        // Choose read-only or read-write.  If neither is specified, guess that the
        // file system should be read-only if no uid/gid was specified.
        boolean readOnly =
                READ_ONLY_FLAG.isSet() ? true : READ_WRITE_FLAG.isSet() ? false
                        : (uid == -1 && gid == -1);

        // Now do the work of mounting the file system, taking care to undo as much as 
        // we can in the event of a failure.
        final DeviceManager dm = DeviceUtils.getDeviceManager();
        final NFS2Device dev = new NFS2Device(host, remoteDirectory, protocol, uid, gid);
        dev.setDriver(new NFS2Driver());
        dm.register(dev);
        boolean ok = false;
        try {
            final FileSystemService fss = InitialNaming.lookup(FileSystemService.NAME);
            NFS2FileSystemType type = fss.getFileSystemType(NFS2FileSystemType.ID);
            final NFS2FileSystem fs = type.create(dev, readOnly);
            fss.registerFileSystem(fs);
            try {
                fss.mount(mountPoint.getAbsolutePath(), fs, null);
                ok = true;
            } finally {
                if (!ok) {
                    fss.unregisterFileSystem(dev);
                }
            }
        } finally {
            if (!ok) {
                dm.unregister(dev);
            }
        }
    }
}
