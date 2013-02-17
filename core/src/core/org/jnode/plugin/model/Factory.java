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

import java.net.URL;
import org.jnode.nanoxml.XMLElement;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class Factory {

    /**
     * Create a new PluginRegistry.
     *
     * @param pluginFiles URLs for plugin files with which to populate the registry.
     * @return the plugin model
     * @throws PluginException
     */
    public static PluginRegistryModel createRegistry(URL[] pluginFiles)
        throws PluginException {
        return new PluginRegistryModel(pluginFiles);
    }

    /**
     * Parse an xml descriptor, returning its contents as a PluginDescriptor.
     * The descriptor will be created with a {@code null} PluginJar.
     *
     * @param root the root element of the descriptor
     * @return the plugin descriptor
     * @throws PluginException
     */
    public static PluginDescriptor parseDescriptor(XMLElement root) throws PluginException {
        return parseDescriptor(null, root);
    }

    /**
     * Parse an xml descriptor, returning its contents as a PluginDescriptorModel.
     *
     * @param root the root element of the descriptor
     * @param jar the PluginJar for the descriptor
     * @return the plugin descriptor
     * @throws PluginException
     */
    static PluginDescriptorModel parseDescriptor(PluginJar jar, XMLElement root) throws PluginException {
        if (root.getName().equals("plugin")) {
            return new PluginDescriptorModel(jar, root);
        } else if (root.getName().equals("fragment")) {
            return new FragmentDescriptorModel(jar, root);
        } else {
            throw new PluginException("Unknown root tag " + root.getName());
        }
    }
}
