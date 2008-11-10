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

import org.jtestserver.common.protocol.ProtocolException;
import org.jtestserver.common.protocol.TimeoutException;
import org.junit.Test;


public class TestUDPProtocol {
    private static final int DISABLE_TIMEOUT_VALUE = 0;
    
    /**
     * set a very small value so that we go in timeout
     */
    private static final int SMALL_TIMEOUT_VALUE = 1;
    
    /**
     * delay for processing the message. 
     * must be bigger than {@link TestUDPProtocol#SMALL_TIMEOUT_VALUE}
     */
    private static final int SERVER_DELAY = SMALL_TIMEOUT_VALUE + 10;
    
    @Test(expected = TimeoutException.class)    
    public void testSendReceiveWithServerTimeout() throws ProtocolException, TimeoutException {
//TODO find a way to simulate a timeout by software        
//        client.setTimeout(DISABLE_TIMEOUT_VALUE);
//        server.setTimeout(SMALL_TIMEOUT_VALUE);
//        
//        sendReceive(client, "test", server, SERVER_DELAY);
    }
    
    @Test(expected = TimeoutException.class)    
    public void testSendReceiveWithClientTimeout() throws ProtocolException, TimeoutException {
//TODO find a way to simulate a timeout by software        
//        final int serverPort = PORT + 1;
//        UDPProtocol client = new UDPProtocol(IP, serverPort) {
//            
//            @Override
//            public void send(String command) throws ProtocolException, TimeoutException {
//                super.send(command);
//            }  
//        };
//        client.setTimeout(SMALL_TIMEOUT_VALUE);
//        
//        UDPProtocol server = UDPProtocol.createServer(serverPort);
//        server.setTimeout(DISABLE_TIMEOUT_VALUE);
//        
//        sendReceive(client, "test", null);
    }
}
