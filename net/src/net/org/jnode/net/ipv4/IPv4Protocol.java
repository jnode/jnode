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
 
package org.jnode.net.ipv4;

import java.net.SocketException;

import org.jnode.net.SocketBuffer;
import org.jnode.net.TransportLayer;

/**
 * Interface for protocols within IP (like TCP, ICMP)
 * 
 * @author epr
 */
public interface IPv4Protocol extends TransportLayer {

    /**
     * Process an ICMP error message that has been received and matches this
     * protocol. The skbuf is position directly after the ICMP header (thus
     * contains the error IP header and error transport layer header). The
     * transportLayerHeader property of skbuf is set to the ICMP message header.
     * 
     * @param skbuf
     * @throws SocketException
     */
    public void receiveError(SocketBuffer skbuf) throws SocketException;
}
