/*
 * $Id$
 */
package org.jnode.plugin.model;

import java.net.URL;
import java.util.Iterator;

import org.jnode.plugin.PluginDescriptor;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class PluginsClassLoader extends ClassLoader {

	private final PluginRegistryModel registry;

	public PluginsClassLoader(PluginRegistryModel registry) {
		this.registry = registry;
	}

	/**
	 * @see java.lang.ClassLoader#findClass(java.lang.String)
	 */
	protected Class findClass(String name) throws ClassNotFoundException {
		for (Iterator i = registry.getDescriptorIterator(); i.hasNext();) {
			final PluginDescriptor descr = (PluginDescriptor) i.next();
			if (!descr.isSystemPlugin()) {
				final PluginClassLoaderImpl cl = (PluginClassLoaderImpl) descr.getPluginClassLoader();
				if (cl.containsClass(name)) {
					return cl.loadClass(name);
				}
			}
		}
		throw new ClassNotFoundException(name);
	}

	/**
	 * @see java.lang.ClassLoader#findResource(java.lang.String)
	 */
	protected URL findResource(String name) {
		for (Iterator i = registry.getDescriptorIterator(); i.hasNext();) {
			final PluginDescriptor descr = (PluginDescriptor) i.next();
			if (!descr.isSystemPlugin()) {
				final PluginClassLoaderImpl cl = (PluginClassLoaderImpl) descr.getPluginClassLoader();
				final URL url = cl.getResource(name);
				if (url != null) {
					return url;
				}
			}
		}
		return null;
	}
}
