/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.Manifest;

import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.plugin.PluginLoader;
import org.jnode.plugin.model.PluginRegistryModel;
import org.jnode.system.BootLog;
import org.jnode.system.MemoryResource;
import org.jnode.util.JarBuffer;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class InitJarProcessor {

    private final JarBuffer jbuf;

    private final Manifest mf;

    /**
     * Initialize this instance.
     * 
     * @param initJarRes
     */
    public InitJarProcessor(MemoryResource initJarRes) {
        JarBuffer jbuf = null;
        Manifest mf = null;
        if (initJarRes != null) {
            try {
                jbuf = new JarBuffer(initJarRes.asByteBuffer());
                mf = jbuf.getManifest();
            } catch (IOException ex) {
                BootLog.error("Cannot instantiate initjar", ex);
            }
        }
        this.jbuf = jbuf;
        this.mf = mf;
    }

    /**
     * Load all plugins found in the initjar.
     * 
     * @param piRegistry
     */
    public List<PluginDescriptor> loadPlugins(PluginRegistryModel piRegistry) {
        if (jbuf == null) { return null; }

        final InitJarPluginLoader loader = new InitJarPluginLoader();
        final ArrayList<PluginDescriptor> descriptors = new ArrayList<PluginDescriptor>();
        for (Map.Entry<String, ByteBuffer> entry : jbuf.entries().entrySet()) {
            final String name = entry.getKey();
            if (name.endsWith(".jar")) {
                try {
                    // Load it
                    loader.setBuffer(entry.getValue());
                    final PluginDescriptor descr = piRegistry.loadPlugin(
                            loader, "", "", false);
                    descriptors.add(descr);
                } catch (PluginException ex) {
                    BootLog.error("Cannot load " + name, ex);
                }
            }
        }
        return descriptors;
    }

    static class InitJarPluginLoader extends PluginLoader {

        private ByteBuffer buf;

        public InitJarPluginLoader() {
        }

        /**
         * @see org.jnode.plugin.PluginLoader#getPluginStream(java.lang.String,
         *      java.lang.String)
         */
        public ByteBuffer getPluginBuffer(String pluginId, String pluginVersion) {
            return buf;
        }

        /**
         * @param is
         *            The is to set.
         */
        final void setBuffer(ByteBuffer buf) {
            this.buf = buf;
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
