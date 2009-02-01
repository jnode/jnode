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
 
package org.jnode.debug;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author crawley@jnode.org
 */
public class RemoteReceiver {
    public static final int DEFAULT_PORT = 5612;

    private static class UDPReceiver {
        private final DatagramSocket socket;
        private final OutputStream out;

        public UDPReceiver(int port, OutputStream out)
            throws IOException {
            final SocketAddress address = new InetSocketAddress(port);
            this.socket = new DatagramSocket(address);
            this.out = out;
        }

        public void run()
            throws IOException {
            final byte[] buf = new byte[2 * 4096];
            final DatagramPacket p = new DatagramPacket(buf, buf.length);
            while (true) {
                socket.receive(p);
                System.out.write(p.getData(), p.getOffset(), p.getLength());
                System.out.flush();
                if (out != null) {
                    out.write(p.getData(), p.getOffset(), p.getLength());
                    out.flush();
                }
            }
        }

    }

    private static class TCPReceiver {
        private final OutputStream out;
        private final int port;

        public TCPReceiver(int port, OutputStream out)
            throws IOException {
            this.out = out;
            this.port = port;
        }

        public void run()
            throws IOException {
            ServerSocket serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(port));
            Socket socket = serverSocket.accept();
            InputStream is = socket.getInputStream();
            socket.shutdownOutput();
            final byte[] buf = new byte[2 * 4096];
            while (true) {
                int count = is.read(buf);
                if (count == -1) {
                    System.out.println("Remote stream closed");
                    System.out.flush();
                    return;
                }
                System.out.write(buf, 0, count);
                System.out.flush();
                if (out != null) {
                    out.write(buf, 0, count);
                    out.flush();
                }
            }
        }

    }


    public static void main(String[] args)
        throws IOException {
        if (args.length > 0 && (args[0].equals("--help") || args[0].equals("-h"))) {
            System.err.println("Usage: receiver [--udp] [<port> [<out-file>]]");
            return;
        }
        int i = 0;
        final boolean udp = args.length > 0 && args[0].equals("--udp");
        if (udp) {
            i++;
        }
        final int port = (args.length > i) ? Integer.parseInt(args[i++]) : DEFAULT_PORT;
        final String fname = (args.length > i) ? args[i++] : null;
        OutputStream out = (fname != null) ? new FileOutputStream(fname, true) : null;
        if (udp) {
            new UDPReceiver(port, out).run();
        } else {
            new TCPReceiver(port, out).run();
        }
    }
}
