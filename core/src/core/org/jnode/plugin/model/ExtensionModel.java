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

import java.util.ArrayList;
import org.jnode.nanoxml.XMLElement;
import org.jnode.plugin.ConfigurationElement;
import org.jnode.plugin.Extension;
import org.jnode.plugin.PluginException;

/**
 * @author epr
 */
final class ExtensionModel extends PluginModelObject implements Extension {

    private final String id;
    private final String uniqueId;
    private final String point;
    private final ConfigurationElement[] elements;

    /**
     * Create a new instance
     *
     * @param e
     */
    public ExtensionModel(PluginDescriptorModel plugin, XMLElement e)
        throws PluginException {
        super(plugin);
        point = getAttribute(e, "point", true);
        id = getAttribute(e, "id", false);

        final ArrayList<ConfigurationElementModel> list = new ArrayList<ConfigurationElementModel>();
        for (final XMLElement ce : e.getChildren()) {
            list.add(new ConfigurationElementModel(plugin, ce));
        }
        elements = (ConfigurationElement[]) list.toArray(new ConfigurationElement[list.size()]);

        if (id != null) {
            if (id.indexOf('.') >= 0) {
                throw new PluginException("id cannot contain an '.' character");
            }
            uniqueId = plugin.getId() + '.' + id;
        } else {
            uniqueId = null;
        }
    }

    protected void resolve(PluginRegistryModel registry) throws PluginException {
        final ExtensionPointModel ep = (ExtensionPointModel) registry.getExtensionPoint(point);
        if (ep == null) {
            throw new PluginException("Unknown extension-point " + point);
        } else {
            ep.add(this);
        }
    }


    /**
     * Remove all references to (elements of) other plugin descriptors
     *
     * @throws PluginException
     */
    protected void unresolve(PluginRegistryModel registry) throws PluginException {
        final ExtensionPointModel ep = (ExtensionPointModel) registry.getExtensionPoint(point);
        if (ep == null) {
            throw new PluginException("Unknown extension-point " + point);
        } else {
            ep.remove(this);
        }
    }

    /**
     * Returns the simple identifier of this extension, or null if this extension does not have an
     * identifier. This identifier is specified in the plug-in manifest (plugin.xml) file as a
     * non-empty string containing no period characters ('.') and must be unique within the
     * defining plug-in.
     */
    public String getSimpleIdentifier() {
        return id;
    }

    /**
     * Returns the unique identifier of this extension, or null if this extension does not have an
     * identifier. If available, this identifier is unique within the plug-in registry, and is
     * composed of the identifier of the plug-in that declared this extension and this extension's
     * simple identifier.
     */
    public String getUniqueIdentifier() {
        return uniqueId;
    }

    /**
     * Gets all child elements
     *
     * @return List&lt;Element&gt;
     */
    public ConfigurationElement[] getConfigurationElements() {
        return elements;
    }

    /**
     * Gets the name of the extension-point this extension connects to.
     */
    public String getExtensionPointUniqueIdentifier() {
        return point;
    }

    public String getExtensionPointPluginId() {
        final int idx = point.lastIndexOf('.');
        return point.substring(0, idx);
    }
}
