/*
 * $Id$
 */
package org.jnode.plugin.model;

import java.util.ArrayList;
import java.util.Iterator;

import nanoxml.XMLElement;

import org.jnode.plugin.Library;
import org.jnode.plugin.PluginException;

/**
 * @author epr
 */
public class LibraryModel extends PluginModelObject implements Library {

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

		final ArrayList list = new ArrayList();
		for (Iterator i = e.getChildren().iterator(); i.hasNext();) {
			final XMLElement exE = (XMLElement) i.next();
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
