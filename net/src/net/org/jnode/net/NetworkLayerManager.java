/*
 * $Id$
 */
package org.jnode.net;

import java.util.Collection;

import org.jnode.driver.net.NetworkException;

/**
 * This interface must be implemented by the network service of the JNode kernel.
 * It contains methods to register/unregister and obtain NetworkLayers, and
 * it is used by Network drivers to deliver receive packets.
 * <p/>
 * The implementation of this interface must be obtained by invoking a lookup
 * of {@link #NAME} on {@link org.jnode.naming.InitialNaming}.
 * 
 * @author epr
 * @see org.jnode.driver.net.NetDeviceAPI
 */
public interface NetworkLayerManager {

	/** Name used to bind the ptm in the InitialNaming namespace */	
	public static final Class NAME = NetworkLayerManager.class;//"system/net/networklayermanager";

	/**
	 * Get all register packet types.
	 * @return A collection of PacketType instances
	 */
	public Collection getNetworkLayers();
	
	/**
	 * Gets the packet type for a given protocol ID
	 * @param protocolID
	 * @throws NoSuchProtocolException
	 */
	public NetworkLayer getNetworkLayer(int protocolID)
	throws NoSuchProtocolException;
	
	/**
	 * Process a packet that has been received. 
	 * The receive method of all those packettypes that have a matching type
	 * and allow the device(of the packet) is called.
	 * The packet is cloned if more then 1 packettypes want to receive the
	 * packet.
	 * 
	 * @param skbuf
	 */
	public void receive(SocketBuffer skbuf)
	throws NetworkException;
}
