/*
 * $Id$
 */
package org.jnode.net.ethernet;

import org.jnode.net.HardwareAddress;
import org.jnode.net.LinkLayerHeader;
import org.jnode.net.SocketBuffer;

/**
 * @author epr
 */
public class EthernetHeader implements LinkLayerHeader, EthernetConstants {

	private final EthernetAddress destination;
	private final EthernetAddress source;
	private final int lengthType;

	/**
	 * Create a new instance
	 * @param destination
	 * @param source
	 * @param lengthType
	 */
	public EthernetHeader(
		EthernetAddress destination,
		EthernetAddress source,
		int lengthType) {
		this.destination = destination;
		this.source = source;
		this.lengthType = lengthType;
	}

	/**
	 * Create a new instance
	 * @param skbuf
	 */
	public EthernetHeader(SocketBuffer skbuf) {
		this.destination = new EthernetAddress(skbuf, 0);
		this.source = new EthernetAddress(skbuf, 6);
		this.lengthType = skbuf.get16(12);
	}
	
	/**
	 * Gets the length of this header in bytes
	 */
	public int getLength() {
		return ETH_HLEN;
	}

	/**
	 * Prefix this header to the front of the given buffer
	 * @param skbuf
	 */
	public void prefixTo(SocketBuffer skbuf) {
		skbuf.insert(ETH_HLEN);
		destination.writeTo(skbuf, 0);
		source.writeTo(skbuf, 6);
		skbuf.set16(12, lengthType);
	}
	
	/**
	 * Finalize the header in the given buffer.
	 * This method is called when all layers have set their header data
	 * and can be used e.g. to update checksum values.
	 * 
	 * @param skbuf The buffer
	 * @param offset The offset to the first byte (in the buffer) of this header (since low layer headers are already prefixed)
	 */
	public void finalizeHeader(SocketBuffer skbuf, int offset) {
		// Do nothing
	}

	/**
	 * Gets the source address of the packet described in this header 
	 */
	public HardwareAddress getSourceAddress() {
		return source;
	}

	/**
	 * Gets the source address of the packet described in this header 
	 */
	public HardwareAddress getDestinationAddress() {
		return destination;
	}
	
	/**
	 * Gets the destination address
	 */
	public EthernetAddress getDestination() {
		return destination;
	}

	/**
	 * Gets the length/type field
	 */
	public int getLengthType() {
		return lengthType;
	}

	/**
	 * Gets the source address
	 */
	public EthernetAddress getSource() {
		return source;
	}

}
