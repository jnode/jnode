/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
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
