/*
 * $Id$
 */
package org.jnode.plugin;


/**
 * Interface of manager of all plugins in the system.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface PluginManager {

	/**
	 * Name used to bind the pluginmanager in the InitialNaming namespace.
	 * 
	 * @see org.jnode.naming.InitialNaming
	 */
	public static final String NAME = "system/PluginManager";

	/**
	 * Gets the plugin registry
	 * 
	 * @return The registry
	 */
	public PluginRegistry getRegistry();

	/**
	 * Start all plugins that can be started, but have not been started yet
	 * 
	 * @throws PluginException
	 */
	public void startPlugins() throws PluginException;

	/**
	 * Starts a single plugin from its descriptor.
	 * 
	 * @param d
	 *            The plugins descriptor to start.
	 * @throws PluginException
	 *             if an error was encounterd and the plugin does not start.
	 */
	public void startPlugin(PluginDescriptor d) throws PluginException;

	/**
	 * Stop all plugins that have been started
	 */
	public void stopPlugins();

	/**
	 * Stops a single plugin.
	 * 
	 * @param d
	 *            the plugins descriptor to stop.
	 * @throws PluginException
	 *             if an error was encountered and the plugin is unable to stop.
	 */
	public void stopPlugin(PluginDescriptor d) throws PluginException;
}
