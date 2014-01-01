/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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
 
package org.jnode.command.dev;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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
    
    private static final String help_addr  = "remote host running the receiver";
    private static final String help_port  = "remote port for the receiver";
    private static final String help_udp   = "if set, use udp, otherwise use tcp";
    private static final String help_super = "send data from System.out,System.err and the logger to a remote receiver";
    @SuppressWarnings("unused")
    private static final String err_capture = "Cannot capture output from the current shell";
    private static final String err_connect = "Connection failed: %s%n";
    private static final String err_host = "Unknown host: %s%n";
    
    public static int DEFAULT_PORT = RemoteReceiver.DEFAULT_PORT;

    private final HostNameArgument argAddress;
    private final IntegerArgument argPort;
    private final FlagArgument argUdp;

    public RemoteOutputCommand() {
        super(help_super);
        argAddress = new HostNameArgument("host", Argument.MANDATORY, help_addr);
        argPort    = new IntegerArgument("port", Argument.OPTIONAL, help_port);
        argUdp     = new FlagArgument("udp", Argument.OPTIONAL, help_udp);
        registerArguments(argAddress, argPort, argUdp);
    }
    
    public void execute() throws Exception {
        PrintWriter err = getError().getPrintWriter();
        try {
            final int port = argPort.isSet() ? argPort.getValue() : DEFAULT_PORT;
            final InetAddress addr = argAddress.getAsInetAddress();
            final SocketAddress sockAddr = new InetSocketAddress(addr, port);
            final boolean udp = argUdp.isSet();
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
            err.format(err_connect, ex.getLocalizedMessage());
            exit(1);
        } catch (UnknownHostException ex) {
            err.format(err_host, ex.getLocalizedMessage());
            exit(1);
        }
    }
    
    private OutputStream createTCPOutputStream(InetAddress addr, int port) throws IOException {
        Socket socket = new Socket(addr, port);
        return socket.getOutputStream();
    }
}
