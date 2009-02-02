/*
 * $Id$
 *
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
 
package org.jnode.net.ipv4.udp;

import gnu.java.net.PlainDatagramSocketImpl;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.net.NetworkInterface;
import java.io.IOException;
import java.io.FileDescriptor;

/**
 * Glue class for supporting gnu.java.nio.DatagramChannelImpl.
 *
 * @author Levente S\u00e1ntha
 */
public class PlainUDPDatagramSocketImpl extends PlainDatagramSocketImpl {

    private MyUDPDatagramSocketImpl udp;

    static class MyUDPDatagramSocketImpl extends UDPDatagramSocketImpl {
        MyUDPDatagramSocketImpl(UDPProtocol protocol) {
            super(protocol);
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
        }

        void setTimeToLive0(int ttl) {
            super.setTimeToLive(ttl);
        }

        int getTimeToLive0() throws IOException {
            return super.getTimeToLive();
        }

        void receive0(DatagramPacket pack) throws IOException {
            super.receive(pack);
        }

        void close0() {
            super.close();
        }

        void join0(InetAddress ineta) throws IOException {
            super.join(ineta);
        }

        void leave0(InetAddress addr) throws IOException {
            super.leave(addr);
        }

        public int peek0(InetAddress addr) throws IOException {
            return super.peek(addr);
        }

        public int peekData0(DatagramPacket packet) throws IOException {
            return super.peekData(packet);
        }

        public void joinGroup0(SocketAddress address, NetworkInterface netIf) throws IOException {
            super.joinGroup(address, netIf);
        }

        public void leaveGroup0(SocketAddress address, NetworkInterface netIf) throws IOException {
            super.leaveGroup(address, netIf);
        }

        @Override
        protected void disconnect() {
            super.disconnect();
        }

        @Override
        protected FileDescriptor getFileDescriptor() {
            return super.getFileDescriptor();
        }

    }

    public PlainUDPDatagramSocketImpl(UDPProtocol proto) {
        this.udp = new MyUDPDatagramSocketImpl(proto);
    }

    @Override
    protected void connect(InetAddress address, int port) throws SocketException {
        super.connect(address, port);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        udp.finalize();
    }

    @Override
    protected synchronized void bind(int port, InetAddress addr) throws SocketException {
        udp.doBind(port, addr);
    }

    @Override
    protected synchronized void create() throws SocketException {
        //nothing to do here
    }

    @Override
    protected synchronized void setTimeToLive(int ttl) throws IOException {
        udp.setTimeToLive0(ttl);
    }

    @Override
    protected synchronized int getTimeToLive() throws IOException {
        return udp.getTimeToLive0();
    }

    private final Object SEND_LOCK = new Object();

    @Override
    protected void send(DatagramPacket packet) throws IOException {
        //synchronized (SEND_LOCK){
        System.out.println("udp: " + udp);
        System.out.println("packet: " + packet);
        udp.send(packet);
        //}
    }

    private final Object RECEIVE_LOCK = new Object();

    @Override
    protected void receive(DatagramPacket packet) throws IOException {
        synchronized (RECEIVE_LOCK) {
            udp.receive0(packet);
        }
    }

    @Override
    public synchronized void setOption(int option_id, Object val) throws SocketException {
        udp.setOption(option_id, val);
    }

    @Override
    public synchronized Object getOption(int option_id) throws SocketException {
        return udp.getOption(option_id);
    }

    @Override
    protected synchronized void close() {
        udp.close0();
    }

    @Override
    protected synchronized byte getTTL() throws IOException {
        return udp.getTTL();
    }

    @Override
    protected synchronized void setTTL(byte ttl) throws IOException {
        udp.setTTL(ttl);
    }

    @Override
    protected synchronized void join(InetAddress addr) throws IOException {
        udp.join0(addr);
    }

    @Override
    protected synchronized void leave(InetAddress addr) throws IOException {
        udp.leave0(addr);
    }

    @Override
    protected synchronized int peek(InetAddress addr) throws IOException {
        return udp.peek0(addr);
    }

    @Override
    public int peekData(DatagramPacket packet) throws IOException {
        return udp.peekData0(packet);
    }

    @Override
    public void joinGroup(SocketAddress address, NetworkInterface netIf) throws IOException {
        udp.joinGroup0(address, netIf);
    }

    @Override
    public void leaveGroup(SocketAddress address, NetworkInterface netIf) throws IOException {
        udp.leaveGroup0(address, netIf);
    }

    @Override
    protected void disconnect() {
        udp.disconnect();
    }

    @Override
    protected FileDescriptor getFileDescriptor() {
        return udp.getFileDescriptor();
    }

    @Override
    protected int getLocalPort() {
        return super.getLocalPort();
    }
}
