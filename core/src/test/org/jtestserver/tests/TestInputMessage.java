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

import java.net.InetAddress;
import java.net.SocketAddress;

import org.jtestserver.common.protocol.Client;
import org.jtestserver.common.protocol.Protocol;
import org.jtestserver.common.protocol.ProtocolException;
import org.jtestserver.common.protocol.ReceivedMessage;
import org.jtestserver.common.protocol.Server;
import org.jtestserver.common.protocol.TimeoutException;
import org.junit.Ignore;


@Ignore 
public class TestInputMessage {
    
    private static class DummyProtocol extends Protocol<Object> {
        private String message;
        public void setMessage(String message) {
            this.message = message;
        }
        
        @Override
        public ReceivedMessage receiveMessage(Object socket) throws ProtocolException, TimeoutException {
            return new ReceivedMessage(message, null);
        }

        @Override        
        public void sendMessage(Object socket, String command, SocketAddress remoteAddress) 
            throws ProtocolException, TimeoutException {
            this.message = command;
        }

        /* (non-Javadoc)
         * @see org.jtestserver.common.protocol.Protocol#createClient(java.net.InetAddress, int)
         */
        @Override
        public Client<Object, ? extends Protocol<Object>> createClient(InetAddress serverIp,
                int serverPort) throws ProtocolException {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.jtestserver.common.protocol.Protocol#createServer(int)
         */
        @Override
        public Server<Object, ? extends Protocol<Object>> createServer(int localPort)
            throws ProtocolException {
            // TODO Auto-generated method stub
            return null;
        }
        
    }
    
    private static final DummyProtocol protocol = new DummyProtocol();
    
    
}
