/*
 * $Id$
 */
package org.jnode.net;

/**
 * Headers of a any OSI layer must implement this interface.
 * 
 * @author epr
 * @see org.jnode.net.LinkLayerHeader
 * @see org.jnode.net.NetworkLayerHeader
 * @see org.jnode.net.TransportLayerHeader
 */
public interface LayerHeader {
	
	/**
	 * Gets the length of this header in bytes
	 */
	public int getLength();

	/**
	 * Prefix this header to the front of the given buffer
	 * @param skbuf
	 */
	public void prefixTo(SocketBuffer skbuf);
	
	/**
	 * Finalize the header in the given buffer.
	 * This method is called when all layers have set their header data
	 * and can be used e.g. to update checksum values.
	 * 
	 * @param skbuf The buffer
	 * @param offset The offset to the first byte (in the buffer) of this header (since low layer headers are already prefixed)
	 */
	public void finalizeHeader(SocketBuffer skbuf, int offset);
}
