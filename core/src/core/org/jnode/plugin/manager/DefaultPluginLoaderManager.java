/*
 * $Id$
 */
package org.jnode.plugin.manager;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jnode.plugin.PluginLoader;
import org.jnode.plugin.PluginLoaderManager;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class DefaultPluginLoaderManager extends PluginLoaderManager {

    private final List loaders = new ArrayList();
    
    /**
     * @see org.jnode.plugin.PluginLoaderManager#addPluginLoader(org.jnode.plugin.PluginLoader)
     */
    public synchronized void addPluginLoader(PluginLoader loader) {
        if (!loaders.contains(loader)) {
            loaders.add(loader);
        }
    }
    
    /**
     * @see org.jnode.plugin.PluginLoaderManager#removePluginLoader(org.jnode.plugin.PluginLoader)
     */
    public synchronized void removePluginLoader(PluginLoader loader) {
        loaders.remove(loader);
    }
    
    /**
     * @see org.jnode.plugin.PluginLoader#getPluginStream(java.lang.String, java.lang.String)
     */
    public InputStream getPluginStream(String pluginId, String pluginVersion) {
        final List loaders;
        synchronized(this) {
            loaders = new ArrayList(this.loaders);
        }
        for (Iterator i = loaders.iterator(); i.hasNext(); ) {
            final PluginLoader loader = (PluginLoader)i.next();
            final InputStream is = loader.getPluginStream(pluginId, pluginVersion);
            if (is != null) {
                return is;
            }
        }
        return null;
    }
}
