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
 
package org.jnode.debug;

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class UDPOutputStream extends OutputStream {

    private final DatagramSocket socket;

    private final SocketAddress address;

    private boolean inWrite = false;

    /**
     * Create a new instance
     * 
     * @param address
     * @throws SocketException
     */
    public UDPOutputStream(SocketAddress address) throws SocketException {
        socket = new DatagramSocket();
        this.address = address;
    }

    /**
     * @see java.io.OutputStream#close()
     * @throws IOException
     */
    public void close() throws IOException {
      socket.close();
      super.close();
    }

    /**
     * @param b
     * @param off
     * @param len
     * @see java.io.OutputStream#write(byte[], int, int)
     * @throws IOException
     * @throws NullPointerException
     * @throws IndexOutOfBoundsException
     */
    public void write(final byte[] b, final int off, final int len)
            throws IOException, NullPointerException, IndexOutOfBoundsException {
        if (!inWrite) {
            inWrite = true;
            try {
                AccessController.doPrivileged(new PrivilegedExceptionAction() {
                    public Object run() throws IOException {
                        final DatagramPacket p = new DatagramPacket(b, off, len);
                        p.setSocketAddress(address);
                        socket.send(p);
                        return null;
                    }
                });
            } catch (PrivilegedActionException ex) {
                final IOException ioe = new IOException();
                ioe.initCause(ex.getException());
                throw ioe;
            } finally {
                inWrite = false;
            }
        }
    }

    /**
     * @param b
     * @see java.io.OutputStream#write(byte[])
     * @throws IOException
     * @throws NullPointerException
     */
    public void write(byte[] b) throws IOException, NullPointerException {
        write(b, 0, b.length);
    }

    /**
     * @param b
     * @see java.io.OutputStream#write(int)
     * @throws IOException
     */
    public void write(int b) throws IOException {
        write(new byte[] { (byte) b }, 0, 1);
    }

}
