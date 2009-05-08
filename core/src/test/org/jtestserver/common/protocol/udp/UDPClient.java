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
import java.net.InetAddress;
import java.net.SocketException;

import org.jtestserver.common.protocol.Client;
import org.jtestserver.common.protocol.ProtocolException;
import org.jtestserver.common.protocol.TimeoutException;

/**
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
class UDPClient implements Client<DatagramSocket, UDPProtocol> {
    private final UDPProtocol protocol;
    private final InetAddress serverIp;
    private final int serverPort;
    private final DatagramSocket socket;
    
    /**
     * @throws SocketException 
     * 
     */
    UDPClient(UDPProtocol protocol, InetAddress serverIp, int serverPort) throws SocketException {
        this.protocol = protocol;
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        socket = new DatagramSocket();
    }
    
    /* (non-Javadoc)
     * @see org.jtestserver.common.protocol.Client#send(java.lang.String)
     */
    @Override
    public synchronized String send(String message, boolean needReply) throws ProtocolException, TimeoutException {
        ensureConnected();
        protocol.sendMessage(socket, message, null);
        
        String reply = null;
        if (needReply) {
            reply = protocol.receiveMessage(socket).getMessage();
        }
        return reply;
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
    
    protected void ensureConnected() throws ProtocolException {
        if (socket.isClosed()) {
            throw new ProtocolException("connection is closed");
        }
        
        if (!socket.isConnected()) {
            socket.connect(serverIp, serverPort);
        }
    }
}
