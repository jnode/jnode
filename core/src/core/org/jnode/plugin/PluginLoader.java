/*
 * $Id$
 */
package org.jnode.plugin;

import java.io.InputStream;

import org.jnode.security.JNodePermission;


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
     * @param pluginId
     * @param pluginVersion
     * @return The input stream, of null if not found.
     */
    public abstract InputStream getPluginStream(String pluginId, String pluginVersion);

    /**
     * Gets the filename of a plugin with a given id and version.
     * @param pluginId
     * @param pluginVersion
     * @return
     */
    public static String getPluginFileName(String pluginId, String pluginVersion) {
        return pluginId + "_" + pluginVersion + ".jar";
    }    
}
