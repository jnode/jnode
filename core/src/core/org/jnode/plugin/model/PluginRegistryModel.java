/*
 * $Id$
 */
package org.jnode.plugin.model;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

import org.jnode.plugin.ExtensionPoint;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.plugin.PluginRegistry;
import org.jnode.util.BootableHashMap;
import org.jnode.vm.VmSystemObject;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author epr
 */
public class PluginRegistryModel extends VmSystemObject implements PluginRegistry {

	/** A map of all descriptors (id, descriptor) */
	private final BootableHashMap descriptorMap;
	/** A map off all extensionpoints (id, ep) */
	private final BootableHashMap extensionPoints;
	private transient PluginsClassLoader classLoader;

	/**
	 * Initialize this instance.
	 * 
	 * @param pluginFiles
	 */
	public PluginRegistryModel(URL[] pluginFiles) throws PluginException {
		this.extensionPoints = new BootableHashMap();
		this.descriptorMap = new BootableHashMap();
		loadDescriptors(pluginFiles);
		resolveDescriptors();
	}

	/**
	 * Gets the descriptor of the plugin with the given id.
	 * 
	 * @param pluginId
	 * @return The plugin descriptor found, or null if not found
	 */
	public PluginDescriptor getPluginDescriptor(String pluginId) {
		return (PluginDescriptor) descriptorMap.get(pluginId);
	}

	/**
	 * Gets the extension point with the given id.
	 * 
	 * @param id
	 * @return The extension point found, or null if not found
	 */
	public ExtensionPoint getExtensionPoint(String id) {
		return (ExtensionPoint) extensionPoints.get(id);
	}

	/**
	 * Returns an iterator to iterate over all PluginDescriptor's.
	 * 
	 * @return Iterator&lt;PluginDescriptor&gt;
	 */
	public Iterator getDescriptorIterator() {
		return descriptorMap.values().iterator();
	}

	/**
	 * Load all plugin descriptors.
	 * 
	 * @param pluginUrls
	 */
	private void loadDescriptors(URL[] pluginUrls) throws PluginException {
		final int max = pluginUrls.length;

		for (int i = 0; i < max; i++) {
			loadPlugin(pluginUrls[i]);
		}
	}

	/**
	 * Resolve all plugin descriptors.
	 */
	public void resolveDescriptors() throws PluginException {
		for (Iterator i = descriptorMap.values().iterator(); i.hasNext();) {
			final PluginDescriptorModel descr = (PluginDescriptorModel) i.next();
			descr.resolve();
		}
	}

	/**
	 * Register a plugin descriptor.
	 * 
	 * @param descr
	 */
	protected synchronized void registerPlugin(PluginDescriptorModel descr) throws PluginException {
		final String id = descr.getId();
		if (descriptorMap.containsKey(id)) {
			throw new PluginException("Duplicate plugin " + id);
		}
		descriptorMap.put(id, descr);
	}

	/**
	 * Register a known extension point.
	 * 
	 * @param ep
	 */
	protected synchronized void registerExtensionPoint(ExtensionPoint ep) throws PluginException {
		final BootableHashMap epMap = this.extensionPoints;
		if (epMap.containsKey(ep.getUniqueIdentifier())) {
			throw new PluginException("Duplicate extension point " + ep.getUniqueIdentifier());
		}
		epMap.put(ep.getUniqueIdentifier(), ep);
	}

	/**
	 * Load a plugin from a given URL. This will not activate the plugin.
	 * 
	 * @param pluginUrl
	 * @return The descriptor of the loaded plugin.
	 * @throws PluginException
	 */
	public PluginDescriptor loadPlugin(URL pluginUrl) throws PluginException {
		try {
			final PluginJar pluginJar = new PluginJar(this, pluginUrl);
			return pluginJar.getDescriptor();
		} catch (IOException ex) {
			throw new PluginException(ex);
		}
	}

	/**
	 * Load a plugin from a given InputStream. This will not activate the plugin.
	 * 
	 * @param is
	 * @return The descriptor of the loaded plugin.
	 * @throws PluginException
	 */
	public PluginDescriptor loadPlugin(InputStream is) throws PluginException {
		final PluginJar pluginJar = new PluginJar(this, is, null);
		return pluginJar.getDescriptor();
	}

	/**
	 * Remove the plugin with the given id from this registry.
	 * 
	 * @param pluginId
	 * @throws PluginException
	 */
	public synchronized void unloadPlugin(String pluginId) throws PluginException {
		final PluginDescriptor descr = getPluginDescriptor(pluginId);
		if (descr != null) {
			if (descr.isSystemPlugin()) {
				throw new PluginException("Cannot unload a system plugin");
			}
			if (descr.getPlugin().isActive()) {
				throw new PluginException("Cannot unload an active plugin");
			}
			descriptorMap.remove(descr.getId());
		}

	}

	/**
	 * Gets the classloader that loads classes from all loaded plugins.
	 * 
	 * @return ClassLoader
	 */
	public ClassLoader getPluginsClassLoader() {
		if (classLoader == null) {
			classLoader = new PluginsClassLoader(this);
		}
		return classLoader;
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
