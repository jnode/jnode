/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
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


/**
 * Descriptor of a Plugin.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface PluginDescriptor {
	
	/**
	 * Gets the unique identifier of this plugin
	 * @return the unique id
	 */
	public abstract String getId();
	
	/**
	 * Gets the human readable name of this plugin
	 * @return The name
	 */
	public abstract String getName();
	
	/**
	 * Gets the name of the provider of this plugin
	 * @return The name of the provider
	 */
	public abstract String getProviderName();
	
    /**
     * Gets the url of the provider of this plugin
     * @return The url of the provider (can be null for no url)
     */
    public abstract String getProviderUrl();
    
    /**
     * Gets the name of the license that this plugin is using
     * @return The name of the license
     */
    public abstract String getLicenseName();
    
    /**
     * Gets the url of the license that this plugin is using
     * @return The url of the license (can be null for no url)
     */
    public abstract String getLicenseUrl();
    
	/**
	 * Gets the version of this plugin
	 * @return The version
	 */
	public abstract String getVersion();
	
	/**
	 * Gets the required imports
	 * @return The required imports
	 */
	public abstract PluginPrerequisite[] getPrerequisites();
	
	/**
	 * Does the plugin described by this descriptor directly depends on the 
	 * given plugin id.
	 * 
	 * @param id
	 * @return True if id is in the list of required plugins of this descriptor, false otherwise.
	 */
	public abstract boolean depends(String id);

	/**
	 * Gets all extension-points provided by this plugin
	 * @return The provided extension-points
	 */
	public abstract ExtensionPoint[] getExtensionPoints();
	
	/**
	 * Returns the extension point with the given simple identifier
	 * declared in this plug-in, or null if there is no such extension 
	 * point. 
	 * @param extensionPointId the simple identifier of the extension point (e.g. "wizard"). 
	 * @return the extension point, or null
	 */
	public abstract ExtensionPoint getExtensionPoint(String extensionPointId);
	
	/**
	 * Gets all extensions provided by this plugin
	 * @return The provided extensions
	 */
	public abstract Extension[] getExtensions();
	
	/**
	 * Gets the runtime information of this descriptor.
	 * @return The runtime, or null if no runtime information is provided.
	 */
	public Runtime getRuntime();

	/**
	 * Gets the plugin that is described by this descriptor.
	 * If no plugin class is given in the descriptor, an empty
	 * plugin is returned.
	 * This method will always returns the same plugin instance for a given
	 * descriptor.
	 * @return The plugin
	 * @throws PluginException
	 */
	public Plugin getPlugin()
	throws PluginException;
	
	/**
	 * Is this a descriptor of a fragment.
	 * 
	 * @return boolean True for a fragment, false for a plugin
	 */
	public boolean isFragment();
	
    /**
     * Is this a descriptor of a system plugin.
     * System plugins are not reloadable.
     * @return boolean
     */
    public boolean isSystemPlugin();
    
	/**
	 * Does this plugin have a custom plugin class specified?
	 * @return
	 */
	public boolean hasCustomPluginClass();
	
    /**
     * Gets the name of the custom plugin class of this plugin.
     * @return Null if no custom plugin class
     */
    public String getCustomPluginClassName();
    
	/**
	 * Has this plugin the auto-start flag set.
	 * If true, the plugin will be started automatically at boot/load time.
	 * @return
	 */
	public boolean isAutoStart();
	
	/**
	 * Gets the classloader of this plugin descriptor.
	 * @return ClassLoader
	 */
	public ClassLoader getPluginClassLoader();
	
	/**
	 * Add a listener to this descriptor.
	 * @param listener
	 */
	public void addListener(PluginDescriptorListener listener);

	/**
	 * Remove a listener from this descriptor.
	 * @param listener
	 */
	public void removeListener(PluginDescriptorListener listener);
}
