/*
 * $Id$
 */
package org.jnode.plugin.model;

import org.jnode.plugin.PluginDescriptor;

/**
 * @author epr
 */
public abstract class PluginModelObject extends AbstractModelObject {

	private final PluginDescriptorModel plugin;
	
	/**
	 * Create a new instance
	 * @param plugin
	 */
	public PluginModelObject(PluginDescriptorModel plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Gets the descriptor of the plugin in which this element was declared.
	 */
	public PluginDescriptor getDeclaringPluginDescriptor() {
		return plugin;
	}
}
