/*
 * $Id$
 */
package org.jnode.plugin;

/**
 * Plugin utility methods.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class PluginUtils {
    
    /**
     * Gets the descriptor of the plugin that contains the given class.
     * @param clazz
     * @return The descriptor, or null if this class is not contained in a plugin or part of a system plugin.
     */
    public static PluginDescriptor getPluginDescriptor(Class clazz) {
        final ClassLoader cl = clazz.getClassLoader();
        if (cl instanceof PluginClassLoader) {
            return ((PluginClassLoader)cl).getDeclaringPluginDescriptor();
        } else {
            return null;
        }
    }
}
