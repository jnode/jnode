/*
 * $Id$
 */
package org.jnode.net;

/**
 * Headers of a LinkLayer must implement this interface.
 * 
 * @author epr
 * @see org.jnode.net.LinkLayer
 */
public interface LinkLayerHeader extends LayerHeader {
	
	/**
	 * Gets the source address of the packet described in this header 
	 */
	public HardwareAddress getSourceAddress();

	/**
	 * Gets the source address of the packet described in this header 
	 */
	public HardwareAddress getDestinationAddress();

}
