/*
 * $Id$
 */
package org.jnode.net.service;

import java.net.VMNetAPI;
import java.net.VMNetUtils;

import javax.naming.NamingException;

import org.jnode.naming.InitialNaming;
import org.jnode.net.NetworkLayerManager;
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
	private QueueProcessorThread packetProcessorThread;
	/** The NetAPI implementation */ 
	private final VMNetAPI api;

	/**
	 * Create a new instance
	 */
	public NetPlugin(PluginDescriptor descriptor) {
		super(descriptor);
		ptm = new DefaultNetworkLayerManager(descriptor.getExtensionPoint("networkLayers"));
		api = new NetAPIImpl(ptm);
		packetProcessorThread = new QueueProcessorThread("net-packet-processor", ptm.getQueue(), ptm);
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
