/*
 * $Id$
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
	 * Utility method to get an attribute from and element and test
	 * for its presence if it is required.
	 * @param e
	 * @param name
	 * @param required
	 * @return The attribute
	 * @throws PluginException required is true, but the attribute was not found
	 */
	protected final String getAttribute(XMLElement e, String name, boolean required) 
	throws PluginException { 
		final String v = e.getStringAttribute(name);
		if (required) {
			if (v == null) {
				throw new PluginException("Required attribute " + name + " in element " + e.getName() + " not found");
			}
		}
		return v;		
	}
	
	/**
	 * Resolve all references to (elements of) other plugin descriptors
	 * @throws PluginException
	 */
	protected abstract void resolve() 
	throws PluginException;
}
