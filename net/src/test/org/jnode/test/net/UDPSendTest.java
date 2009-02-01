/*
 * $Id$
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
 
package org.jnode.test.net;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import org.jnode.net.ipv4.IPv4Address;

/**
 * @author epr
 */
public class UDPSendTest {

    public static void main(String[] args) throws Exception {

        final DatagramSocket socket = new DatagramSocket();
        try {
            final int size;
            if (args.length > 1) {
                size = Integer.parseInt(args[1]);
            } else {
                size = 5000;
            }

            System.out.println("SendBufferSize=" + socket.getSendBufferSize());
            final byte[] buf = new byte[size];
            final DatagramPacket dp = new DatagramPacket(buf, buf.length);

            dp.setPort(2237);
            dp.setAddress(new IPv4Address(args[0]).toInetAddress());

            System.out.println("Sending packet of size " + dp.getLength());
            socket.send(dp);
        } finally {
            socket.close();
        }
    }
}
