/*
 * $Id$
 */
package org.jnode.plugin.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
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

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class PluginJar implements BootableObject {

    /** The descriptor of this file */
    private final PluginDescriptorModel descriptor;

    /** The resources in the jar file */
    private final Map resources;

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
        if (!root.getName().equals("plugin")) { throw new PluginException(
                "plugin element expected"); }
        this.descriptor = new PluginDescriptorModel(this, root);
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

    private Map loadResources(InputStream is) throws IOException {
        final BootableHashMap map = new BootableHashMap();
        final JarInputStream jis = new JarInputStream(is);
        try {
            JarEntry entry;
            final byte[] buf = new byte[ 4096];
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while ((entry = jis.getNextJarEntry()) != null) {
                FileUtils.copy(jis, bos, buf, false);
                map.put(entry.getName(), bos.toByteArray());
                bos.reset();
            }
            return map;
        } finally {
            jis.close();
        }
    }
}