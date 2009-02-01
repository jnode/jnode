/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
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
 
package org.jnode.shell.isolate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.net.UnknownHostException;

/**
 * The implementation class for IsolateSoccket.
 * 
 * @author crawley@jnode.org
 */
public class IsolateSocketImpl extends SocketImpl {
    
    private final InputStream in;
    private final OutputStream out;
    private boolean closed;
    private final InetAddress dummyAddress;
    
    private IsolateSocketImpl(InputStream in, OutputStream out) {
        super();
        this.in = in;
        this.out = out;
        InetAddress d;
        try {
            d = InetAddress.getByAddress(new byte[]{0, 0, 0, 0});
        } catch (UnknownHostException ex) {
            d = null;
        }
        this.dummyAddress = d;
    }

    public IsolateSocketImpl(InputStream in) {
        this(in, null);
    }

    public IsolateSocketImpl(OutputStream out) {
        this(null, out);
    }

    @Override
    protected void accept(SocketImpl s) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected int available() throws IOException {
        if (in == null) {
            throw new SocketException("socket is closed");
        }
        return in.available();
    }

    @Override
    protected InetAddress getInetAddress() {
        // We have to return something non-null because the generic Socket code uses
        // a non-null address to indicate that the socket is connected.
        return dummyAddress;
    }

    @Override
    protected void bind(InetAddress host, int port) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected synchronized void close() throws IOException {
        if (!closed) {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            closed = true;
        }
    }

    @Override
    protected void connect(String host, int port) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void connect(InetAddress host, int port) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void connect(SocketAddress address, int timeout) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void create(boolean stream) throws IOException {
        // Do nothing,
    }

    @Override
    protected synchronized InputStream getInputStream() throws IOException {
        if (closed || in == null) {
            throw new SocketException("socket is closed");
        }
        return in;
    }

    @Override
    protected synchronized OutputStream getOutputStream() throws IOException {
        if (closed || out == null) {
            throw new SocketException("socket is closed");
        }
        return out;
    }

    @Override
    protected void listen(int backlog) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void sendUrgentData(int data) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getOption(int optID) throws SocketException {
        // no options supported
        return null;
    }

    @Override
    public void setOption(int optID, Object value) throws SocketException {
        // no options supported
    }
}
