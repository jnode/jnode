/*
 * $Id$
 */
package org.jnode.net;

/**
 * Headers of a NetworkLayer must implement this interface.
 * 
 * @author epr
 * @see org.jnode.net.NetworkLayer
 */
public interface NetworkLayerHeader extends LayerHeader {

	/**
	 * Gets the source address of the packet described in this header 
	 */
	public ProtocolAddress getSourceAddress();

	/**
	 * Gets the source address of the packet described in this header 
	 */
	public ProtocolAddress getDestinationAddress();
}
