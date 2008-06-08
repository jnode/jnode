/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 
package org.jnode.net.ipv4.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.net.SocketOptions;

import org.apache.log4j.Logger;
import org.jnode.net.ipv4.IPv4Address;

/**
 * @author epr
 */
public class TCPSocketImpl extends SocketImpl {

    /**
     * The protocol I'm using
     */
    private final TCPProtocol protocol;

    /**
     * The control block
     */
    private TCPControlBlock controlBlock;

    /**
     * The output stream
     */
    private TCPOutputStream os;

    /**
     * The input stream
     */
    private TCPInputStream is;

    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(TCPSocketImpl.class);

    /**
     * Initialize a new instance
     * 
     * @param protocol
     */
    public TCPSocketImpl(TCPProtocol protocol) {
        this.protocol = protocol;
    }

    /**
     * Accepts a connection on this socket.
     * 
     * @param s The implementation object for the accepted connection.
     * @see java.net.SocketImpl#accept(java.net.SocketImpl)
     */
    protected void accept(SocketImpl s) throws IOException {
        log.debug("accept " + s);
        if (controlBlock == null) {
            throw new IOException("Not listening");
        }
        final TCPSocketImpl impl = (TCPSocketImpl) s;
        log.debug("accept: blocking");
        impl.controlBlock = controlBlock.appAccept();
        log.debug("accept: got one");
    }

    protected int getLocalPort() {
        log.debug("getLocalPort: controlBlock.getLocalPort()");
        return controlBlock.getLocalPort();
    }

    /**
     * @see java.net.SocketImpl#available()
     */
    protected final int available() throws IOException {
        return getInputStream().available();
    }

    /**
     * @see java.net.SocketImpl#bind(java.net.InetAddress, int)
     */
    protected void bind(InetAddress host, int port) throws IOException {
        if (controlBlock != null) {
            throw new IOException("Already bound");
        }
        if (host.isAnyLocalAddress()) {
            host = InetAddress.getLocalHost();
        }
        controlBlock = protocol.bind(new IPv4Address(host), port);
    }

    /**
     * @see java.net.SocketImpl#close()
     */
    protected synchronized void close() throws IOException {
        if (is != null) {
            is.close();
        }
        if (os != null) {
            os.close();
        }
        if (controlBlock != null) {
            controlBlock.appClose();
            controlBlock = null;
        }
    }

    /**
     * @see java.net.SocketImpl#connect(java.net.InetAddress, int)
     */
    protected final void connect(InetAddress host, int port) throws IOException {
        connect(new InetSocketAddress(host, port), 0);
    }

    /**
     * @see java.net.SocketImpl#connect(java.net.SocketAddress, int)
     */
    protected void connect(SocketAddress address, int timeout) throws IOException {
        if (!(address instanceof InetSocketAddress)) {
            throw new IOException("InetSocketAddress expected");
        }
        final InetSocketAddress sa = (InetSocketAddress) address;
        if (controlBlock == null) {
            bind(InetAddress.getLocalHost(), 0);
        }
        controlBlock.appConnect(new IPv4Address(sa.getAddress()), sa.getPort());
    }

    /**
     * @see java.net.SocketImpl#connect(java.lang.String, int)
     */
    protected final void connect(String host, int port) throws IOException {
        connect(InetAddress.getByName(host), port);
    }

    /**
     * @see java.net.SocketImpl#create(boolean)
     */
    protected void create(boolean stream) throws IOException {
        // Do nothing yet
    }

    /**
     * @see java.net.SocketImpl#getInputStream()
     */
    protected InputStream getInputStream() throws IOException {
        if (controlBlock == null) {
            throw new IOException("Connect first");
        }
        if (is == null) {
            is = new TCPInputStream(controlBlock, this);
        }
        return is;
    }

    /**
     * @see java.net.SocketOptions#getOption(int)
     */
    public Object getOption(int option_id) throws SocketException {
        switch (option_id) {
            case SocketOptions.SO_BINDADDR:
                return controlBlock.getLocalAddress().toInetAddress();
            case SocketOptions.SO_RCVBUF:
                return controlBlock.getReceiveBufferSize();
            case SocketOptions.SO_SNDBUF:
                return controlBlock.getSendBufferSize();
            case SocketOptions.SO_TIMEOUT:
                // todo implement it, 0 means disabled
                return 0;
            default:
                throw new SocketException("Option " + option_id +
                        " is not recognised or not implemented");
        }
    }

    /**
     * @see java.net.SocketImpl#getOutputStream()
     */
    protected OutputStream getOutputStream() throws IOException {
        if (controlBlock == null) {
            throw new IOException("Connect first");
        }
        if (os == null) {
            os = new TCPOutputStream(controlBlock, this);
        }
        return os;
    }

    /**
     * Starts listening for connections on a socket. The backlog parameter is
     * how many pending connections will queue up waiting to be serviced before
     * being accept'ed. If the queue of pending requests exceeds this number,
     * additional connections will be refused.
     * 
     * @param backlog The length of the pending connection queue
     * @throws IOException If an error occurs
     * @see java.net.SocketImpl#listen(int)
     */
    protected void listen(int backlog) throws IOException {
        if (controlBlock == null) {
            throw new IOException("Call bind first");
        }
        controlBlock.appListen();
    }

    /**
     * @see java.net.SocketImpl#sendUrgentData(int)
     */
    protected void sendUrgentData(int data) throws IOException {
        // TODO Auto-generated method stub

    }

    /**
     * @see java.net.SocketOptions#setOption(int, java.lang.Object)
     */
    public void setOption(int option_id, Object val) throws SocketException {
        // TODO Auto-generated method stub

    }

    /**
     * @see java.net.SocketImpl#shutdownInput()
     */
    protected final void shutdownInput() throws IOException {
        getInputStream().close();
    }

    /**
     * @see java.net.SocketImpl#shutdownOutput()
     */
    protected final void shutdownOutput() throws IOException {
        getOutputStream().close();
    }

    /**
     * @see java.net.SocketImpl#getInetAddress()
     */
    protected InetAddress getInetAddress() {
        if (controlBlock != null) {
            return controlBlock.getForeignAddress().toInetAddress();
        } else {
            return null;
        }
    }

    /**
     * @see java.net.SocketImpl#getPort()
     */
    protected int getPort() {
        if (controlBlock != null) {
            return controlBlock.getForeignPort();
        } else {
            return 0;
        }
    }

}
