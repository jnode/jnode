/*
 * $Id$
 */
package org.jnode.plugin;

/**
 * Interface of manager of all plugins in the system.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class PluginManager {

	/**
	 * Name used to bind the pluginmanager in the InitialNaming namespace.
	 * 
	 * @see org.jnode.naming.InitialNaming
	 */
	public static final Class NAME = PluginManager.class;//"system/PluginManager";

	/**
	 * Gets the plugin registry
	 * 
	 * @return The registry
	 */
	public abstract PluginRegistry getRegistry();
	
	/**
	 * Gets the plugin loader manager.
	 */
	public abstract PluginLoaderManager getLoaderManager();

	/**
	 * Start all plugins that can be started, but have not been started yet
	 * 
	 * @throws PluginException
	 */
	public abstract void startPlugins() throws PluginException;

	/**
	 * Starts a single plugin from its descriptor.
	 * 
	 * @param d
	 *            The plugins descriptor to start.
	 * @throws PluginException
	 *             if an error was encounterd and the plugin does not start.
	 */
	public abstract void startPlugin(PluginDescriptor d) throws PluginException;

	/**
	 * Stop all plugins that have been started
	 */
	public abstract void stopPlugins();

	/**
	 * Stops a plugin and all plugins that depend on it.
	 * 
	 * @param d
	 *            the plugins descriptor to stop.
	 * @throws PluginException
	 *             if an error was encountered and the plugin is unable to stop.
	 */
	public abstract void stopPlugin(PluginDescriptor d) throws PluginException;
	
	/**
	 * Start the given plugin.
	 * No dependent plugins will be started.
	 * 
	 * @param plugin
	 * @throws PluginException
	 */
	protected final void startSinglePlugin(Plugin plugin) throws PluginException {
		plugin.start();
	}
	
	/**
	 * Stop the given plugin.
	 * No dependent plugins will be stopped.
	 * 
	 * @param plugin
	 * @throws PluginException
	 */
	protected final void stopSinglePlugin(Plugin plugin) throws PluginException {
		plugin.stop();
	}
}
