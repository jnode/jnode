/*
 * $Id$
 */
package org.jnode.plugin.model;

import java.util.ArrayList;
import java.util.Iterator;

import nanoxml.XMLElement;

import org.jnode.plugin.ConfigurationElement;
import org.jnode.plugin.Extension;
import org.jnode.plugin.PluginException;

/**
 * @author epr
 */
public class ExtensionModel extends PluginModelObject implements Extension {

	private final String id;
	private final String uniqueId;
	private final String point;
	private final ConfigurationElement[] elements;

	/**
	 * Create a new instance
	 * 
	 * @param e
	 */
	public ExtensionModel(PluginDescriptorModel plugin, XMLElement e) 
	throws PluginException {
		super(plugin);
		point = getAttribute(e, "point", true);
		id = getAttribute(e, "id", false);

		final ArrayList list = new ArrayList();
		for (Iterator i = e.getChildren().iterator(); i.hasNext();) {
			final XMLElement ce = (XMLElement) i.next();
			list.add(new ConfigurationElementModel(plugin, ce));
		}
		elements = (ConfigurationElement[]) list.toArray(new ConfigurationElement[list.size()]);

		if (id != null) {
			if (id.indexOf('.') >= 0) {
				throw new PluginException("id cannot contain an '.' character");
			}
			uniqueId = plugin.getId() + "." + id;
		} else {
			uniqueId = null;
		}
	}

	protected void resolve() throws PluginException {
		final ExtensionPointModel ep = (ExtensionPointModel) getDeclaringPluginDescriptor().getPluginRegistry().getExtensionPoint(point);
		if (ep == null) {
			throw new PluginException("Unknown extension-point " + point);
		} else {
			ep.add(this);
		}
	}

	/**
	 * Returns the simple identifier of this extension, or null if this extension does not have an
	 * identifier. This identifier is specified in the plug-in manifest (plugin.xml) file as a
	 * non-empty string containing no period characters ('.') and must be unique within the
	 * defining plug-in.
	 */
	public String getSimpleIdentifier() {
		return id;
	}

	/**
	 * Returns the unique identifier of this extension, or null if this extension does not have an
	 * identifier. If available, this identifier is unique within the plug-in registry, and is
	 * composed of the identifier of the plug-in that declared this extension and this extension's
	 * simple identifier.
	 */
	public String getUniqueIdentifier() {
		return uniqueId;
	}

	/**
	 * Gets all child elements
	 * 
	 * @return List&lt;Element&gt;
	 */
	public ConfigurationElement[] getConfigurationElements() {
		return elements;
	}

	/**
	 * Gets the name of the extension-point this extension connects to.
	 */
	public String getExtensionPointUniqueIdentifier() {
		return point;
	}
}
