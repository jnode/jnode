/*
 * $Id$
 */
package org.jnode.net;

/**
 * Interface for network hardware addresses, such as an ethernet address.
 * @author epr
 */
public interface HardwareAddress {

	/**
	 * Is this address equal to the given address.
	 * @param o
	 */
	public boolean equals(HardwareAddress o);
	
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
	 * Is this a broadcast address?
	 */
	public boolean isBroadcast();

	/**
	 * Gets the default broadcast address for this kind of hardware address.
	 */
	public HardwareAddress getDefaultBroadcastAddress();
	
	/**
	 * Gets the type of this address.
	 * This type is used by (e.g.) ARP.
	 */
	public int getType();
	
}
