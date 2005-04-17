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
 
package org.jnode.boot;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.plugin.PluginLoader;
import org.jnode.plugin.model.PluginRegistryModel;
import org.jnode.system.BootLog;
import org.jnode.system.MemoryResource;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class InitJarProcessor {

    private final JarInputStream jis;

    private final Manifest mf;

    /**
     * Initialize this instance.
     * 
     * @param initJarRes
     */
    public InitJarProcessor(MemoryResource initJarRes) {
        JarInputStream jis = null;
        Manifest mf = null;
        if (initJarRes != null) {
            try {
                jis = new JarInputStream(new MemoryResourceInputStream(
                        initJarRes));
                mf = jis.getManifest();
            } catch (IOException ex) {
                BootLog.error("Cannot instantiate initjar", ex);
            }
        }
        this.jis = jis;
        this.mf = mf;
    }

    /**
     * Load all plugins found in the initjar.
     * 
     * @param piRegistry
     */
    public List<PluginDescriptor> loadPlugins(PluginRegistryModel piRegistry) {
        if (jis == null) { return null; }

        final InitJarPluginLoader loader = new InitJarPluginLoader();
        final ArrayList<PluginDescriptor> descriptors = new ArrayList<PluginDescriptor>();
        JarEntry entry;
        try {
            while ((entry = jis.getNextJarEntry()) != null) {
                if (entry.getName().endsWith(".jar")) {
                    try {
                        // Load it
                        loader.setIs(new NoCloseInputStream(jis));
                        final PluginDescriptor descr = piRegistry.loadPlugin(
                                loader, "", "", false);
                        descriptors.add(descr);
                    } catch (PluginException ex) {
                        BootLog.error("Cannot load " + entry.getName(), ex);
                    }
                }
                jis.closeEntry();
            }
        } catch (IOException ex) {
            BootLog.error("Cannot load initjars", ex);
        }
        return descriptors;
    }

    static class InitJarPluginLoader extends PluginLoader {

        private InputStream is;

        public InitJarPluginLoader() {
        }

        /**
         * @see org.jnode.plugin.PluginLoader#getPluginStream(java.lang.String,
         *      java.lang.String)
         */
        public InputStream getPluginStream(String pluginId, String pluginVersion) {
            return is;
        }

        /**
         * @param is
         *            The is to set.
         */
        final void setIs(InputStream is) {
            this.is = is;
        }
    }

    /**
     * Gets the name of the Main-Class from the initjar manifest.
     * 
     * @return The classname of the main class, or null.
     */
    public String getMainClassName() {
        if (mf != null) {
            return mf.getMainAttributes().getValue("Main-Class");
        } else {
            return null;
        }
    }
}
