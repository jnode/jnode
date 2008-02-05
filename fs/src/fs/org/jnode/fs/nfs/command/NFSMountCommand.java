/*
 * $Id $
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
import java.net.InetAddress;

import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceUtils;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemType;
import org.jnode.fs.nfs.nfs2.NFS2Device;
import org.jnode.fs.nfs.nfs2.NFS2Driver;
import org.jnode.fs.nfs.nfs2.NFS2FileSystem;
import org.jnode.fs.nfs.nfs2.NFS2FileSystemType;
import org.jnode.fs.service.FileSystemService;
import org.jnode.naming.InitialNaming;
import org.jnode.net.nfs.Protocol;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.Syntax;
import org.jnode.shell.help.argument.FileArgument;
import org.jnode.shell.help.argument.IntegerArgument;
import org.jnode.shell.help.argument.OptionArgument;

/**
 * @author Andrei Dore
 */
public class NFSMountCommand extends AbstractCommand {
    private static final FileArgument MOUNTPOINT_ARG = new FileArgument(
            "directory", "the mountpoint");
    private static final NFSHostNameArgument HOST_ARG = new NFSHostNameArgument(
            "host:remoteDir", "NFS host");

    private static final OptionArgument PROTOCOL_ARG = new OptionArgument(
            "protocol", "protocol", new OptionArgument.Option[] {
                    new OptionArgument.Option("tcp", "tcp protocol"),
                    new OptionArgument.Option("udp", "udp protocol") });

    private static final IntegerArgument USER_ID_ARG = new IntegerArgument(
            "uid", "user id");
    private static final IntegerArgument GROUP_ID_ARG = new IntegerArgument(
            "gid", "group id");

    private static final Parameter PARAMETER_PROTOCOL = new Parameter(
            PROTOCOL_ARG, Parameter.OPTIONAL);

    private static final Parameter PARAMETER_USER_ID = new Parameter(
            USER_ID_ARG, Parameter.MANDATORY);
    private static final Parameter PARAMETER_GROUP_ID = new Parameter(
            GROUP_ID_ARG, Parameter.MANDATORY);

    static Help.Info HELP_INFO = new Help.Info("nfsmount",

    new Syntax("Mount a read only NFS filesystem", new Parameter[] {
            new Parameter(MOUNTPOINT_ARG, Parameter.MANDATORY),
            new Parameter(HOST_ARG, Parameter.MANDATORY),

            PARAMETER_PROTOCOL }),

    new Syntax("Mount a  NFS filesystem", new Parameter[] {
            new Parameter(MOUNTPOINT_ARG, Parameter.MANDATORY),
            new Parameter(HOST_ARG, Parameter.MANDATORY), PARAMETER_USER_ID,
            PARAMETER_GROUP_ID, PARAMETER_PROTOCOL }));

    public static void main(String[] args) throws Exception {
        new NFSMountCommand().execute(args);
    }

    public void execute(CommandLine commandLine, InputStream in,
            PrintStream out, PrintStream err) throws Exception {
        ParsedArguments cmdLine = HELP_INFO.parse(commandLine);

        final String mount_point = MOUNTPOINT_ARG.getValue(cmdLine);
        final InetAddress host = HOST_ARG.getAddress(cmdLine);
        final String remoteDirectory = HOST_ARG.getRemoteDirectory(cmdLine);

        // select the protocol (udp or tcp) the default value it is udp.
        final Protocol protocol;
        if (PARAMETER_PROTOCOL.isSet(cmdLine)) {

            String protocolOption = PROTOCOL_ARG.getValue(cmdLine)
                    .toLowerCase().intern();

            if (protocolOption == "tcp") {
                protocol = Protocol.TCP;
            } else {
                protocol = Protocol.UDP;
            }
        } else {
            protocol = Protocol.UDP;
        }

        int uid = -1;
        int gid = -1;
        boolean readOnly;
        if (PARAMETER_USER_ID.isSet(cmdLine)
                && PARAMETER_GROUP_ID.isSet(cmdLine)) {

            uid = USER_ID_ARG.getInteger(cmdLine);
            gid = GROUP_ID_ARG.getInteger(cmdLine);

            readOnly = false;

        } else {
            readOnly = true;
        }

        final NFS2Device dev;
        if (!readOnly) {
            dev = new NFS2Device(host, remoteDirectory, protocol, uid, gid);
        } else {
            dev = new NFS2Device(host, remoteDirectory, protocol);
        }

        dev.setDriver(new NFS2Driver());
        final DeviceManager dm = DeviceUtils.getDeviceManager();
        dm.register(dev);
        final FileSystemService fss = InitialNaming
                .lookup(FileSystemService.NAME);
        NFS2FileSystemType type = NFS2FileSystemType.getInstance();
        final NFS2FileSystem fs = type.create(dev, readOnly);
        fss.registerFileSystem(fs);
        fss.mount(mount_point, fs, null);

    }
}
