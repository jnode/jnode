/*
 * $Id$
 */
package org.jnode.plugin.model;

import java.util.ArrayList;
import java.util.Iterator;

import nanoxml.XMLElement;

import org.jnode.plugin.Library;
import org.jnode.plugin.PluginException;
import org.jnode.plugin.Runtime;

/**
 * @author epr
 */
public class RuntimeModel extends PluginModelObject implements Runtime {

	private final LibraryModel[] libraries;

	/**
	 * @param plugin
	 */
	public RuntimeModel(PluginDescriptorModel plugin, XMLElement e) throws PluginException {
		super(plugin);

		final ArrayList list = new ArrayList();
		for (Iterator i = e.getChildren().iterator(); i.hasNext();) {
			final XMLElement lE = (XMLElement) i.next();
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
	 * @throws DocumentException
	 */
	protected void resolve() throws PluginException {
		for (int i = 0; i < libraries.length; i++) {
			libraries[i].resolve();
		}
	}
}
