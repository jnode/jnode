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
 
package org.jnode.plugin.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Hashtable;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import nanoxml.XMLElement;

import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.util.BootableHashMap;
import org.jnode.util.FileUtils;
import org.jnode.vm.BootableObject;
import org.jnode.vm.ResourceLoader;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class PluginJar implements BootableObject, ResourceLoader {

    /** The descriptor of this file */
    private final PluginDescriptorModel descriptor;

    /** The resources in the jar file */
    private final Map<String, byte[]> resources;

    /**
     * Initialize this instance
     * 
     * @param registry
     * @param pluginUrl
     */
    public PluginJar(PluginRegistryModel registry, URL pluginUrl)
            throws PluginException, IOException {
        this(registry, pluginUrl.openStream(), null);
    }

    /**
     * Initialize this instance
     * 
     * @param registry
     * @param pluginIs
     */
    public PluginJar(PluginRegistryModel registry, InputStream pluginIs,
            URL pluginUrl) throws PluginException {

        try {
            // Load the plugin into memory
            resources = loadResources(pluginIs);
        } catch (IOException ex) {
            throw new PluginException("Error loading jarfile", ex);
        }

        final XMLElement root;
        try {
            // Not find the plugin.xml
            final InputStream pluginXmlRes = getResourceAsStream("plugin.xml");
            if (pluginXmlRes == null) { throw new PluginException(
                    "plugin.xml not found in jar file"); }

            // Now parse plugin.xml
            root = new XMLElement(new Hashtable(), true, false);
            final Reader r = new InputStreamReader(pluginXmlRes);
            try {
                root.parseFromReader(r);
            } finally {
                r.close();
            }
        } catch (IOException ex) {
            throw new PluginException("Plugin " + pluginUrl, ex);
        }
        final String rootTag = root.getName();
        if (rootTag.equals("plugin")) {
            this.descriptor = new PluginDescriptorModel(this, root);           
        } else if (rootTag.equals("fragment")) {
            this.descriptor = new FragmentDescriptorModel(this, root);            
        } else {
            throw new PluginException("plugin or fragment element expected");
        }
        if (descriptor.isSystemPlugin()) {
            resources.clear();
        }
    }

    /**
     * Does this jar-file contain the resource with the given name.
     * 
     * @param resourceName
     * @return boolean
     */
    public final InputStream getResourceAsStream(String resourceName) {
        final byte[] data = (byte[]) resources.get(resourceName);
        if (data == null) {
            return null;
        } else {
            return new ByteArrayInputStream(data);
        }
    }

    /**
     * @see org.jnode.vm.ResourceLoader#getResourceAsBuffer(java.lang.String)
     */
    public ByteBuffer getResourceAsBuffer(String resourceName) {
        final byte[] data = (byte[]) resources.get(resourceName);
        if (data == null) {
            return null;
        } else {
            return ByteBuffer.wrap(data);
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

    private Map<String, byte[]> loadResources(InputStream is) throws IOException {
        final BootableHashMap<String, byte[]> map = new BootableHashMap<String, byte[]>();
        final JarInputStream jis = new JarInputStream(is);
        try {
            JarEntry entry;
            final byte[] buf = new byte[ 4096];
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while ((entry = jis.getNextJarEntry()) != null) {
                FileUtils.copy(jis, bos, buf, false);
                map.put(entry.getName().intern(), bos.toByteArray());
                bos.reset();
            }
            return map;
        } finally {
            jis.close();
        }
    }
}
