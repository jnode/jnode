/*
 * $Id: UDPOutputCommand.java 4032 2008-04-27 13:53:11Z crawley $
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

package org.jnode.shell.command.debug;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.jnode.debug.RemoteAppender;
import org.jnode.debug.RemoteReceiver;
import org.jnode.debug.UDPOutputStream;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.ShellUtils;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.HostNameArgument;
import org.jnode.shell.syntax.IntegerArgument;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Martin Husted Hartvig (hagar@jnode.org)
 * @author crawley@jnode.org
 */
public class RemoteOutputCommand extends AbstractCommand {
    public static int DEFAULT_PORT = RemoteReceiver.DEFAULT_PORT;

    private final HostNameArgument ARG_ADDRESS =
            new HostNameArgument("host", Argument.MANDATORY, "remote host running the receiver");

    private final IntegerArgument ARG_PORT =
        new IntegerArgument("port", Argument.OPTIONAL, "remote port for the receiver");

    private final FlagArgument FLAG_UDP =
        new FlagArgument("udp", Argument.OPTIONAL, "if set use udp, otherwise use tcp");

    public RemoteOutputCommand() {
        super("send data from System.out, System.err and the logger to a remote receiver");
        registerArguments(ARG_ADDRESS, ARG_PORT, FLAG_UDP);
    }

    public void execute(CommandLine commandLine, InputStream in,
            PrintStream out, PrintStream err) 
        throws Exception {
        try {
            final int port = ARG_PORT.isSet() ? ARG_PORT.getValue() : DEFAULT_PORT;
            final InetAddress addr = ARG_ADDRESS.getAsInetAddress();
            final SocketAddress sockAddr = new InetSocketAddress(addr, port);
            final boolean udp = FLAG_UDP.isSet();
            OutputStream remoteOut = udp ? new UDPOutputStream(sockAddr) :
                createTCPOutputStream(addr, port);
            Writer remoteWriter = new OutputStreamWriter(remoteOut);
            try {
                ShellUtils.getCurrentShell().addConsoleOuputRecorder(remoteWriter);
            } catch (UnsupportedOperationException ex) {
                err.println("Cannot capture output from the current shell");
                remoteOut.close();
                exit(2);
            }
            // FIXME ... we cannot do this for TCP because it triggers a
            // kernel panic.  I suspect that this is something to do with
            // the TCP protocol stack itself trying to log debug messages.
            if (udp) {
                final Logger root = Logger.getRootLogger();
                root.addAppender(new RemoteAppender(remoteOut, null));
            }
        } catch (ConnectException ex) {
            err.println("Connection failed: " + ex.getMessage());
            exit(1);
        } catch (UnknownHostException ex) {
            err.println("Unknown host: " + ex.getMessage());
            exit(1);
        }
    }

    private OutputStream createTCPOutputStream(InetAddress addr, int port) throws IOException {
        Socket socket = new Socket(addr, port);
        return socket.getOutputStream();
    }
}
