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

import nanoxml.XMLElement;

import org.jnode.plugin.PluginException;
import org.jnode.vm.VmSystemObject;

/**
 * @author epr
 */
public abstract class AbstractModelObject extends VmSystemObject {

	/**
	 * Utility method to get an attribute from and element and test for its presence if it is required.
	 * 
	 * @param e
	 * @param name
	 * @param required
	 * @return The attribute
	 * @throws PluginException
	 *             required is true, but the attribute was not found
	 */
	protected final String getAttribute(XMLElement e, String name, boolean required) throws PluginException {
		final String v = e.getStringAttribute(name);
		if (required) {
			if (v == null) {
				throw new PluginException("Required attribute " + name + " in element " + e.getName() + " not found");
			}
		}
		return v;
	}

	/**
	 * Utility method to get an attribute from and element and test for its presence if it is required.
	 * 
	 * @param e
	 * @param name
	 * @param defValue
	 * @return The attribute
	 * @throws PluginException
	 *             required is true, but the attribute was not found
	 */
	protected final boolean getBooleanAttribute(XMLElement e, String name, boolean defValue) throws PluginException {
		final String v = getAttribute(e, name, false);
		if (v == null) {
			return defValue;
		}
		return v.equalsIgnoreCase("true") || v.equals("1") || v.equalsIgnoreCase("yes") || v.equalsIgnoreCase("on");
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
