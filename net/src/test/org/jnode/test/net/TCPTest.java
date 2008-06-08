/*
 * $Id$
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
 
package org.jnode.test.net;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A very trivial HTTP server, which actually only receives the HTTP request and
 * then closes the connection.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class TCPTest {

    public static void main(String[] args) throws Exception {
        // Listen to http messages
        final ServerSocket socket = new ServerSocket(80);
        try {
            for (int i = 0; i < 5; i++) {
                Socket s = socket.accept();
                System.out.println("Received call on port 80 from " + s.getRemoteSocketAddress());

                final InputStream is = s.getInputStream();
                final BufferedReader in = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println(line);
                    if (line.length() == 0) {
                        break;
                    }
                }
                System.out.println("Got EOF or blank line");

                s.close();
            }

            System.out.println("I'm stopping now");
        } finally {
            socket.close();
        }
    }
}
