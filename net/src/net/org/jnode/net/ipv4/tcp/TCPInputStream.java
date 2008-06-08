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

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class TCPInputStream extends InputStream {

    /** The control block used to send data to */
    private final TCPControlBlock controlBlock;

    /** Has this stream been closed? */
    private boolean closed;

    /** The SocketImpl */
    private final TCPSocketImpl impl;

    /**
     * Create a new instance
     * 
     * @param controlBlock
     */
    public TCPInputStream(TCPControlBlock controlBlock, TCPSocketImpl impl) {
        this.controlBlock = controlBlock;
        this.impl = impl;
        this.closed = false;
    }

    /**
     * @see java.io.InputStream#available()
     */
    public final int available() throws IOException {
        if (closed) {
            return 0;
        } else {
            return controlBlock.appAvailable();
        }
    }

    /**
     * @see java.io.InputStream#close()
     */
    public final void close() throws IOException {
        if (!this.closed) {
            this.closed = true;
            // Close the socket itself
            impl.close();
        }
    }

    /**
     * @see java.io.InputStream#read()
     */
    public final int read() throws IOException {
        final byte[] buf = new byte[1];
        if (read(buf, 0, 1) == 1) {
            return buf[0] & 0xff;
        } else {
            return -1;
        }
    }

    /**
     * @see java.io.InputStream#read(byte[], int, int)
     */
    public final int read(byte[] b, int off, int len) throws IOException {
        if (closed) {
            return -1;
        } else {
            return controlBlock.appRead(b, off, len);
        }
    }

    /**
     * @see java.io.InputStream#read(byte[])
     */
    public final int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

}
