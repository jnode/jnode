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
		for (Iterator<PluginDescriptor> i = registry.getDescriptorIterator(); i.hasNext();) {
			final PluginDescriptor descr = i.next();
			if (!descr.isSystemPlugin() && !descr.isFragment()) {
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
		for (Iterator<PluginDescriptor> i = registry.getDescriptorIterator(); i.hasNext();) {
			final PluginDescriptor descr = i.next();
			if (!descr.isSystemPlugin() && !descr.isFragment()) {
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
