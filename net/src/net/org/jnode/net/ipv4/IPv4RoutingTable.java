/*
 * $Id$
 */
package org.jnode.net.ipv4;

import java.net.NoRouteToHostException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * @author epr
 */
public class IPv4RoutingTable {
	
	/** All entries as instanceof IPv4Route */
	private final Vector entries = new Vector();
	
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
	 * @param index
	 */
	public IPv4Route get(int index) {
		return (IPv4Route)entries.get(index);
	}
	
	/**
	 * Add an entry
	 * @param entry
	 */
	public void add(IPv4Route entry) {
		entries.add(entry);
	}
	
	/**
	 * Remove a given entry
	 * @param entry
	 */
	public void remove(IPv4Route entry) {
		entries.remove(entry);
	}
	
	/**
	 * Get all entries
	 * @see IPv4Route
	 * @return a list of IPv4Route entries.
	 */
	public List entries() {
		return new ArrayList(entries);
	}
	
	/**
	 * Search for a route to the given destination
	 * @param destination
	 * @throws NoRouteToHostException No route has been found
	 * @return The route that has been selected.
	 */
	public IPv4Route search(IPv4Address destination) 
	throws NoRouteToHostException {
		while (true) {
			try {
				// First search for a matching host-address route
				for (Iterator i = entries.iterator(); i.hasNext(); ) {
					final IPv4Route r = (IPv4Route)i.next();
					if (r.isHost() && r.isUp()) {
						if (r.getDestination().equals(destination)) {
							return r;
						}
					}
				}
				// Not direct host found, search through the networks
				for (Iterator i = entries.iterator(); i.hasNext(); ) {
					final IPv4Route r = (IPv4Route)i.next();
					if (r.isNetwork() && r.isUp()) {
						if (r.getDestination().matches(destination, r.getSubnetmask())) {
							return r;
						}
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
		final StringBuffer b = new StringBuffer();
		for (Iterator i = entries.iterator(); i.hasNext(); ) {
			b.append(i.next());
			b.append('\n');
		}
		return b.toString();
	}
}
