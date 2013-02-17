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
 
package org.jnode.plugin;

import java.util.Set;

/**
 * Extension specific configuration element.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface ConfigurationElement {

    /**
     * Gets the name of this element.
     *
     * @return the name
     */
    public String getName();

    /**
     * Gets all child elements.
     *
     * @return the child elements
     */
    public ConfigurationElement[] getElements();

    /**
     * Gets the value of an attribute with a given name.
     *
     * @param name the attribute name
     * @return The attribute value, or null if not found
     */
    public String getAttribute(String name);

    /**
     * Gets the names of all attributes in this element.
     *
     * @return the set of attribute names
     */
    public Set<String> attributeNames();

    /**
     * Gets the descriptor of the plugin in which this element was declared.
     *
     * @return The descriptor
     */
    public PluginDescriptor getDeclaringPluginDescriptor();
}
