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
 
package org.jnode.fs.service.def;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jnode.fs.FileSystemType;
import org.jnode.plugin.ConfigurationElement;
import org.jnode.plugin.Extension;
import org.jnode.plugin.ExtensionPoint;
import org.jnode.plugin.ExtensionPointListener;

/**
 * @author epr
 */
final class FileSystemTypeManager implements ExtensionPointListener {

	/** My logger */
	private static final Logger log = Logger.getLogger(FileSystemTypeManager.class);
	/** All registered types */
	private final HashMap<String, FileSystemType> types = new HashMap<String, FileSystemType>();
	/** The org.jnode.fs.types extension point */
	private final ExtensionPoint typesEP;

	/**
	 * Create a new instance
	 */
	protected FileSystemTypeManager(ExtensionPoint typesEP) {
		this.typesEP = typesEP;
		if (typesEP == null) {
			throw new IllegalArgumentException("The types extension-point cannot be null");
		}
		refreshFileSystemTypes();
	}

	/**
	 * Gets all registered file system types.
	 * All instances of the returned collection are instanceof FileSystemType.
	 */
	public Collection<FileSystemType> fileSystemTypes() {
		return Collections.unmodifiableCollection(types.values());
	}
    
    /**
     * Get a registered filesystemType byt its name
     * @param name the fileSystemType name
     * @return null if it doesn't exists
     */
    public FileSystemType getSystemType(String name) {
        return types.get(name);
    }
    

	/**
	* Load all known file system types.
	*/
	protected synchronized void refreshFileSystemTypes() {
		types.clear();
		
		final Extension[] extensions = typesEP.getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			final Extension ext = extensions[i];
			final ConfigurationElement[] elements = ext.getConfigurationElements();
			for (int j = 0; j < elements.length; j++) {
				createType(types, elements[j]);
			}
		}
	}

	/**
	 * Create a filesystem type from a given configuration element
	 * @param types
	 * @param element
	 */
	private void createType(Map<String, FileSystemType> types, ConfigurationElement element) {
		final String className = element.getAttribute("class");
		if (className != null) { 
			try {
				final ClassLoader cl = Thread.currentThread().getContextClassLoader();
				final Object obj = cl.loadClass(className).newInstance();
				final FileSystemType type = (FileSystemType)obj;
				types.put(type.getName(), type);
			} catch (ClassCastException ex) {
				log.error(
					"FileSystemType "
						+ className
						+ " does not implement FileSystemType.");
			} catch (ClassNotFoundException ex) {
				log.error("Cannot load FileSystemType " + className);
			} catch (IllegalAccessException ex) {
				log.error("No access to FileSystemType " + className);
			} catch (InstantiationException ex) {
				log.error("Cannot instantiate FileSystemType " + className);
			}
		}
	}

	/**
	 * @see org.jnode.plugin.ExtensionPointListener#extensionAdded(org.jnode.plugin.ExtensionPoint, org.jnode.plugin.Extension)
	 */
	public void extensionAdded(ExtensionPoint point, Extension extension) {
		refreshFileSystemTypes();
	}

	/**
	 * @see org.jnode.plugin.ExtensionPointListener#extensionRemoved(org.jnode.plugin.ExtensionPoint, org.jnode.plugin.Extension)
	 */
	public void extensionRemoved(ExtensionPoint point, Extension extension) {
		refreshFileSystemTypes();
	}

}
