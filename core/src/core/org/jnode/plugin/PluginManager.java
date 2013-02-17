/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.plugin;

import java.util.List;

import org.jnode.bootlog.BootLogInstance;

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
    public static final Class<PluginManager> NAME = PluginManager.class; //"system/PluginManager";

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
     * Start all system plugins and plugins with the auto-start flag on.
     * This method requires a JNodePermission("startSystemPlugins").
     *
     * @throws PluginException
     */
    public abstract void startSystemPlugins(List<? extends PluginDescriptor> descriptors) throws PluginException;

    /**
     * Stop all plugins that have been started
     * This method requires a JNodePermission("stopPlugins").
     */
    public abstract void stopPlugins();

    /**
     * Start the given plugin.
     * No dependent plugins will be started.
     *
     * @param plugin
     * @throws PluginException
     */
    protected final void startSinglePlugin(Plugin plugin) throws PluginException {
        try {
            plugin.start();
        } catch (PluginException ex) {
            BootLogInstance.get().error("Error starting " + plugin.getDescriptor().getId());
            throw ex;
        } catch (Throwable ex) {
            BootLogInstance.get().error("Error starting " + plugin.getDescriptor().getId());
            throw new PluginException(ex);
        }
    }

    /**
     * Stop the given plugin.
     * No dependent plugins will be stopped.
     *
     * @param plugin
     * @throws PluginException
     */
    protected final void stopSinglePlugin(Plugin plugin) throws PluginException {
        try {
            if (plugin.isActive()) {
                BootLogInstance.get().info("Stopping " + plugin.getDescriptor().getId());
                plugin.stop();
            }
        } catch (PluginException ex) {
            BootLogInstance.get().error("Error stopping " + plugin.getDescriptor().getId());
            throw ex;
        } catch (Throwable ex) {
            BootLogInstance.get().error("Error stopping " + plugin.getDescriptor().getId());
            throw new PluginException(ex);
        }
    }
}
