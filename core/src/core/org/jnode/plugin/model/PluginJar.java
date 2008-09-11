/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.plugin.model;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

import org.jnode.nanoxml.XMLElement;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.util.BootableHashMap;
import org.jnode.util.ByteBufferInputStream;
import org.jnode.util.FileUtils;
import org.jnode.util.JarBuffer;
import org.jnode.vm.BootableObject;
import org.jnode.vm.ResourceLoader;
import org.jnode.vm.Vm;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class PluginJar implements BootableObject, ResourceLoader {

    /**
     * The descriptor of this file
     */
    private final PluginDescriptorModel descriptor;

    /**
     * The resources in the jar file
     */
    private final Map<String, ByteBuffer> resources;

    private ByteBuffer buffer;
    //This field holds the system plugin data added to the boot image
    //Results in a 7MB increase of memory usage but the system jars
    //become accessible to the complier
    private final byte[] initBuffer;

    /**
     * Initialize this instance
     *
     * @param registry
     * @param pluginUrl
     */
    public PluginJar(PluginRegistryModel registry, URL pluginUrl)
        throws PluginException, IOException {
        this(registry, FileUtils.loadToBuffer(pluginUrl.openStream(), true), null);
    }

    /**
     * Initialize this instance
     *
     * @param registry
     * @param pluginIs
     */
    public PluginJar(PluginRegistryModel registry, ByteBuffer pluginIs,
                     URL pluginUrl) throws PluginException {

        try {
            //get a reference to the plugin jar data
            if (Vm.isWritingImage()) {
                //buildtime
                initBuffer = pluginIs.array();
            } else {
                //runtime
                buffer = pluginIs;
                initBuffer = new byte[0];
            }
            // Load the plugin into memory
            resources = loadResources(pluginIs);
        } catch (IOException ex) {
            throw new PluginException("Error loading jarfile", ex);
        }

        final XMLElement root;
        try {
            // Not find the plugin.xml
            final ByteBuffer buf = getResourceAsBuffer("plugin.xml");
            if (buf == null) {
                throw new PluginException(
                    "plugin.xml not found in jar file");
            }

            // Now parse plugin.xml
            root = new XMLElement(new Hashtable(), true, false);
            final Reader r = new InputStreamReader(new ByteBufferInputStream(buf));
            try {
                root.parseFromReader(r);
            } finally {
                r.close();
            }
        } catch (IOException ex) {
            throw new PluginException("Plugin " + pluginUrl, ex);
        }
        this.descriptor = Factory.parseDescriptor(this, root);
    }

    /**
     * @see org.jnode.vm.ResourceLoader#getResourceAsBuffer(java.lang.String)
     */
    public ByteBuffer getResourceAsBuffer(String resourceName) {
        final ByteBuffer data = resources.get(resourceName);
        if (data == null) {
            return null;
        } else {
            return (ByteBuffer) data.asReadOnlyBuffer().rewind();
        }
    }

    /**
     * Does this jar-file contain the resource with the given name.
     *
     * @param resourceName
     * @return boolean
     */
    public final boolean containsResource(String resourceName) {
        return resources.containsKey(resourceName);
    }

    /**
     * Return the names of all resources in this plugin jar.
     *
     * @return
     */
    public Collection<String> resourceNames() {
        return Collections.unmodifiableCollection(resources.keySet());
    }

    /**
     * Remove all resources.
     */
    public void clearResources() {
        resources.clear();
    }

    /**
     * Does this jar-file contain the resource with the given name.
     *
     * @param resourceName
     * @return boolean
     */
    public final URL getResource(String resourceName) {
        if (resourceName.startsWith("/")) {
            resourceName = resourceName.substring(1);
        }
        try {
            if (resourceName.length() > 0) {
                if (!resources.containsKey(resourceName)) {
                    return null;
                }
            }
            final String id = descriptor.getId();
            return new URL("plugin:" + id + "!/" + resourceName);
        } catch (IOException ex) {
            System.out.println("ioex: " + ex.getMessage());
            return null;
        }
    }

    /**
     * Gets the descriptor of this plugin-jar file.
     *
     * @return Returns the descriptor.
     */
    final PluginDescriptorModel getDescriptorModel() {
        return this.descriptor;
    }

    /**
     * Gets the descriptor of this plugin-jar file.
     *
     * @return Returns the descriptor.
     */
    public final PluginDescriptor getDescriptor() {
        return this.descriptor;
    }

    private Map<String, ByteBuffer> loadResources(ByteBuffer buffer) throws IOException {
        final BootableHashMap<String, ByteBuffer> map = new BootableHashMap<String, ByteBuffer>();
        final JarBuffer jbuf = new JarBuffer(buffer);
        for (Map.Entry<String, ByteBuffer> entry : jbuf.entries().entrySet()) {
            if (entry.getValue().limit() > 0) {
                map.put(entry.getKey(), entry.getValue());
            }
        }
        return map;
    }

    public ByteBuffer getBuffer() {
        if (buffer == null) {
            //such plugins were added to the bootimage during the build
            buffer = ByteBuffer.wrap(initBuffer);
        }
        return buffer;
    }
}
