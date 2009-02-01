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

import java.net.VMNetAPI;
import java.net.VMNetUtils;

import javax.naming.NamingException;

import org.jnode.naming.InitialNaming;
import org.jnode.net.NetworkLayerManager;
import org.jnode.net.SocketBuffer;
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.util.QueueProcessorThread;

/**
 * Default basic networking services service.
 * 
 * @author epr
 */
public class NetPlugin extends Plugin {

    /** The packet type manager */
    private DefaultNetworkLayerManager ptm;
    
    /** The processor for the packet queue */
    private QueueProcessorThread<SocketBuffer> packetProcessorThread;
    
    /** The NetAPI implementation */
    private final VMNetAPI api;

    /**
     * Create a new instance
     */
    public NetPlugin(PluginDescriptor descriptor) {
        super(descriptor);
        ptm = new DefaultNetworkLayerManager(descriptor.getExtensionPoint("networkLayers"));
        api = new NetAPIImpl(ptm);
        packetProcessorThread =
                new QueueProcessorThread<SocketBuffer>("net-packet-processor", ptm.getQueue(), ptm);
    }

    /**
     * Start this plugin
     */
    protected void startPlugin() throws PluginException {
        try {
            InitialNaming.bind(NetworkLayerManager.NAME, ptm);
            packetProcessorThread.start();
            VMNetUtils.setAPI(api, this);
        } catch (NamingException ex) {
            throw new PluginException(ex);
        }
    }

    /**
     * Stop this plugin
     */
    protected void stopPlugin() throws PluginException {
        VMNetUtils.resetAPI(this);
        InitialNaming.unbind(NetworkLayerManager.NAME);
        packetProcessorThread.stopProcessor();
    }

}
