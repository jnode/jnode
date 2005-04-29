/*
 * $Id$
 */
package org.jnode.plugin.model;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import nanoxml.XMLElement;

import org.jnode.plugin.FragmentDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.system.BootLog;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class FragmentDescriptorModel extends PluginDescriptorModel implements
        FragmentDescriptor, ResourceLoader {

    private final String pluginId;

    private final String pluginVersion;
    
    private PluginDescriptorModel plugin;

    /**
     * @param jarFile
     * @param e
     * @throws PluginException
     */
    public FragmentDescriptorModel(PluginJar jarFile, XMLElement e)
            throws PluginException {
        super(jarFile, e);
        this.pluginId = getAttribute(e, "plugin-id", true);
        this.pluginVersion = getAttribute(e, "plugin-version", true);
    }

    /**
     * @param e
     * @throws PluginException
     */
    public FragmentDescriptorModel(XMLElement e) throws PluginException {
        this(null, e);
    }

    /**
     * @see org.jnode.plugin.model.ResourceLoader#containsResource(java.lang.String)
     */
    public boolean containsResource(String resourceName) {
        final PluginJar jar = getJarFile();
        if (jar != null) {
            return jar.containsResource(resourceName);
        } else {
            return false;
        }
    }

    /**
     * @see org.jnode.plugin.FragmentDescriptor#getPluginId()
     */
    public final String getPluginId() {
        return pluginId;
    }

    /**
     * @see org.jnode.plugin.FragmentDescriptor#getPluginVersion()
     */
    public final String getPluginVersion() {
        return pluginVersion;
    }

    /**
     * @see org.jnode.plugin.model.ResourceLoader#getResource(java.lang.String)
     */
    public URL getResource(String resourceName) {
        final PluginJar jar = getJarFile();
        if (jar != null) {
            return jar.getResource(resourceName);
        } else {
            return null;
        }
    }

    /**
     * @see org.jnode.plugin.model.PluginDescriptorModel#getPluginClassLoader()
     */
    public ClassLoader getPluginClassLoader() {
        if (plugin != null) {
            return plugin.getPluginClassLoader();
        } else {
            throw new IllegalStateException("Fragment not yet resolved");
        }
    }

    /**
     * @see org.jnode.plugin.model.ResourceLoader#getResourceAsStream(java.lang.String)
     */
    public InputStream getResourceAsStream(String resourceName) {
        final PluginJar jar = getJarFile();
        if (jar != null) {
            return jar.getResourceAsStream(resourceName);
        } else {
            return null;
        }
    }

    /**
     * @see org.jnode.plugin.model.PluginDescriptorModel#initializeRequiresList(java.util.List)
     */
    protected void initializeRequiresList(List<PluginPrerequisiteModel> list) {
        super.initializeRequiresList(list);
        list.add(new PluginPrerequisiteModel(this, pluginId, pluginVersion));
    }

    /**
     * @see org.jnode.plugin.model.PluginDescriptorModel#resolve(org.jnode.plugin.model.PluginRegistryModel)
     */
    public void resolve(PluginRegistryModel registry) throws PluginException {
        super.resolve(registry);
        plugin = (PluginDescriptorModel)registry.getPluginDescriptor(pluginId);
        if (plugin == null) {
            throw new PluginException("Plugin " + getPluginId() + " not found");
        }
        BootLog.info("Resolve " + getId());
        plugin.add(this);
    }

    /**
     * @see org.jnode.plugin.model.PluginDescriptorModel#unresolve(org.jnode.plugin.model.PluginRegistryModel)
     */
    protected void unresolve(PluginRegistryModel registry) throws PluginException {
        if (plugin != null) {
            plugin.remove(this);
            plugin = null;
        }
        BootLog.info("Unresolve " + getId());
        super.unresolve(registry);
    }

    /**
     * @see org.jnode.plugin.model.PluginDescriptorModel#isFragment()
     */
    public boolean isFragment() {
        return true;
    }
}
