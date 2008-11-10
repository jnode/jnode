/*
JTestServer is a client/server framework for testing any JVM implementation.
 
Copyright (C) 2008  Fabien DUMINY (fduminy@jnode.org)

JTestServer is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

JTestServer is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package org.jtestserver.tests;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.jtestserver.common.protocol.Protocol;
import org.jtestserver.common.protocol.ProtocolException;
import org.jtestserver.common.protocol.TimeoutException;

class TestUtils {
    static final int PORT = 11000; // use a different port than default one
    static final InetAddress IP;
    static final InetAddress UNKNOWN_IP;
    
    static {
        try {
            IP = InetAddress.getLocalHost();
            UNKNOWN_IP = InetAddress.getByAddress(new byte[]{123, 123, 123, 123});
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    static void sendReceive(Protocol client, String message, Protocol server) 
        throws ProtocolException, TimeoutException {
        sendReceive(client, message, server, 0); // by default, no server delay (0)
    }

    static void sendReceive(Protocol client, String message, Protocol server, int serverDelay)
        throws ProtocolException, TimeoutException {
        client.send(message);

        if (serverDelay > 0) {
            try {
                Thread.sleep(serverDelay);
            } catch (InterruptedException e) {
                // ignore
            }
        }
        
        String receivedMessage = server.receive();
        assertEquals(message, receivedMessage);
    }
}
