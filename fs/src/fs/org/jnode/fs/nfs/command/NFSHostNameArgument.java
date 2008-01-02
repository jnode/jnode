package org.jnode.fs.nfs.command;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.List;

import org.jnode.net.nfs.Protocol;
import org.jnode.net.nfs.nfs2.mount.ExportEntry;
import org.jnode.net.nfs.nfs2.mount.Mount1Client;
import org.jnode.net.nfs.nfs2.mount.MountException;
import org.jnode.shell.help.Argument;
import org.jnode.shell.help.ParsedArguments;

public class NFSHostNameArgument extends Argument {

    public NFSHostNameArgument(String name, String description, boolean multi) {
        super(name, description, multi);
    }

    public NFSHostNameArgument(String name, String description) {
        super(name, description);
    }

    public String complete(String partial) {

        if (partial == null) {
            return null;
        }

        int index = partial.indexOf(':');
        if (index == -1) {
            return partial;
        }

        final InetAddress host;
        try {
            host = InetAddress.getByName(partial.substring(0, index));
        } catch (UnknownHostException e) {
            return partial;
        }

        String partialDirectory = partial.substring(index + 1);

        List<ExportEntry> exportEntryList;
        try {
            exportEntryList = AccessController
                    .doPrivileged(new PrivilegedExceptionAction<List<ExportEntry>>() {
                        public List<ExportEntry> run() throws IOException,
                                MountException {

                            Mount1Client client = new Mount1Client(host,
                                    Protocol.TCP, -1, -1);

                            List<ExportEntry> exportEntryList;
                            try {
                                exportEntryList = client.export();
                            } finally {
                                client.close();
                            }
                            return exportEntryList;

                        }
                    });
        } catch (PrivilegedActionException e) {

            return partial;
        }

        List<String> valueList = new ArrayList<String>();
        for (int i = 0; i < exportEntryList.size(); i++) {
            ExportEntry exportEntry = exportEntryList.get(i);

            if (exportEntry.getDirectory().startsWith(partialDirectory)) {
                valueList.add(partial.substring(0, index) + ":"
                        + exportEntry.getDirectory());
            }

        }

        String completed = complete(partial, valueList);

        return completed;

    }

    public InetAddress getAddress(ParsedArguments args)
            throws UnknownHostException {
        String value = getValue(args);
        if (value == null) {
            return null;
        }

        int index = value.indexOf(':');
        if (index == -1) {
            return InetAddress.getByName(value);
        } else {
            return InetAddress.getByName(value.substring(0, index));
        }

    }

    public String getRemoteDirectory(ParsedArguments args) {

        String value = getValue(args);

        if (value == null) {
            return null;
        }

        int index = value.indexOf(':');
        if (index == -1) {
            return null;
        }

        return value.substring(index + 1);

    }

}
