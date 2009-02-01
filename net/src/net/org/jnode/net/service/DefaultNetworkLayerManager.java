/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.net.service;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.net.NetDeviceAPI;
import org.jnode.driver.net.NetworkException;
import org.jnode.net.LayerAlreadyRegisteredException;
import org.jnode.net.NetworkLayer;
import org.jnode.net.NetworkLayerManager;
import org.jnode.net.NoSuchProtocolException;
import org.jnode.net.SocketBuffer;
import org.jnode.plugin.ConfigurationElement;
import org.jnode.plugin.Extension;
import org.jnode.plugin.ExtensionPoint;
import org.jnode.plugin.ExtensionPointListener;
import org.jnode.util.NumberUtils;
import org.jnode.util.Queue;
import org.jnode.util.QueueProcessor;

/**
 * @author epr
 */
public class DefaultNetworkLayerManager implements NetworkLayerManager,
        QueueProcessor<SocketBuffer>, ExtensionPointListener {

    /** My logger */
    private static final Logger log = Logger.getLogger(DefaultNetworkLayerManager.class);

    /** Registered packet types */
    private final HashMap<Integer, NetworkLayer> layers = new HashMap<Integer, NetworkLayer>();

    /** Queue of received packets */
    private final Queue<SocketBuffer> packetQueue = new Queue<SocketBuffer>();

    /** The networkLayers extension-point */
    private final ExtensionPoint networkLayersEP;

    /**
     * Initialize a new instance
     * 
     * @param networkLayersEP
     */
    public DefaultNetworkLayerManager(ExtensionPoint networkLayersEP) {
        this.networkLayersEP = networkLayersEP;
        networkLayersEP.addListener(this);
        refreshNetworkLayers();
    }

    /**
     * Register a packet type.
     * 
     * @param pt
     */
    protected synchronized void registerNetworkLayer(NetworkLayer pt)
        throws LayerAlreadyRegisteredException {
        layers.put(pt.getProtocolID(), pt);
    }

    /**
     * Unregister a packet type. If the packettype has not been registered, this
     * method returns without an error.
     * 
     * @param pt
     */
    public synchronized void unregisterNetworkLayer(NetworkLayer pt) {
        layers.remove(pt);
    }

    /**
     * Get all register packet types.
     * 
     * @return A collection of PacketType instances
     */
    public synchronized Collection<NetworkLayer> getNetworkLayers() {
        return new ArrayList<NetworkLayer>(layers.values());
    }

    /**
     * Gets the packet type for a given protocol ID
     * 
     * @param protocolID
     * @throws NoSuchProtocolException
     */
    public NetworkLayer getNetworkLayer(int protocolID) throws NoSuchProtocolException {
        final NetworkLayer pt = (NetworkLayer) layers.get(new Integer(protocolID));
        if (pt == null) {
            throw new NoSuchProtocolException("protocolID " + protocolID);
        }
        return pt;
    }

    /**
     * Process a packet that has been received. The receive method of all those
     * packettypes that have a matching type and allow the device(of the packet)
     * is called. The packet is cloned if more then 1 packettypes want to
     * receive the packet.
     * 
     * @param skbuf
     */
    public void receive(SocketBuffer skbuf) {
        packetQueue.add(skbuf);
    }

    /**
     * Process the received packet
     * 
     * @param skbuf
     */
    public synchronized void process(SocketBuffer skbuf) {
        try {
            final int protoID = skbuf.getProtocolID();
            final Device dev = skbuf.getDevice();
            if (dev == null) {
                throw new NetworkException("Device not set on SocketBuffer");
            }
            final NetDeviceAPI deviceAPI;
            try {
                deviceAPI = dev.getAPI(NetDeviceAPI.class);
            } catch (ApiNotFoundException ex) {
                throw new NetworkException("Device in SocketBuffer is not a network device");
            }

            // Find all the packettype that want to process the given packet
            try {
                final NetworkLayer pt = getNetworkLayer(protoID);
                if (pt.isAllowedForDevice(dev)) {
                    pt.receive(skbuf, deviceAPI);
                }
            } catch (NoSuchProtocolException ex) {
                log.debug("No network layer handler for protocol 0x" + NumberUtils.hex(protoID, 4));
            }
        } catch (SocketException ex) {
            log.error("Cannot process packet", ex);
        }
    }

    /**
     * Gets the packet queue
     */
    protected final Queue<SocketBuffer> getQueue() {
        return packetQueue;
    }

    /**
     * Reload the network layer list from the extension-point
     */
    protected void refreshNetworkLayers() {
        if (networkLayersEP != null) {
            layers.clear();
            final Extension[] extensions = networkLayersEP.getExtensions();
            for (int i = 0; i < extensions.length; i++) {
                final Extension ext = extensions[i];
                final ConfigurationElement[] elements = ext.getConfigurationElements();
                for (int j = 0; j < elements.length; j++) {
                    configureLayer(layers, elements[j]);
                }
            }
        }
        log.debug("Found " + layers.size() + " network layers");
    }

    private void configureLayer(Map<Integer, NetworkLayer> layers, ConfigurationElement element) {
        final String className = element.getAttribute("class");
        if (className != null) {
            try {
                final Class<?> cls =
                        Thread.currentThread().getContextClassLoader().loadClass(className);
                final NetworkLayer layer = (NetworkLayer) cls.newInstance();
                layers.put(layer.getProtocolID(), layer);
            } catch (ClassNotFoundException ex) {
                log.error("Cannot find networklayer class " + className);
            } catch (IllegalAccessException ex) {
                log.error("Cannot access networklayer class " + className);
            } catch (InstantiationException ex) {
                log.error("Cannot instantiate networklayer class " + className);
            } catch (ClassCastException ex) {
                log.error("Networklayer class " + className +
                        " does not implement the NetworkLayer interface");
            }
        }
    }

    /**
     * @see org.jnode.plugin.ExtensionPointListener#extensionAdded(org.jnode.plugin.ExtensionPoint,
     *      org.jnode.plugin.Extension)
     */
    public void extensionAdded(ExtensionPoint point, Extension extension) {
        refreshNetworkLayers();
    }

    /**
     * @see org.jnode.plugin.ExtensionPointListener#extensionRemoved(org.jnode.plugin.ExtensionPoint,
     *      org.jnode.plugin.Extension)
     */
    public void extensionRemoved(ExtensionPoint point, Extension extension) {
        refreshNetworkLayers();
    }
}
