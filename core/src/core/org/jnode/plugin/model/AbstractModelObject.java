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
import org.jnode.vm.objects.VmSystemObject;

/**
 * @author epr
 */
abstract class AbstractModelObject extends VmSystemObject {

    /**
     * Utility method to get an attribute from and element and test for its presence if it is required.
     *
     * @param e
     * @param name
     * @param required
     * @return The attribute
     * @throws PluginException required is true, but the attribute was not found
     */
    protected final String getAttribute(XMLElement e, String name, boolean required) throws PluginException {
        final String v = e.getStringAttribute(name);
        if (required) {
            if (v == null) {
                throw new PluginException("Required attribute " + name + " in element " + e.getName() + " not found");
            }
        }
        if (v != null) {
            return v.intern();
        } else {
            return null;
        }
    }

    /**
     * Utility method to get an attribute from and element and test for its presence if it is required.
     *
     * @param e
     * @param name
     * @param defValue
     * @return The attribute
     * @throws PluginException required is true, but the attribute was not found
     */
    protected final boolean getBooleanAttribute(XMLElement e, String name, boolean defValue) throws PluginException {
        final String v = getAttribute(e, name, false);
        if (v == null) {
            return defValue;
        }
        return v.equalsIgnoreCase("true") || v.equals("1") || v.equalsIgnoreCase("yes") || v.equalsIgnoreCase("on");
    }

    /**
     * Utility method to get an attribute from and element and test for its presence if it is required.
     *
     * @param e
     * @param name
     * @param defValue
     * @return The attribute
     * @throws PluginException required is true, but the attribute was not found
     */
    protected final int getIntAttribute(XMLElement e, String name, int defValue) throws PluginException {
        final String v = getAttribute(e, name, false);
        if (v == null) {
            return defValue;
        }
        try {
            return Integer.parseInt(v);
        } catch (Exception ex) {
            return defValue;
        }
    }

    /**
     * Resolve all references to (elements of) other plugin descriptors
     *
     * @throws PluginException
     */
    protected abstract void resolve(PluginRegistryModel registry) throws PluginException;

    /**
     * Remove all references to (elements of) other plugin descriptors
     *
     * @throws PluginException
     */
    protected abstract void unresolve(PluginRegistryModel registry) throws PluginException;
}
