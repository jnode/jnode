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
	
	public PluginPrerequisiteModel(PluginDescriptorModel plugin, XMLElement e) 
	throws PluginException {
		super(plugin);
		this.plugin = getAttribute(e, "plugin", true);
	}

	/**
	 * Gets the identifier of the plugin that is required
	 */
	public String getPluginId() {
		return plugin;
	}

	
	/**
	 * Resolve all references to (elements of) other plugin descriptors
	 * @throws PluginException
	 */
	protected void resolve() 
	throws PluginException {
		if (getDeclaringPluginDescriptor().getPluginRegistry().getPluginDescriptor(plugin) == null) {
			throw new PluginException("Unknown plugin " + plugin + " in import of " + getDeclaringPluginDescriptor().getId()); 
		}
	}

	/**
	 * Remove all references to (elements of) other plugin descriptors
	 * 
	 * @throws PluginException
	 */
	protected void unresolve() throws PluginException {
	    // Nothing to do
	}
}
