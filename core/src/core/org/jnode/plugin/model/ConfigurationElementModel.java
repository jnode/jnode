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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import nanoxml.XMLElement;

import org.jnode.plugin.ConfigurationElement;
import org.jnode.plugin.PluginException;

/**
 * @author epr
 */
final class ConfigurationElementModel extends PluginModelObject implements ConfigurationElement {

    private final String name;
    private final AttributeModel[] attributes;
    private final ConfigurationElement[] elements;

    /**
     * Create a new instance
     *
     * @param e
     */
    public ConfigurationElementModel(PluginDescriptorModel plugin, XMLElement e)
            throws PluginException {
        super(plugin);
        name = e.getName();

        final Enumeration<?> aI = e.enumerateAttributeNames();
        if (aI.hasMoreElements()) {
            final ArrayList<AttributeModel> list = new ArrayList<AttributeModel>();
            while (aI.hasMoreElements()) {
                final String name = (String) aI.nextElement();
                final String value = e.getStringAttribute(name);
                list.add(new AttributeModel(name, value));
                if (value == null) {
                    throw new PluginException("Cannot find attribute value for attribute " + name);
                }
                //System.out.println("name[" + name + "] value[" + value + "]");
            }
            attributes = (AttributeModel[]) list.toArray(new AttributeModel[list.size()]);
        } else {
            attributes = null;
        }

        final ArrayList<ConfigurationElementModel> list = new ArrayList<ConfigurationElementModel>();
        for (Iterator<?> i = e.getChildren().iterator(); i.hasNext();) {
            final XMLElement ce = (XMLElement) i.next();
            list.add(new ConfigurationElementModel(plugin, ce));
        }
        elements = (ConfigurationElement[]) list.toArray(new ConfigurationElement[list.size()]);
    }

    /**
     * Gets the value of an attribute with a given name
     *
     * @param name
     * @return The attribute value, or null if not found
     */
    public String getAttribute(String name) {
        if (attributes != null) {
            final int max = attributes.length;
            for (int i = 0; i < max; i++) {
                if (attributes[i].getName().equals(name)) {
                    return attributes[i].getValue();
                }
            }
        }
        return null;
    }


    /**
     * Gets the names of all attributes in this element.
     *
     * @return
     */
    public Set<String> attributeNames() {
        final HashSet<String> set = new HashSet<String>();
        if (attributes != null) {
            for (AttributeModel attr : attributes) {
                set.add(attr.getName());
            }
        }
        return set;
    }

    /**
     * Gets all child elements
     */
    public ConfigurationElement[] getElements() {
        return elements;
    }

    /**
     * Gets the name of this element
     */
    public String getName() {
        return name;
    }


    /**
     * Resolve all references to (elements of) other plugin descriptors
     */
    protected void resolve(PluginRegistryModel registry) {
        // Do nothing
    }

    /**
     * Remove all references to (elements of) other plugin descriptors
     */
    protected void unresolve(PluginRegistryModel registry) {
        // Do nothing
    }

    public String toString() {
        StringBuilder tmp = new StringBuilder(name);
        for (AttributeModel attr : attributes) {
            tmp.append(' ').append(attr.getName()).append("=\"").append(attr.getValue()).append('\"');
        }
        return tmp.toString();
    }
}
