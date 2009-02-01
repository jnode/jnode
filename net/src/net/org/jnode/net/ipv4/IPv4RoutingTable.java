/*
 * $Id$
 *
 * JNode.org
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

import java.net.NoRouteToHostException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Vector;

/**
 * @author epr
 */
public class IPv4RoutingTable {

    /** All entries as instanceof IPv4Route */
    private final Vector<IPv4Route> entries = new Vector<IPv4Route>();

    /**
     * Create a new instance
     */
    public IPv4RoutingTable() {
    }

    /**
     * Gets the number of entries
     */
    public int getSize() {
        return entries.size();
    }

    /**
     * Get an entry at a given index
     * 
     * @param index
     */
    public IPv4Route get(int index) {
        return (IPv4Route) entries.get(index);
    }

    /**
     * Add an entry
     * 
     * @param entry
     */
    public void add(IPv4Route entry) {
        entries.add(entry);
    }

    /**
     * Remove a given entry
     * 
     * @param entry
     */
    public void remove(IPv4Route entry) {
        entries.remove(entry);
    }

    /**
     * Get all entries
     * 
     * @see IPv4Route
     * @return a list of IPv4Route entries.
     */
    public List<IPv4Route> entries() {
        return new ArrayList<IPv4Route>(entries);
    }

    /**
     * Search for a route to the given destination
     * 
     * @param destination
     * @throws NoRouteToHostException No route has been found
     * @return The route that has been selected.
     */
    public IPv4Route search(IPv4Address destination) throws NoRouteToHostException {
        while (true) {
            try {
                // First search for a matching host-address route
                for (IPv4Route r : entries) {
                    if (r.isHost() && r.isUp()) {
                        if (r.getDestination().equals(destination)) {
                            return r;
                        }
                    }
                }
                // No direct host found, search through the networks
                for (IPv4Route r : entries) {
                    if (r.isNetwork() && r.isUp()) {
                        if (r.getDestination().matches(destination, r.getSubnetmask())) {
                            return r;
                        }
                    }
                }

                // No network found, search for the default gateway
                for (IPv4Route r : entries) {
                    if (r.isGateway() && r.isUp()) {
                        return r;
                    }
                }
                // No route found
                throw new NoRouteToHostException(destination.toString());
            } catch (ConcurrentModificationException ex) {
                // The list of entries was modified, while we are searching,
                // Just loop and try it again
            }
        }
    }

    /**
     * Convert to a String representation
     * @see java.lang.Object#toString()
     */
    public String toString() {
        final StringBuilder b = new StringBuilder();
        for (IPv4Route r : entries) {
            b.append(r);
            b.append('\n');
        }
        return b.toString();
    }
}
