/*
 * $Id$
 */
package org.jnode.plugin.model;

import nanoxml.XMLElement;

import org.jnode.plugin.PluginException;
import org.jnode.plugin.PluginPrerequisite;

/**
 * @author epr
 */
public class PluginPrerequisiteModel extends PluginModelObject implements PluginPrerequisite {
	
	private final String plugin;
	private final String version;
	
	public PluginPrerequisiteModel(PluginDescriptorModel plugin, XMLElement e) 
	throws PluginException {
		super(plugin);
		this.plugin = getAttribute(e, "plugin", true);
		final String version = getAttribute(e, "version", false);
		if (version != null) {
			this.version = version;
		} else {
			this.version = plugin.getVersion();
		}
	}

	/**
	 * Gets the identifier of the plugin that is required
	 */
	public String getPluginId() {
		return plugin;
	}

	
	/**
	 * Gets the version of the plugin that is required.
	 * If not specified, this version is equal to the version of the
	 * declaring plugin.
	 * @return The version
	 */
	public String getPluginVersion() {
		return version;
	}

	/**
	 * Resolve all references to (elements of) other plugin descriptors
	 * @throws PluginException
	 */
	protected void resolve(PluginRegistryModel registry) 
	throws PluginException {
		if (registry.getPluginDescriptor(plugin) == null) {
			throw new PluginException("Unknown plugin " + plugin + " in import of " + getDeclaringPluginDescriptor().getId()); 
		}
	}

	/**
	 * Remove all references to (elements of) other plugin descriptors
	 * 
	 * @throws PluginException
	 */
	protected void unresolve(PluginRegistryModel registry) throws PluginException {
	    // Nothing to do
	}
}
