/*
 * $Id$
 */
package org.jnode.plugin;

import java.io.InputStream;
import java.net.URL;
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
	 * Load a plugin from a given URL.
	 * This will not activate the plugin.
	 * 
	 * @param pluginUrl
	 * @return The descriptor of the loaded plugin.
	 * @throws PluginException
	 */
	public PluginDescriptor loadPlugin(URL pluginUrl) throws PluginException;

	/**
	 * Load a plugin from a given InputStream.
	 * This will not activate the plugin.
	 * 
	 * @param is
	 * @return The descriptor of the loaded plugin.
	 * @throws PluginException
	 */
	public PluginDescriptor loadPlugin(InputStream is) throws PluginException;

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
