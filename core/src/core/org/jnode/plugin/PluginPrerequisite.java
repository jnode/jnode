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
	 * Gets the version of the plugin that is required.
	 * If not specified, this version is equal to the version of the
	 * declaring plugin.
	 * @return The version
	 */
	public String getPluginVersion();

	/**
	 * Gets the descriptor of the plugin in which this element was declared.
	 * @return The descriptor
	 */
	public PluginDescriptor getDeclaringPluginDescriptor();
}