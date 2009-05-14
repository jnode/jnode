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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.rmi.server.RemoteServer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jtestserver.common.protocol.Protocol;
import org.jtestserver.common.protocol.ProtocolException;
import org.jtestserver.common.protocol.ReceivedMessage;
import org.jtestserver.common.protocol.TimeoutException;

public class UDPProtocol extends Protocol<DatagramSocket> {
    private static final Logger LOGGER = Logger.getLogger(UDPProtocol.class.getName());
        
    private static final int MAX_SIZE = Integer.MAX_VALUE; // 1024 * 1024;

    //private static final int CHAR_SIZE = 2; // size of a char in bytes
    private static final int INT_SIZE = 4; // size of an int in bytes
    
    @Override
    public final UDPClient createClient(InetAddress serverIp, int serverPort) throws ProtocolException {
        try {
            return new UDPClient(this, serverIp, serverPort);
        } catch (SocketException e) {
            throw new ProtocolException(e);
        }
    }
    
    @Override
    public final UDPServer createServer(int localPort) throws ProtocolException {
        try {
            return new UDPServer(this, localPort);
        } catch (SocketException e) {
            throw new ProtocolException(e);
        }
    }

    @Override
    protected void sendMessage(DatagramSocket socket, String message, SocketAddress remoteAddress) 
        throws ProtocolException, TimeoutException {
        try {
            final byte[] bytes = message.getBytes(); 
            
            // send size of data
            ByteBuffer byteBuffer = ByteBuffer.allocate(INT_SIZE).putInt(bytes.length);
            byte[] data = byteBuffer.array();
            remoteAddress = (remoteAddress == null) ? socket.getRemoteSocketAddress() : remoteAddress;
            DatagramPacket packet = new DatagramPacket(data, data.length, remoteAddress);
                        
            socket.send(packet);
            
            LOGGER.log(Level.INFO, "nb bytes sent : " + bytes.length);
            
            // send data
            packet = new DatagramPacket(bytes, bytes.length, remoteAddress);
            socket.send(packet);
            
//            ByteBuffer bb = ByteBuffer.allocate(command.length() * CHAR_SIZE + INT_SIZE);
//            bb.putInt(command.length()).asCharBuffer().append(command);
//            socket.getChannel().send(bb, socket.getRemoteSocketAddress());
        } catch (SocketTimeoutException e) {
            throw new TimeoutException("timeout in receive", e);
        } catch (IOException e) {
            throw new ProtocolException("error in receive", e);
        }
    }

    @Override
    protected ReceivedMessage receiveMessage(DatagramSocket socket) throws ProtocolException, TimeoutException {
        try {
            // receive size of data
            byte[] data = new byte[INT_SIZE];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            socket.receive(packet);
            int size = ByteBuffer.wrap(data).getInt();

            LOGGER.log(Level.INFO, "nb bytes received : " + size);
            if (size > MAX_SIZE) {
                throw new ProtocolException(
                        "stream probably corrupted : received more than "
                        + MAX_SIZE + " bytes (" + size + ")");
            }
            
            // receive actual data
            data = new byte[size];
            packet = new DatagramPacket(data, data.length);
            socket.receive(packet);
            
            return new ReceivedMessage(new String(packet.getData()), packet.getSocketAddress());
            
//            ByteBuffer bb = ByteBuffer.allocate(INT_SIZE);
//            socket.getChannel().read(bb);
//            int size = bb.getInt();
//            bb = ByteBuffer.allocate(size);
//            socket.getChannel().read(bb);
//            
//            return bb.asCharBuffer().rewind().toString();
        } catch (SocketTimeoutException e) {
            throw new TimeoutException("timeout in receive", e);
        } catch (IOException e) {
            throw new ProtocolException("error in receive", e);
        }
    }
}
