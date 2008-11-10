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

import static org.jtestserver.tests.TestUtils.IP;
import static org.jtestserver.tests.TestUtils.PORT;

import java.util.Arrays;
import java.util.List;

import org.jtestserver.common.protocol.Protocol;
import org.jtestserver.common.protocol.ProtocolException;
import org.jtestserver.common.protocol.TimeoutException;
import org.jtestserver.common.protocol.UDPProtocol;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestProtocol {
    @Parameters
    public static List<Protocol[]> getProtocols() throws ProtocolException {
        return Arrays.asList(new Protocol[][] {{UDPProtocol.createServer(PORT), UDPProtocol.createClient(IP, PORT)}});
    }
    
    private final Protocol server;
    private final Protocol client;
    
    public TestProtocol(Protocol server, Protocol client) {
        this.server = server;
        this.client = client;
    }

    @Test
    public void testSendReceive() throws ProtocolException, TimeoutException {
        sendReceive("A Message");
    }
    
    @Test
    public void testSendReceiveBlank() throws ProtocolException, TimeoutException {
        sendReceive("  ");
    }
    
    @Test
    public void testSendReceiveEmpty() throws ProtocolException, TimeoutException {
        sendReceive("");
    }
    
    @Test(expected = NullPointerException.class)
    public void testSendReceiveNull() throws ProtocolException, TimeoutException {
        sendReceive(null);
    }

    private void sendReceive(String message) throws ProtocolException, TimeoutException {
        TestUtils.sendReceive(client, message, server);
    }
}
