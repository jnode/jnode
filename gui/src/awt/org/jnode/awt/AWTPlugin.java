/*
 * $Id$
 */
package org.jnode.awt;

import java.awt.GraphicsEnvironment;
import java.awt.image.VMImageUtils;

import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class AWTPlugin extends Plugin {

	/**
	 * @param descriptor
	 */
	public AWTPlugin(PluginDescriptor descriptor) {
		super(descriptor);
	}

	/**
	 * Start this plugin
	 * @throws PluginException
	 */
	protected void startPlugin() throws PluginException {
		System.getProperties().setProperty("awt.toolkit", "org.jnode.awt.peer.JNodeToolkit");
		VMImageUtils.setAPI(new VMImageAPIImpl(), this);
		GraphicsEnvironment.setLocalGraphicsEnvironment(new JNodeGraphicsEnvironment());
	}

	/**
	 * Stop this plugin
	 * @throws PluginException
	 */
	protected void stopPlugin() throws PluginException {
		// GraphicsEnvironment.setLocalGraphicsEnvironment(null);
		VMImageUtils.resetAPI(this);
	}
}
