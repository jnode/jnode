/*
 * $Id$
 */
package org.jnode.plugin;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class PluginClassLoader extends ClassLoader {

	/**
	 * Gets the descriptor of the plugin in which this element was declared.
	 * @return The descriptor
	 */
	public abstract PluginDescriptor getDeclaringPluginDescriptor();    
}
