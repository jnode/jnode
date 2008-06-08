/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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
     * 
     * @param skbuf
     * @param deviceAPI
     * @throws SocketException
     */
    public void receive(SocketBuffer skbuf, NetDeviceAPI deviceAPI) throws SocketException;

    /**
     * Gets the statistics of this protocol
     */
    public Statistics getStatistics();

    /**
     * Register a transportlayer as possible destination of packets received by
     * this networklayer
     * 
     * @param layer
     */
    public void registerTransportLayer(TransportLayer layer)
        throws LayerAlreadyRegisteredException, InvalidLayerException;

    /**
     * Unregister a transportlayer
     * 
     * @param layer
     */
    public void unregisterTransportLayer(TransportLayer layer);

    /**
     * Gets all registered transport-layers
     */
    public Collection<TransportLayer> getTransportLayers();

    /**
     * Gets a registered transportlayer by its protocol ID.
     * 
     * @param protocolID
     * @throws NoSuchProtocolException No protocol with the given ID was found.
     */
    public TransportLayer getTransportLayer(int protocolID) throws NoSuchProtocolException;

    /**
     * Gets the protocol addresses for a given name, or null if not found.
     * 
     * @param hostname
     * @return
     */
    public ProtocolAddress[] getHostByName(String hostname);
}
