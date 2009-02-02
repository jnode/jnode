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
 
package org.jnode.net;

import java.net.InetAddress;

/**
 * Interface for network protocol addresses, such as an IP address.
 * @author epr
 */
public interface ProtocolAddress {

    /**
     * Is this address equal to the given address.
     * @param o
     */
    public boolean equals(ProtocolAddress o);

    /**
     * Gets the length of this address in bytes
     */
    public int getLength();

    /**
     * Gets the address-byte at a given index
     * @param index
     */
    public byte get(int index);

    /**
     * Write this address to a given offset in the given buffer
     * @param skbuf
     * @param skbufOffset
     */
    public void writeTo(SocketBuffer skbuf, int skbufOffset);

    /**
     * Gets the type of this address.
     * This type is used by (e.g.) ARP.
     */
    public int getType();

    /**
     * Convert to a java.net.InetAddress
     * @see java.net.InetAddress
     * @return This address as java.net.InetAddress
     */
    public InetAddress toInetAddress();

    /**
     * Convert to a new byte array.
     * @return This address as byte array.
     */ 
    public byte[] toByteArray();
}
