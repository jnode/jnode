/*
 * $Id$
 */
package org.jnode.driver;

import javax.naming.NameNotFoundException;

import org.jnode.naming.InitialNaming;
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;

/**
 * Plugin used to start the device discovery process.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class DeviceFinderPlugin extends Plugin {

	/**
	 * @param descriptor
	 */
	public DeviceFinderPlugin(PluginDescriptor descriptor) {
		super(descriptor);
	}

	/**
	 * Start this plugin
	 * @throws PluginException
	 */
	protected void startPlugin() throws PluginException {
		try {
			final DefaultDeviceManager devMan = (DefaultDeviceManager)InitialNaming.lookup(DeviceManager.NAME);
			devMan.findDevices();
		} catch (NameNotFoundException ex) {
			throw new PluginException("Cannot find DeviceManager");
		} catch (InterruptedException ex) {
		    throw new PluginException("findDevices was interrupted", ex);
        }
	}

	/**
	 * Stop this plugin
	 * @throws PluginException
	 */
	protected void stopPlugin() throws PluginException {
		// Do nothing
	}
}
