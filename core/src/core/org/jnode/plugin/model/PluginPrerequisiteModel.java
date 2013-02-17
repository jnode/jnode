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
 
package org.jnode.plugin.model;

import org.jnode.nanoxml.XMLElement;
import org.jnode.plugin.PluginException;
import org.jnode.plugin.PluginPrerequisite;

/**
 * @author epr
 */
final class PluginPrerequisiteModel extends PluginModelObject implements PluginPrerequisite {

    private final String pluginIdentifier;
    private final String version;

    /**
     * Initialize this instance.
     *
     * @param plugin
     * @param e
     * @throws PluginException
     */
    public PluginPrerequisiteModel(PluginDescriptorModel plugin, XMLElement e)
        throws PluginException {
        super(plugin);
        this.pluginIdentifier = getAttribute(e, "plugin", true);
        final String version = getAttribute(e, "version", false);
        if (version != null) {
            this.version = version;
        } else {
            this.version = plugin.getVersion();
        }
    }

    /**
     * Initialize this instance.
     *
     * @param plugin
     * @param pluginIdentifier
     * @param pluginVersion
     */
    public PluginPrerequisiteModel(PluginDescriptorModel plugin,
                                   String pluginIdentifier, String pluginVersion) {
        super(plugin);
        if (pluginIdentifier == null) {
            throw new IllegalArgumentException("pluginId is null");
        }
        if (pluginVersion == null) {
            throw new IllegalArgumentException("pluginVersion is null");
        }
        this.pluginIdentifier = pluginIdentifier;
        this.version = pluginVersion;
    }

    /**
     * Gets the identifier of the plugin that is required
     */
    public String getPluginId() {
        return pluginIdentifier;
    }


    /**
     * Gets the version of the plugin that is required.
     * If not specified, this version is equal to the version of the
     * declaring plugin.
     *
     * @return The version
     */
    public String getPluginVersion() {
        return version;
    }

    /**
     * Resolve all references to (elements of) other plugin descriptors
     *
     * @throws PluginException
     */
    protected void resolve(PluginRegistryModel registry)
        throws PluginException {
        if (registry.getPluginDescriptor(pluginIdentifier) == null) {
            throw new PluginException(
                "Unknown plugin " + pluginIdentifier + " in import of " + getDeclaringPluginDescriptor().getId());
        }
    }

    /**
     * Remove all references to (elements of) other plugin descriptors
     *
     * @throws PluginException
     */
    protected void unresolve(PluginRegistryModel registry) throws PluginException {
        // Nothing to do
    }
}
