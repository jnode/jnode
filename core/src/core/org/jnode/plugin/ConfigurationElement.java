/*
 * $Id$
 */
package org.jnode.plugin;

/**
 * Extension specific configuration element.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface ConfigurationElement {

	/**
	 * Gets the name of this element
	 * @return The name
	 */
	public String getName();

	/**
	 * Gets all child elements
	 * @return The child elements
	 */
	public ConfigurationElement[] getElements();

	/**
	 * Gets the value of an attribute with a given name
	 * @param name
	 * @return The attribute value, or null if not found
	 */
	public String getAttribute(String name);

	/**
	 * Gets the descriptor of the plugin in which this element was declared.
	 * @return The descriptor
	 */
	public PluginDescriptor getDeclaringPluginDescriptor();
}
