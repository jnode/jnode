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
 
package org.jnode.net.ipv4.udp;

import gnu.java.net.PlainDatagramSocketImpl;
import gnu.java.net.PlainDatagramSocketImplFactory;

import java.net.DatagramSocketImpl;

/**
 * @author epr
 */
public class UDPDatagramSocketImplFactory implements PlainDatagramSocketImplFactory {

    private final UDPProtocol protocol;

    /**
     * Create a new instance
     * 
     * @param protocol
     */
    public UDPDatagramSocketImplFactory(UDPProtocol protocol) {
        this.protocol = protocol;
    }

    /**
     * @see java.net.DatagramSocketImplFactory#createDatagramSocketImpl()
     */
    public DatagramSocketImpl createDatagramSocketImpl() {
        return new UDPDatagramSocketImpl(protocol);
    }

    /**
     * @see java.net.DatagramSocketImplFactory#createDatagramSocketImpl()
     */
    public PlainDatagramSocketImpl createPlainDatagramSocketImpl() {
        return new PlainUDPDatagramSocketImpl(protocol);
    }

}
