/*
 * $Id$
 */
package org.jnode.plugin.model;

import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;

/**
 * @author epr
 */
public class EmptyPlugin extends Plugin {

	/**
	 * Initialize a new instance
	 * @param descriptor
	 */
	public EmptyPlugin(PluginDescriptor descriptor) {
		super(descriptor);
	}

	/**
	 * @see org.jnode.plugin.Plugin#startPlugin()
	 */
	protected void startPlugin() throws PluginException {
		// Do nothing
	}

	/**
	 * @see org.jnode.plugin.Plugin#stopPlugin()
	 */
	protected void stopPlugin() throws PluginException {
		// Do nothing
	}
}
