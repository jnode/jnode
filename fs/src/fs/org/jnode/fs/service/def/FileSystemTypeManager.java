/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.fs.service.def;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jnode.fs.FileSystemType;
import org.jnode.plugin.ConfigurationElement;
import org.jnode.plugin.Extension;
import org.jnode.plugin.ExtensionPoint;
import org.jnode.plugin.ExtensionPointListener;

/**
 * This class contains implementation of methods use to manage {@link FileSystemType}.
 *  
 * @author epr
 */
final class FileSystemTypeManager implements ExtensionPointListener {

    /** My logger */
    private static final Logger log = Logger.getLogger(FileSystemTypeManager.class);
    /** All registered file system types */
    private final Map<Class<?>, FileSystemType<?>> types =
            new HashMap<Class<?>, FileSystemType<?>>();
    /** The extension point for file system types*/
    private final ExtensionPoint typesEP;

    /**
     * Construct new file system manager.
     * 
     * @param typesEP {@link ExtensionPoint} for file system types.
     * 
     * @throws IllegalArgumentException if typesEP is null;
     */
    protected FileSystemTypeManager(ExtensionPoint typesEP) throws IllegalArgumentException {
        this.typesEP = typesEP;
        if (typesEP == null) {
            throw new IllegalArgumentException("The types extension-point cannot be null");
        }
        refreshFileSystemTypes();
    }

    
    /**
     * Gets all registered file system types
     * 
     * @return synchronized collection of FileSystemType.
     */
    public synchronized Collection<FileSystemType<?>> fileSystemTypes() {
        return new ArrayList<FileSystemType<?>>(types.values());
    }
   
    /**
     * Get a registered filesystemType by its name
     * 
     * @param name the fileSystemType name.
     * 
     * @return a FileSystemType implementation or null if it doesn't exists.
     */
    public synchronized <T extends FileSystemType<?>> T getSystemType(Class<T> name) {
        // FIXME ... there is a real type problem here.  There is nothing to stop the
        // method returning a FileSystemType for the wrong kind of file system.  We
        // should either figure out how to make this typesafe or change the signature
        // to return FileSystemType<?>
        return (T) types.get(name);
    }

    /**
     * Load all known file system types.
     */
    protected final void refreshFileSystemTypes() {
        // Create new type map
        Map<Class<?>, FileSystemType<?>> newTypes = new HashMap<Class<?>, FileSystemType<?>>();
        final Extension[] extensions = typesEP.getExtensions();
        for (int i = 0; i < extensions.length; i++) {
            final Extension ext = extensions[i];
            final ConfigurationElement[] elements = ext.getConfigurationElements();
            for (int j = 0; j < elements.length; j++) {
                createType(newTypes, elements[j]);
            }
        }

        // Save new type map
        synchronized (this) {
            this.types.clear();
            this.types.putAll(newTypes);
        }
    }

    /**
     * Create a file system type from a given configuration element.
     * 
     * @param types the file system type.
     * @param element the configuration element.
     */
    private void createType(Map<Class<?>, FileSystemType<?>> types, ConfigurationElement element) {
        final String className = element.getAttribute("class");
        if (className != null) {
            try {
                final ClassLoader cl = Thread.currentThread().getContextClassLoader();
                final Object obj = cl.loadClass(className).newInstance();
                final FileSystemType<?> type = (FileSystemType<?>) obj;
                types.put(type.getClass(), type);
            } catch (ClassCastException ex) {
                log.error("FileSystemType " + className + " does not implement FileSystemType.");
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
     * @see org.jnode.plugin.ExtensionPointListener#extensionAdded(org.jnode.plugin.ExtensionPoint,
     *      org.jnode.plugin.Extension)
     */
    public void extensionAdded(ExtensionPoint point, Extension extension) {
        refreshFileSystemTypes();
    }

    /**
     * @see org.jnode.plugin.ExtensionPointListener#extensionRemoved(org.jnode.plugin.ExtensionPoint, 
     *      org.jnode.plugin.Extension)
     */
    public void extensionRemoved(ExtensionPoint point, Extension extension) {
        refreshFileSystemTypes();
    }
}
