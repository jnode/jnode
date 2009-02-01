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
 
package org.jnode.apps.derby;


import java.io.File;
import java.io.PrintWriter;

import org.apache.derby.impl.drda.NetworkServerControlImpl;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.IntegerArgument;

/**
 * Command for handling Derby server.
 *
 * @author Martin Husted Hartvig (hagar@jnode.org)
 */
public class DerbyCommand extends AbstractCommand {
    private final FileArgument ARG_HOME = new FileArgument(
        "home", Argument.OPTIONAL, "home directory for derby");

    private final FlagArgument FLAG_START = new FlagArgument(
        "start", Argument.OPTIONAL, "if set, start the derby server");

    private final FlagArgument FLAG_STOP = new FlagArgument(
        "stop", Argument.OPTIONAL, "if set, stop the derby server");

    static final IntegerArgument ARG_PORT = new IntegerArgument(
        "port", Argument.OPTIONAL, "jdbc port (default 1527)");

    public DerbyCommand() {
        super("start or stop the derby db server");
        registerArguments(ARG_HOME, ARG_PORT, FLAG_START, FLAG_STOP);
    }

    public static void main(String[] args) throws Exception {
        new DerbyCommand().execute(args);
    }

    public void execute()
        throws Exception {
        File home_dir = ARG_HOME.getValue();
        String command = FLAG_START.isSet() ? "start" : FLAG_STOP.isSet() ? "stop" : "?";

        // FIXME ... this needs to be passed to the server somehow.
        int port = ARG_PORT.isSet() ? ARG_PORT.getValue() : 1527;

        NetworkServerControlImpl server = new NetworkServerControlImpl();

        int server_command = server.parseArgs(new String[]{command});

        if (server_command == NetworkServerControlImpl.COMMAND_START) {
            PrintWriter printWriter = getOutput().getPrintWriter();
            server.setLogWriter(printWriter);
            server.start(printWriter);
        } else if (server_command == NetworkServerControlImpl.COMMAND_SHUTDOWN) {
            server.shutdown();
        }

//    server.executeWork(server_command);

//    NetworkServerControl.main(new String[]{command});
    }
}
