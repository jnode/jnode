/*
 * $Id$
 */
package org.jnode.plugin;

/**
 * Resource library descriptor.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface Library {

	/**
	 * Gets the name of the jar file or directory
	 * @return The name
	 */
	public String getName();
	
	/**
	 * Is this a code library?
	 * @return boolean
	 */
	public boolean isCode();

	/**
	 * Is this a resource only library?
	 * @return boolean
	 */
	public boolean isResource();

	/**
	 * Gets all declared export names
	 * @return All declared export names
	 */
	public String[] getExports();

	/**
	 * Gets the descriptor of the plugin in which this element was declared.
	 * @return The descriptor
	 */
	public PluginDescriptor getDeclaringPluginDescriptor();
}
