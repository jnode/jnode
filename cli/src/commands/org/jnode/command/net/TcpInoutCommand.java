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
 
package org.jnode.command.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.HostNameArgument;
import org.jnode.shell.syntax.PortNumberArgument;

/**
 * This command establishes a TCP connection to a remote machine, either by
 * connecting to it or accepting a remote connection.  Once the connection has
 * been set up, it sends the command's standard input to the remote connection and
 * sends the output from the connection to the command's standard output.
 * 
 * @author quades
 * @author crawley@jnode.org
 */
public class TcpInoutCommand extends AbstractCommand {
    // FIXME this command is only useful for testing. What we Really need is
    // implementations of TELNET, RSH and SSH protocols (client and
    // server-side).

    private static final String help_host = "the hostname of the server to contact";
    private static final String help_port = "the port the server is listening to";
    private static final String help_lport = "the local port we should listen to";
    private static final String help_super = "Set up an interactive TCP connection to a remote machine";
    
    private final HostNameArgument argHost;
    private final PortNumberArgument argPort;
    private final PortNumberArgument argLocalPort;

    private Socket socket;
    private CopyThread toThread;
    private CopyThread fromThread;

    public TcpInoutCommand() {
        super(help_super);
        argHost      = new HostNameArgument("host", Argument.OPTIONAL, help_host);
        argPort      = new PortNumberArgument("port", Argument.OPTIONAL, help_port);
        argLocalPort = new PortNumberArgument("localPort", Argument.OPTIONAL, help_lport);
        registerArguments(argHost, argLocalPort, argPort);
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        new TcpInoutCommand().execute(args);
    }

    public void execute() throws IOException {
        Socket socket;
        if (argLocalPort.isSet()) {
            int port = argLocalPort.getValue();
            ServerSocket ss = ServerSocketFactory.getDefault().createServerSocket(port);
            socket = ss.accept();
        } else {
            InetAddress host = argHost.getAsInetAddress();
            int port = argPort.getValue();
            socket = SocketFactory.getDefault().createSocket(host, port);
        }
        InputStream in = getInput().getInputStream();
        OutputStream out = getOutput().getOutputStream();
        PrintWriter err = getError().getPrintWriter();
        toThread = new CopyThread(in, socket.getOutputStream(), err);
        fromThread = new CopyThread(socket.getInputStream(), out, err);

        synchronized (this) {
            toThread.start();
            fromThread.start();
            try {
                wait();
            } catch (InterruptedException e) {
                close(null);
            }
        }
    }

    private synchronized void close(CopyThread source) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                // We don't care ...
            }
            socket = null;
            notifyAll();
        }
        if (source != toThread) {
            toThread.terminate();
        }
        if (source != fromThread) {
            fromThread.terminate();
        }
    }

    private class CopyThread extends Thread {
        private final InputStream in;
        private final OutputStream out;
        private final PrintWriter err;
        private boolean terminated;

        CopyThread(InputStream in, OutputStream out, PrintWriter err) {
            this.in = in;
            this.out = out;
            this.err = err;
        }

        synchronized void terminate() {
            if (!this.terminated) {
                interrupt();
                this.terminated = true;
            }
        }

        public void run() {
            try {
                while (socket != null) {
                    int b = in.read();
                    if (b == -1) {
                        break;
                    }
                    out.write(b);
                }
            } catch (IOException ex) {
                synchronized (this) {
                    if (!terminated) {
                        err.println(ex.getLocalizedMessage());
                    }
                }
            } finally {
                close(this);
            }
        }
    }
}
