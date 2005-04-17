/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
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
	public Iterator<PluginDescriptor> getDescriptorIterator();

	/**
	 * Load a plugin from a given loader.
	 * 
	 * @param loader
	 * @param pluginId
	 * @param pluginVersion 
	 * @return The descriptor of the loaded plugin.
	 * @throws PluginException
	 */
	public PluginDescriptor loadPlugin(PluginLoader loader, String pluginId, String pluginVersion) throws PluginException;

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
