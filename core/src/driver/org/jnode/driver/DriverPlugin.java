/*
 * $Id$
 */
package org.jnode.driver;

import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;

/**
 * Plugin start instantiates and initialize the default device manager.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class DriverPlugin extends Plugin {

	private DefaultDeviceManager devMgr;
	
	/**
	 * @param descriptor
	 */
	public DriverPlugin(PluginDescriptor descriptor) {
		super(descriptor);
	}

	/**
	 * Start this plugin
	 * @throws PluginException
	 */
	protected void startPlugin() throws PluginException {
		devMgr = new DefaultDeviceManager(getDescriptor().getExtensionPoint("finders"), getDescriptor().getExtensionPoint("mappers"));
		devMgr.start();
	}

	/**
	 * Stop this plugin
	 * @throws PluginException
	 */
	protected void stopPlugin() throws PluginException {
		devMgr.stop();
		devMgr = null;
	}
}
