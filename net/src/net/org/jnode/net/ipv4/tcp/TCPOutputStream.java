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
import java.io.OutputStream;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class TCPOutputStream extends OutputStream {

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
    public TCPOutputStream(TCPControlBlock controlBlock, TCPSocketImpl impl) {
        this.controlBlock = controlBlock;
        this.impl = impl;
        this.closed = false;
    }

    /**
     * @see java.io.OutputStream#close()
     */
    public void close() throws IOException {
        if (!this.closed) {
            this.closed = true;
            // Close the socket itself
            impl.close();
        }
    }

    /**
     * @see java.io.OutputStream#flush()
     */
    public void flush() throws IOException {
        // TODO Auto-generated method stub
        super.flush();
    }

    /**
     * @see java.io.OutputStream#write(byte[], int, int)
     */
    public final void write(byte[] b, int off, int len)
        throws IOException, NullPointerException, IndexOutOfBoundsException {
        if (closed) {
            throw new IOException("closed");
        } else {
            controlBlock.appSendData(b, off, len);
        }
    }

    /**
     * @see java.io.OutputStream#write(byte[])
     */
    public final void write(byte[] b) throws IOException, NullPointerException {
        write(b, 0, b.length);
    }

    /**
     * @see java.io.OutputStream#write(int)
     */
    public final void write(int b) throws IOException {
        final byte[] buf = new byte[1];
        buf[0] = (byte) b;
        write(buf, 0, 1);
    }

}
