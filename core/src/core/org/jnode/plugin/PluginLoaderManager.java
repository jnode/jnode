/*
 * $Id$
 */
package org.jnode.plugin;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class PluginLoaderManager extends PluginLoader {
    
    /**
     * Add a plugin loader.
     * @param loader
     */
    public abstract void addPluginLoader(PluginLoader loader);

    /**
     * Remove a plugin loader.
     * @param loader
     */
    public abstract void removePluginLoader(PluginLoader loader);
}
