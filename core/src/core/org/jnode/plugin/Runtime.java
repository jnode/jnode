/*
 * $Id$
 */
package org.jnode.plugin;

/**
 * Descriptor of runtime libraries provided by the plugin.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface Runtime {
	
	/**
	 * Gets all declared libraries
	 * @return The libraries
	 */
	public Library[] getLibraries();

	/**
	 * Gets the descriptor of the plugin in which this element was declared.
	 * @return The descriptor
	 */
	public PluginDescriptor getDeclaringPluginDescriptor();
}
