/*
 * $Id$
 *
 * Copyright (C) 2003-2012 JNode.org
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
 
package org.jnode.driver.input;

/**
 * @author qades
 */
public interface MouseProtocolHandler {

    /**
     * Gets the name of the protocol of this handler.
     *
     * @return the protocol name
     */
    public String getName();

    /**
     * Does this protocol handler support a given mouse id.
     *
     * @param id
     * @return {@code true} if this handler supports the given id, {@code false} otherwise.
     */
    public boolean supportsId(int id);

    /**
     * Gets the size in bytes of a single packet in this protocol.
     *
     * @return the size of a packet
     */
    public int getPacketSize();

    /**
     * Create an event based on the supplied data packet.
     *
     * @param data a data packet
     * @return a PointerEvent
     */
    public PointerEvent buildEvent(byte[] data);

}
