/*
 * $Id$
 */
package org.jnode.plugin;

/**
 * Descriptor of plugin prerequisites.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface PluginPrerequisite {
	
	/**
	 * Gets the identifier of the plugin that is required
	 * @return The id
	 */
	public String getPluginId();

	/**
	 * Gets the descriptor of the plugin in which this element was declared.
	 * @return The descriptor
	 */
	public PluginDescriptor getDeclaringPluginDescriptor();
}