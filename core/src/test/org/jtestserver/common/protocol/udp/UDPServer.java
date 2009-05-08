/*
JTestServer is a client/server framework for testing any JVM implementation.
 
Copyright (C) 2009  Fabien DUMINY (fduminy@jnode.org)

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
package org.jtestserver.common.protocol.udp;

import java.net.DatagramSocket;
import java.net.SocketException;

import org.jtestserver.common.protocol.MessageProcessor;
import org.jtestserver.common.protocol.ProtocolException;
import org.jtestserver.common.protocol.ReceivedMessage;
import org.jtestserver.common.protocol.Server;
import org.jtestserver.common.protocol.TimeoutException;

/**
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
class UDPServer implements Server<DatagramSocket, UDPProtocol> {
    private final UDPProtocol protocol;
    private final DatagramSocket socket;
    
    /**
     * @throws SocketException 
     * 
     */
    UDPServer(UDPProtocol protocol, int localPort) throws SocketException {
        this.protocol = protocol;
        this.socket = new DatagramSocket(localPort);
    }
    
    /* (non-Javadoc)
     * @see org.jtestserver.common.protocol.Server#receive(org.jtestserver.common.protocol.MessageProcessor)
     */
    @Override
    public synchronized void receive(MessageProcessor processor) throws ProtocolException, TimeoutException {
        ReceivedMessage receivedMessage = protocol.receiveMessage(socket);
        String reply = processor.process(receivedMessage.getMessage());
        
        if (reply != MessageProcessor.NO_RESPONSE) {
            protocol.sendMessage(socket, reply, receivedMessage.getRemoteAddress());
        }
    }
    
    public void setTimeout(int timeout) throws ProtocolException {
        try {
            socket.setSoTimeout(timeout);
        } catch (SocketException se) {
            throw new ProtocolException(se);
        }
    }
    
    @Override
    public void close() {
        socket.disconnect();
        socket.close();
    }    
}
