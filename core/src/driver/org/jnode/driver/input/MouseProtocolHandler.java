/*
 * $Id$
 */
package org.jnode.driver.input;

/**
 * @author qades
 */
public interface MouseProtocolHandler {

	/**
	 * Gets the name of the protocol of this handler.
	 * @return String
	 */
	public String getName();
	
	/**
	 * Does this protocol handler support a given mouse id.
	 * @param id
	 * @return True if this handler supports the given id, false otherwise.
	 */
	public boolean supportsId(int id);
	
	/**
	 * Gets the size in bytes of a single packet in this protocol.
	 * @return
	 */
	public int getPacketSize();
	
	/**
	 * Create an event based of the given data packet.
	 * @param data
	 * @return
	 */
	public PointerEvent buildEvent(byte[] data);

}