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

	/**
	 * Load a plugin from a given loader.
	 * This will not activate the plugin.
	 * 
	 * @param loader
	 * @param pluginId
	 * @param pluginVersion 
	 * @return The descriptor of the loaded plugin.
	 * @throws PluginException
	 */
	public PluginDescriptor loadPlugin(PluginLoader loader, String pluginId, String pluginVersion, boolean resolve) throws PluginException;

	/**
	 * Remove the plugin with the given id from this registry.
	 * 
	 * @param pluginId
	 * @throws PluginException
	 */
	public void unloadPlugin(String pluginId) throws PluginException;
	
	/**
	 * Gets the classloader that loads classes from all loaded plugins.
	 * @return ClassLoader
	 */
	public ClassLoader getPluginsClassLoader();
}
