/*
 * $Id$
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
}
