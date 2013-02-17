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
import org.jnode.plugin.Library;
import org.jnode.plugin.PluginException;

/**
 * @author epr
 */
final class LibraryModel extends PluginModelObject implements Library {

    private final String name;
    private final String[] exports;
    private final String type;

    /**
     * @param plugin
     */
    public LibraryModel(PluginDescriptorModel plugin, XMLElement e) throws PluginException {
        super(plugin);
        name = getAttribute(e, "name", true);
        type = getAttribute(e, "type", false);

        final ArrayList<String> list = new ArrayList<String>();
        for (final XMLElement exE : e.getChildren()) {
            if (exE.getName().equals("export")) {
                list.add(getAttribute(exE, "name", true));
            }
        }
        exports = (String[]) list.toArray(new String[list.size()]);
    }

    /**
     * Resolve all references to (elements of) other plugin descriptors
     *
     * @throws PluginException
     */
    protected void resolve(PluginRegistryModel registry) throws PluginException {
        // Do nothing
    }

    /**
     * Remove all references to (elements of) other plugin descriptors
     *
     * @throws PluginException
     */
    protected void unresolve(PluginRegistryModel registry) throws PluginException {
        // Do nothing
    }

    /**
     * Gets the name of the jar file or directory
     */
    public String getName() {
        return name;
    }

    /**
     * Is this a code library?
     */
    public boolean isCode() {
        return !isResource();
    }

    /**
     * Is this a resource only library?
     */
    public boolean isResource() {
        return (type != null) && (type.equals("resource"));
    }

    /**
     * Gets all declared export names
     */
    public String[] getExports() {
        return exports;
    }
}
