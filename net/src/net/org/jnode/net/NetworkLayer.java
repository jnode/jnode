/*
 * $Id$
 */
package org.jnode.net;

import java.net.SocketException;
import java.util.Collection;

import org.jnode.driver.Device;
import org.jnode.driver.net.NetDeviceAPI;
import org.jnode.util.Statistics;

/**
 * OSI network layers must implement this interface.
 * 
 * @author epr
 */
public interface NetworkLayer {
	
	/**
	 * Gets the name of this type
	 */
	public String getName();
	
	/**
	 * Gets the protocol ID this layer handles
	 */
	public int getProtocolID();
	
	/**
	 * Can this packet type process packets received from the given device?
	 */
	public boolean isAllowedForDevice(Device dev);
	
	/**
	 * Process a packet that has been received and matches getType()
	 * @param skbuf
	 * @param deviceAPI
	 * @throws SocketException
	 */
	public void receive(SocketBuffer skbuf, NetDeviceAPI deviceAPI)
	throws SocketException;
	
	/**
	 * Gets the statistics of this protocol
	 */
	public Statistics getStatistics();	

	/**
	 * Register a transportlayer as possible destination of packets received by this networklayer
	 * @param layer
	 */
	public void registerTransportLayer(TransportLayer layer)
	throws LayerAlreadyRegisteredException, InvalidLayerException;
	
	/**
	 * Unregister a transportlayer
	 * @param layer
	 */
	public void unregisterTransportLayer(TransportLayer layer);
	
	/**
	 * Gets all registered transport-layers
	 */
	public Collection getTransportLayers();
	
	/**
	 * Gets a registered transportlayer by its protocol ID.
	 * @param protocolID
	 * @throws NoSuchProtocolException No protocol with the given ID was found.
	 */
	public TransportLayer getTransportLayer(int protocolID)
	throws NoSuchProtocolException;

	/**
	 * Gets the protocol addresses for a given name, or null if not found.
	 * @param hostname
	 * @return
	 */
    public ProtocolAddress[] getHostByName(String hostname);
}
