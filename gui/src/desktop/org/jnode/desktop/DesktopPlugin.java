/*
 * $Id$
 */
package org.jnode.desktop;

import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class DesktopPlugin extends Plugin {

	/**
	 * @param descriptor
	 */
	public DesktopPlugin(PluginDescriptor descriptor) {
		super(descriptor);
	}

	/**
	 * @see org.jnode.plugin.Plugin#startPlugin()
	 */
	protected void startPlugin() throws PluginException {
		System.setProperty("jnode.desktop", "org.jnode.desktop.Desktop");
	}

	/**
	 * @see org.jnode.plugin.Plugin#stopPlugin()
	 */
	protected void stopPlugin() throws PluginException {
		// Nothing to do
	}
}
