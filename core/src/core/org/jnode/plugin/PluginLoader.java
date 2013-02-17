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

import java.nio.ByteBuffer;
import org.jnode.permission.JNodePermission;


/**
 * Loader of plugin files.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class PluginLoader {

    private static final JNodePermission CREATE_PERM = new JNodePermission("createPluginLoader");

    /**
     * Initialize this instance.
     * A JNodePermission("createPluginLoader") is required to execute
     * this constructor.
     */
    public PluginLoader() {
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(CREATE_PERM);
        }
    }

    /**
     * Open an inputstream for a plugin with a given id and version.
     *
     * @param pluginId
     * @param pluginVersion
     * @return The input stream, of null if not found.
     */
    public abstract ByteBuffer getPluginBuffer(String pluginId, String pluginVersion);

    /**
     * Gets the filename of a plugin with a given id and version.
     *
     * @param pluginId
     * @param pluginVersion
     * @return the filename of the plugin
     */
    public static String getPluginFileName(String pluginId, String pluginVersion) {
        return pluginId + '_' + pluginVersion + ".jar";
    }
}
