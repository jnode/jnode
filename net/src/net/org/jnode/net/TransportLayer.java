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
 
package org.jnode.net;

import java.net.DatagramSocketImplFactory;
import java.net.SocketException;
import java.net.SocketImplFactory;

import org.jnode.util.Statistics;

/**
 * OSI transport layers must implement this interface.
 * 
 * @author epr
 */
public interface TransportLayer {

    /**
     * Gets the name of this type
     */
    public String getName();

    /**
     * Gets the protocol ID this layer handles
     */
    public int getProtocolID();

    /**
     * Gets the statistics of this protocol
     */
    public Statistics getStatistics();

    /**
     * Process a packet that has been received and matches getType()
     * @param skbuf
     * @throws SocketException
     */
    public void receive(SocketBuffer skbuf) throws SocketException;

    /**
     * Gets the SocketImplFactory of this protocol.
     * @throws SocketException If this protocol is not Socket based.
     */
    public SocketImplFactory getSocketImplFactory() throws SocketException;

    /**
     * Gets the DatagramSocketImplFactory of this protocol.
     * @throws SocketException If this protocol is not DatagramSocket based.
     */
    public DatagramSocketImplFactory getDatagramSocketImplFactory() throws SocketException;
}
