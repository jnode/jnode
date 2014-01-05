/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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
import org.jnode.plugin.PluginReference;
import org.jnode.util.Version;

/**
 * @author epr
 */
final class PluginPrerequisiteModel extends PluginModelObject implements PluginPrerequisite {

    private final PluginReference reference;

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
        final String pluginIdentifier = getAttribute(e, "plugin", true);
        String version = getAttribute(e, "version", false);
        if (version == null) {
            version = plugin.getVersion().toString();
        }
        reference = new PluginReference(pluginIdentifier, new Version(version));
    }

    /**
     * Initialize this instance.
     *
     * @param plugin
     * @param pluginIdentifier
     * @param pluginVersion
     */
    public PluginPrerequisiteModel(PluginDescriptorModel plugin,
                                   String pluginIdentifier, Version pluginVersion) {
        super(plugin);
        if (pluginIdentifier == null) {
            throw new IllegalArgumentException("pluginId is null");
        }
        if (pluginVersion == null) {
            throw new IllegalArgumentException("pluginVersion is null");
        }
        reference = new PluginReference(pluginIdentifier, pluginVersion);
    }


    /**
     * Gets the fully qualified reference to the plugin that is required
     *
     * @return The fully qualified reference to the required plugin.
     */
    public PluginReference getPluginReference() {
        return reference;
    }

    /**
     * Resolve all references to (elements of) other plugin descriptors
     *
     * @throws PluginException
     */
    protected void resolve(PluginRegistryModel registry)
        throws PluginException {
        if (registry.getPluginDescriptor(reference.getId()) == null) {
            throw new PluginException(
                "Unknown plugin " + reference + " in import of " + getDeclaringPluginDescriptor().getId());
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
