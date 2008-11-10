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
package org.jtestserver.common.protocol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

public class UDPProtocol implements Protocol {
    private static final int DEFAULT_PORT = 10000;

    private static final int INT_SIZE = 4; // size of an int in bytes
    
    private final DatagramSocket socket;
    private InetAddress remoteIp;
    private int remotePort;

    /**
     * Create an UDPProtocol for a server listening on {@link UDPProtocol#DEFAULT_PORT}
     * @return
     * @throws ProtocolException
     */
    public static UDPProtocol createServer() throws ProtocolException {
        return createServer(DEFAULT_PORT);
    }

    /**
     * Create an UDPProtocol for a server listening on given port
     * @param localPort
     * @return
     * @throws ProtocolException
     */
    public static UDPProtocol createServer(int localPort) throws ProtocolException {
        return new UDPProtocol(localPort);
    }
    
    /**
     * Create an UDPProtocol for a client of the server at specified address 
     * listening on {@link UDPProtocol#DEFAULT_PORT}
     * 
     * @param serverIp
     * @return
     * @throws ProtocolException
     */
    public static UDPProtocol createClient(InetAddress serverIp) throws ProtocolException {
        return createClient(serverIp, DEFAULT_PORT);
    }

    /**
     * Create an UDPProtocol for a client of the server at specified address and port
     * @param serverIp
     * @param serverPort
     * @return
     * @throws ProtocolException
     */
    public static UDPProtocol createClient(InetAddress serverIp, int serverPort) throws ProtocolException {
        return new UDPProtocol(serverIp, serverPort);
    }
    
    /**
     * Create an UDPProtocol for a server listening on given port.
     * Note : the constructor should be 'private' but we need it to simulate timeouts in the tests.
     * @param localPort
     * @throws ProtocolException
     */
    protected UDPProtocol(int localPort) throws ProtocolException {
        try {
            socket = new DatagramSocket(localPort);
        } catch (SocketException se) {
            throw new ProtocolException(se);
        }
    }

    /**
     * Create an UDPProtocol for a client of the server at specified address and port
     * Note : the constructor should be 'private' but we need it to simulate timeouts in the tests.
     * @param serverIp
     * @param serverPort
     * @throws ProtocolException
     */
    protected UDPProtocol(InetAddress serverIp, int serverPort) throws ProtocolException {
        try {
            socket = new DatagramSocket();
            this.remoteIp = serverIp;
            this.remotePort = serverPort;
        } catch (SocketException se) {
            throw new ProtocolException(se);
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
    public void send(String command) throws ProtocolException, TimeoutException {
        try {
            // send size of data
            ByteBuffer byteBuffer = ByteBuffer.allocate(INT_SIZE).putInt(command.length());
            byte[] data = byteBuffer.array();
            DatagramPacket packet = new DatagramPacket(data, data.length, remoteIp, remotePort);
            
            socket.send(packet);
            
            // send data
            data = command.getBytes();
            packet = new DatagramPacket(data, data.length, remoteIp, remotePort);
            socket.send(packet);
        } catch (SocketTimeoutException e) {
            throw new TimeoutException("timeout in receive", e);
        } catch (IOException e) {
            throw new ProtocolException("error in receive", e);
        }
    }

    @Override
    public String receive() throws ProtocolException, TimeoutException {
        try {
            // receive size of data
            byte[] data = new byte[INT_SIZE];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            socket.receive(packet);
            int size = ByteBuffer.wrap(data).getInt();

            // receive actual data
            data = new byte[size];
            packet = new DatagramPacket(data, data.length);
            socket.receive(packet);

            remoteIp = packet.getAddress();
            remotePort = packet.getPort();

            return new String(packet.getData());
        } catch (SocketTimeoutException e) {
            throw new TimeoutException("timeout in receive", e);
        } catch (IOException e) {
            throw new ProtocolException("error in receive", e);
        }
    }

    @Override
    public void close() {
        socket.disconnect();
        socket.close();
    }
}
