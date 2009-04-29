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
 
package org.jnode.command.net;

import java.io.File;

import org.jnode.net.ipv4.tftp.TFTPClient;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.HostNameArgument;

/**
 * This Command class does a batch mode TFTP get or put, or starts a simple TFTP client.
 * 
 * @author markhale
 * @author crawley
 */
public class TftpCommand extends AbstractCommand {
    
    private static final String help_put = "if set, transfer a file to the TFTP server";
    private static final String help_get = "if set, fetch a file from the TFTP server";
    private static final String help_host = "the hostname of the TFTP server";
    private static final String help_file = "the file to transfer";
    private static final String help_super = "Do a TFTP get or put, or run an interactive TFTP client";
    
    private final FlagArgument argPut;
    private final FlagArgument argGet;
    private final HostNameArgument argServer;
    private final FileArgument argFile;

    public TftpCommand() {
        super(help_super);
        argPut = new FlagArgument("put", Argument.OPTIONAL, help_put);
        argGet = new FlagArgument("get", Argument.OPTIONAL, help_get);
        argServer = new HostNameArgument("host", Argument.OPTIONAL, help_host);
        argFile = new FileArgument("filename", Argument.OPTIONAL, help_file);
        registerArguments(argGet, argPut, argFile, argServer);
    }

    public static void main(String[] args) throws Exception {
        new TftpCommand().execute(args);
    }

    public void execute() throws Exception {
        TFTPClient client = new TFTPClient(getOutput().getPrintWriter());
        String host = argServer.getValue();
        File file = argFile.getValue();
        if (argPut.isSet()) {
            if (client.executeCommand(new String[] {TFTPClient.CONNECT_CMD, host})) {
                if (!client.executeCommand(new String[] {TFTPClient.PUT_CMD, file.toString()})) {
                    exit(1);
                }
            } else {
                exit(2);
            }
        } else if (argGet.isSet()) {
            if (client.executeCommand(new String[] {TFTPClient.CONNECT_CMD, host})) {
                if (!client.executeCommand(new String[] {TFTPClient.GET_CMD, file.toString()})) {
                    exit(1);
                }
            } else {
                exit(2);
            }
        } else {
            if (host != null) {
                if (!client.executeCommand(new String[] {TFTPClient.CONNECT_CMD, host})) {
                    exit(2);
                }
            }
            client.run(getInput().getReader());
        }
    }
}
