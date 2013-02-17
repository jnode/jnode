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
import org.jnode.plugin.Runtime;

/**
 * @author epr
 */
final class RuntimeModel extends PluginModelObject implements Runtime {

    private final LibraryModel[] libraries;

    /**
     * @param plugin
     */
    public RuntimeModel(PluginDescriptorModel plugin, XMLElement e) throws PluginException {
        super(plugin);

        final ArrayList<LibraryModel> list = new ArrayList<LibraryModel>();
        for (final XMLElement lE : e.getChildren()) {
            if (lE.getName().equals("library")) {
                list.add(new LibraryModel(plugin, lE));
            }
        }
        libraries = (LibraryModel[]) list.toArray(new LibraryModel[list.size()]);
    }

    /**
     * Gets all declared libraries
     *
     * @see org.jnode.plugin.Runtime#getLibraries()
     */
    public Library[] getLibraries() {
        return libraries;
    }

    /**
     * Resolve all references to (elements of) other plugin descriptors
     *
     * @throws PluginException
     */
    protected void resolve(PluginRegistryModel registry) throws PluginException {
        for (LibraryModel library : libraries) {
            library.resolve(registry);
        }
    }

    /**
     * Remove all references to (elements of) other plugin descriptors
     *
     * @throws PluginException
     */
    protected void unresolve(PluginRegistryModel registry) throws PluginException {
        for (LibraryModel library : libraries) {
            library.unresolve(registry);
        }
    }
}
