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

import java.net.SocketException;

/**
 * OSI datalink layers must implement this interface.
 * 
 * @author epr
 */
public interface LinkLayer {

    /**
     * Gets the name of this layer
     */
    public String getName();

    /**
     * Gets the type of frames this layer handles
     */
    public int getType();

    /**
     * Process a packet that has been received and matches the type of this
     * layer.
     * 
     * @param skbuf
     * @throws SocketException
     */
    public void receive(SocketBuffer skbuf) throws SocketException;

}
