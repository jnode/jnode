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
import java.util.Iterator;
import java.util.LinkedList;

/**
 * A list of IPv4ControlBlock's.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class IPv4ControlBlockList {

    protected final LinkedList<IPv4ControlBlock> list = new LinkedList<IPv4ControlBlock>();
    private int lastFreePort = IPv4Constants.IPPORT_RESERVED;

    /**
     * Lookup the best matching control block for the given parameters.
     * 
     * @param fAddr
     * @param fPort
     * @param lAddr
     * @param lPort
     * @param allowWildcards
     * @return Null if no match, the best matching Control Block otherwise.
     */
    public IPv4ControlBlock lookup(IPv4Address fAddr, int fPort, IPv4Address lAddr, int lPort,
            boolean allowWildcards) {

        IPv4ControlBlock bestcb = null;
        int bestmatch = Integer.MAX_VALUE;

        for (IPv4ControlBlock cb : list) {
            final int match = cb.match(fAddr, fPort, lAddr, lPort, allowWildcards);
            if (match == 0) {
                // Exact match
                return cb;
            } else if ((match >= 0) && (match < bestmatch)) {
                bestmatch = match;
                bestcb = cb;
            }
        }
        return bestcb;
    }

    /**
     * Create a binding for a local address & port.
     * 
     * @param lAddr
     * @param lPort
     * @return The created binding
     */
    public synchronized IPv4ControlBlock bind(IPv4Address lAddr, int lPort) throws BindException {
        if (lPort != 0) {
            // Specific local port
            if (lookup(IPv4Address.ANY, 0, lAddr, lPort, true) != null) {
                throw new BindException("Address already in use");
            }
        } else {
            // Choose free port
            lPort = lastFreePort;
            do {
                lPort++;
                if ((lPort < IPv4Constants.IPPORT_RESERVED) ||
                        (lPort > IPv4Constants.IPPORT_USERRESERVED)) {
                    lPort = IPv4Constants.IPPORT_RESERVED;
                }
            } while (lookup(IPv4Address.ANY, 0, lAddr, lPort, true) != null);
            lastFreePort = lPort;
        }
        final IPv4ControlBlock cb = createControlBlock(null);
        cb.bind(lAddr, lPort);
        list.add(cb);
        return cb;
    }

    /**
     * Create an implementation specific control block.
     * 
     * @return The created control block
     */
    protected abstract IPv4ControlBlock createControlBlock(IPv4ControlBlock parent);

    /**
     * Add a block to the list
     * @param cb
     */
    final synchronized void add(IPv4ControlBlock cb) {
        list.add(cb);
    }

    /**
     * Remove a block from the list
     * @param cb
     */
    final synchronized void remove(IPv4ControlBlock cb) {
        list.remove(cb);
    }

    /**
     * Create an iterator over all entries
     * @return The iterator
     */
    protected Iterator<IPv4ControlBlock> iterator() {
        return list.iterator();
    }
}
