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
 
package org.jnode.fs.nfs.command;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.List;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.net.nfs.Protocol;
import org.jnode.net.nfs.nfs2.mount.ExportEntry;
import org.jnode.net.nfs.nfs2.mount.Mount1Client;
import org.jnode.net.nfs.nfs2.mount.MountException;
import org.jnode.shell.CommandLine.Token;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.CommandSyntaxException;

public class NFSHostNameArgument extends Argument<String> {

    public NFSHostNameArgument(String name, int flags, String description) {
        super(name, flags, new String[0], description);
    }

    public void complete(CompletionInfo completion, String partial) {
        int index = partial.indexOf(':');
        if (index <= 0) {
            return;
        }

        String hostName = partial.substring(0, index);
        final InetAddress host;
        try {
            host = InetAddress.getByName(hostName);
        } catch (UnknownHostException e) {
            return;
        }

        String partialDirectory = partial.substring(index + 1);

        List<ExportEntry> exportEntryList;
        try {
            exportEntryList = AccessController.doPrivileged(
                    new PrivilegedExceptionAction<List<ExportEntry>>() {
                        public List<ExportEntry> run() throws IOException, MountException {
                            Mount1Client client = 
                                new Mount1Client(host, Protocol.TCP, -1, -1);
                            List<ExportEntry> exportEntryList;
                            try {
                                exportEntryList = client.export();
                            } finally {
                                if (client != null) {
                                    try {
                                        client.close();
                                    } catch (IOException e) {
                                        // squash
                                    }
                                }
                            }
                            return exportEntryList;
                        }
                    });
        } catch (PrivilegedActionException e) {
            return;
        }

        for (int i = 0; i < exportEntryList.size(); i++) {
            ExportEntry exportEntry = exportEntryList.get(i);
            if (exportEntry.getDirectory().startsWith(partialDirectory)) {
                completion.addCompletion(hostName + ":" + exportEntry.getDirectory());
            }
        }
    }

    public InetAddress getAddress() throws UnknownHostException {
        String value = getValue();
        if (value == null) {
            return null;
        }
        int index = value.indexOf(':');
        return InetAddress.getByName(index == -1 ? value : value.substring(0, index));
    }

    public String getRemoteDirectory() {
        String value = getValue();
        if (value == null) {
            return null;
        }
        int index = value.indexOf(':');
        return (index == -1 || index == value.length() - 1) ? null : value.substring(index + 1);
    }

    @Override
    protected String argumentKind() {
        return "hostname:directory";
    }

    @Override
    protected String doAccept(Token value) throws CommandSyntaxException {
        int index = value.text.indexOf(':');
        if (index == -1) {
            throw new CommandSyntaxException("missing ':'");
        } else if (index == 0) {
            throw new CommandSyntaxException("no hostname before ':'");
        } else if (index == value.text.length() - 1) {
            throw new CommandSyntaxException("no directory after ':'");
        } else {
            return value.text;
        }
    }
}
