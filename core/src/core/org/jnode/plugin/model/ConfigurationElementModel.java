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
import java.util.HashSet;
import java.util.Set;

import org.jnode.nanoxml.XMLElement;
import org.jnode.plugin.ConfigurationElement;
import org.jnode.plugin.PluginException;

/**
 * @author epr
 */
final class ConfigurationElementModel extends PluginModelObject implements ConfigurationElement {

    /** Name of the current configuration element. */
    private final String name;
    private final AttributeModel[] attributes;
    /** Child elements of the current configuration element. */
    private final ConfigurationElement[] elements;

    /**
     * Create new instance of configuration element model.
     *
     * @param plugin The model for plugin descriptor.
     * @param element An XML element.
     * @throws PluginException
     */
    public ConfigurationElementModel(PluginDescriptorModel plugin, XMLElement element)
            throws PluginException {
        super(plugin);
        name = element.getName();

        final Set<String> attributeNames = element.attributeNames();
        if (!attributeNames.isEmpty()) {
            final ArrayList<AttributeModel> list = new ArrayList<AttributeModel>();
            for (String name : attributeNames) {
                final String value = element.getStringAttribute(name);
                list.add(new AttributeModel(name, value));
                if (value == null) {
                    throw new PluginException("Cannot find attribute value for attribute " + name);
                }
            }
            attributes = list.toArray(new AttributeModel[list.size()]);
        } else {
            attributes = null;
        }

        final ArrayList<ConfigurationElementModel> list =
                new ArrayList<ConfigurationElementModel>();
        for (final XMLElement ce : element.getChildren()) {
            list.add(new ConfigurationElementModel(plugin, ce));
        }
        elements = list.toArray(new ConfigurationElement[list.size()]);
    }

    /**
     * Gets the value of an attribute with a given name
     *
     * @param name Name of the element.
     * @return The attribute value, or null if not found.
     */
    @Override
    public String getAttribute(String name) {
        if (attributes != null) {
            for (AttributeModel attribute : attributes)
                if (attribute.getName().equals(name)) {
                    return attribute.getValue();
                }
        }
        return null;
    }

    /**
     * Gets the names of all attributes in this element.
     *
     * @return A set of attribute names. This set can be empty if no attributes
     *         found.
     */
    @Override
    public Set<String> attributeNames() {
        final HashSet<String> set = new HashSet<String>();
        if (attributes != null) {
            for (AttributeModel attribute : attributes) {
                set.add(attribute.getName());
            }
        }
        return set;
    }

    /**
     * Gets all child elements
     */
    @Override
    public ConfigurationElement[] getElements() {
        return elements;
    }

    /**
     * Gets the name of this element
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Resolve all references to (elements of) other plugin descriptors
     */
    @Override
    protected void resolve(PluginRegistryModel registry) {
        // Do nothing
    }

    /**
     * Remove all references to (elements of) other plugin descriptors
     */
    @Override
    protected void unresolve(PluginRegistryModel registry) {
        // Do nothing
    }

    @Override
    public String toString() {
        StringBuilder tmp = new StringBuilder(name);
        for (AttributeModel attr : attributes) {
            tmp.append(' ').append(attr.getName()).append("=\"").append(attr.getValue())
                    .append('\"');
        }
        return tmp.toString();
    }
}
