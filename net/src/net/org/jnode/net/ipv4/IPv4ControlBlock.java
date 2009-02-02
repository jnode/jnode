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

import java.net.BindException;
import java.net.SocketException;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class IPv4ControlBlock {

    /** The list I'm a part of */
    private final IPv4ControlBlockList list;

    /** The local address */
    private IPv4Address lAddr;

    /** The local port */
    private int lPort;

    /** The foreign address */
    private IPv4Address fAddr;

    /** The foreign port */
    private int fPort;

    /** The protocol to use in creating IPv4 headers */
    private final int protocol;

    /** Type of Service to use in creating IPv4 headers */
    private final int tos;

    /** Time to Live to use in creating IPv4 headers */
    private final int ttl;

    /**
     * Create a new instance
     * 
     * @param list
     * @param protocol
     * @param ttl
     */
    public IPv4ControlBlock(IPv4ControlBlockList list, int protocol, int ttl) {
        this.list = list;
        this.protocol = protocol;
        this.tos = 0;
        this.ttl = ttl;
        this.lAddr = IPv4Address.ANY;
        this.lPort = 0;
        this.fAddr = IPv4Address.ANY;
        this.fPort = 0;
    }

    /**
     * @return Returns the foreign address.
     */
    public final IPv4Address getForeignAddress() {
        return this.fAddr;
    }

    /**
     * @return Returns the foreign port.
     */
    public final int getForeignPort() {
        return this.fPort;
    }

    /**
     * @return Returns the local address.
     */
    public final IPv4Address getLocalAddress() {
        return this.lAddr;
    }

    /**
     * @return Returns the local port.
     */
    public final int getLocalPort() {
        return this.lPort;
    }

    /**
     * Match this control block against the given parameters
     * 
     * @param fAddr
     * @param fPort
     * @param lAddr
     * @param lPort
     * @param allowWildcards
     * @return -1 does not match, >= 0 matches, returns number of wildcards
     */
    final int match(IPv4Address fAddr, int fPort, IPv4Address lAddr, int lPort,
            boolean allowWildcards) {
        if (this.lPort != lPort) {
            // No match
            return -1;
        }

        int wildcard = 0;
        if (!this.lAddr.isAny()) {
            if (lAddr.isAny()) {
                wildcard++;
            } else if (!this.lAddr.equals(lAddr)) {
                // No match
                return -1;
            }
        } else {
            if (!lAddr.isAny()) {
                wildcard++;
            }
        }

        if (!this.fAddr.isAny()) {
            if (fAddr.isAny()) {
                wildcard++;
            } else if (!this.fAddr.equals(fAddr) || (this.fPort != fPort)) {
                // No match
                return -1;
            }
        } else {
            if (!fAddr.isAny()) {
                wildcard++;
            }
        }

        if (!allowWildcards && (wildcard > 0)) {
            // No wildcard match allowed
            return -1;
        }

        return wildcard;
    }

    /**
     * Bind to a specific local address.
     * 
     * @param lAddr
     * @param lPort
     */
    final void bind(IPv4Address lAddr, int lPort) throws BindException {
        if ((this.lPort != 0) || !this.lAddr.isAny()) {
            throw new BindException("ControlBlock already bound");
        }
        this.lAddr = lAddr;
        this.lPort = lPort;
    }

    /**
     * Connect to a foreign address.
     * 
     * @param lAddr
     * @param fAddr
     * @param fPort
     */
    public synchronized void connect(IPv4Address lAddr, IPv4Address fAddr, int fPort) {
        if (this.lAddr.isAny()) {
            if (lAddr.isAny()) {
                throw new IllegalArgumentException("Specific local address required");
            }
            this.lAddr = lAddr;
        } else if (!this.lAddr.equals(lAddr)) {
            throw new IllegalArgumentException("Different lAddr " + lAddr);
        }
        this.fAddr = fAddr;
        this.fPort = fPort;
    }

    /**
     * Create a new control block that has the same local address and port and
     * is connecto to the given foreign address.
     * 
     * @param lAddr
     * @param fAddr
     * @param fPort
     */
    public IPv4ControlBlock copyAndConnect(IPv4Address lAddr, IPv4Address fAddr, int fPort)
        throws SocketException {
        final IPv4ControlBlock copy = list.createControlBlock(this);
        copy.bind(lAddr, lPort);
        copy.connect(lAddr, fAddr, fPort);
        list.add(copy);
        return copy;
    }

    /**
     * Close any connection and remove from the control block list.
     */
    public synchronized void removeFromList() {
        this.list.remove(this);
    }

    /**
     * Create an IPv4 header for outgoing packets.
     * This control block must have been connected before calling this method.
     * The dataLength of the header is set to 0, this must be
     * changes before prefixing this header to a SocketBuffer.
     */
    protected IPv4Header createOutgoingIPv4Header() {
        return new IPv4Header(tos, ttl, protocol, fAddr, 0);
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "local " + lAddr + ":" + lPort + ", foreign " + fAddr + ":" + fPort;
    }

}
