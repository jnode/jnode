/*
 * $Id$
 */
package org.jnode.plugin;

import java.util.Iterator;

/**
 * Registry of all plugins in the system.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface PluginRegistry {

	/**
	 * Gets the descriptor of the plugin with the given id.
	 * @param pluginId
	 * @return The plugin descriptor found, or null if not found
	 */
	public PluginDescriptor getPluginDescriptor(String pluginId);
	
	/**
	 * Gets the extension point with the given id.
	 * @param id
	 * @return The extension point found, or null if not found
	 */
	public ExtensionPoint getExtensionPoint(String id);
	
	/**
	 * Returns an iterator to iterate over all PluginDescriptor's.
	 * @return Iterator&lt;PluginDescriptor&gt;
	 */
	public Iterator getDescriptorIterator();
}
