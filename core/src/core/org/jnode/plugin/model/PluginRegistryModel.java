/*
 * $Id$
 */
package org.jnode.plugin.model;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import nanoxml.XMLElement;

import org.jnode.plugin.ExtensionPoint;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.plugin.PluginRegistry;
import org.jnode.util.ObjectArrayIterator;
import org.jnode.vm.VmSystemObject;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author epr
 */
public class PluginRegistryModel extends VmSystemObject implements PluginRegistry {

	private final PluginDescriptorModel[] descriptors;
	private ExtensionPoint[] extensionPointArray;

	private transient HashMap extensionsPointsCache;

	/**
	 * Create a new instance
	 */
	public PluginRegistryModel(URL[] pluginFiles) throws PluginException {
		descriptors = loadDescriptors(pluginFiles);
		resolveDescriptors();
	}

	/**
	 * Gets the descriptor of the plugin with the given id.
	 * 
	 * @param pluginId
	 * @return The plugin descriptor found, or null if not found
	 */
	public PluginDescriptor getPluginDescriptor(String pluginId) {

		final int max = descriptors.length;
		for (int i = 0; i < max; i++) {
			if (descriptors[i].getId().equals(pluginId)) {
				return descriptors[i];
			}
		}
		return null;
	}

	/**
	 * Gets the extension point with the given id.
	 * 
	 * @param id
	 * @return The extension point found, or null if not found
	 */
	public ExtensionPoint getExtensionPoint(String id) {
		return (ExtensionPoint) getExtensionPointsCache().get(id);
	}

	/**
	 * Returns an iterator to iterate over all PluginDescriptor's.
	 * 
	 * @return Iterator&lt;PluginDescriptor&gt;
	 */
	public Iterator getDescriptorIterator() {
		return new ObjectArrayIterator(descriptors);
	}

	/**
	 * Load all plugin descriptors.
	 * 
	 * @param pluginFiles
	 */
	private PluginDescriptorModel[] loadDescriptors(URL[] pluginFiles) throws PluginException {

		final int max = pluginFiles.length;
		final PluginDescriptorModel[] list = new PluginDescriptorModel[max];

		for (int i = 0; i < max; i++) {
			final URL url = pluginFiles[i];
			//reader.setValidation(true);
			//reader.setValidation(false);
			//System.out.println("url=" + url);
			final XMLElement root;
			try {
				root = new XMLElement(new Hashtable(), true, false);
				final Reader r = new InputStreamReader(url.openStream());
				try {
					root.parseFromReader(r);
				} finally {
					r.close();
				}
			} catch (IOException ex) {
				throw new PluginException(ex);
			}
			if (!root.getName().equals("plugin")) {
				throw new PluginException("plugin element expected");
			}
			list[i] = new PluginDescriptorModel(this, root);
		}

		return list;
	}

	/**
	 * Resolve all plugin descriptors.
	 * 
	 * @param pluginFiles
	 */
	private void resolveDescriptors() throws PluginException {

		final int max = descriptors.length;

		for (int i = 0; i < max; i++) {
			descriptors[i].resolve();
		}
	}

	/**
	 * Register a known extension point.
	 * 
	 * @param ep
	 */
	protected void registerExtensionPoint(ExtensionPoint ep) throws PluginException {
		final HashMap epMap = getExtensionPointsCache();
		if (epMap.containsKey(ep.getUniqueIdentifier())) {
			throw new PluginException("Duplicate extension point " + ep.getUniqueIdentifier());
		}
		epMap.put(ep.getUniqueIdentifier(), ep);
	}

	/**
	 * @see org.jnode.vm.VmSystemObject#verifyBeforeEmit()
	 */
	public void verifyBeforeEmit() {
		super.verifyBeforeEmit();
		extensionPointArray = new ExtensionPoint[getExtensionPointsCache().size()];
		getExtensionPointsCache().values().toArray(extensionPointArray);
	}

	private HashMap getExtensionPointsCache() {
		if (extensionsPointsCache == null) {
			extensionsPointsCache = new HashMap();
			if (extensionPointArray != null) {
				final int max = extensionPointArray.length;
				for (int i = 0; i < max; i++) {
					final ExtensionPoint ep = extensionPointArray[i];
					extensionsPointsCache.put(ep.getUniqueIdentifier(), ep);
				}
			}
		}
		return extensionsPointsCache;
	}

	static class DTDResolver implements EntityResolver {

		/**
		 * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String, java.lang.String)
		 */
		public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
			if ((systemId != null) && systemId.endsWith("jnode.dtd")) {
				return new InputSource(getClass().getResourceAsStream("/jnode.dtd"));
			} else {
				return null;
			}
		}

	}
}
