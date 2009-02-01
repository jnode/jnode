/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.partitions.service.def;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jnode.partitions.PartitionTableType;
import org.jnode.plugin.ConfigurationElement;
import org.jnode.plugin.Extension;
import org.jnode.plugin.ExtensionPoint;
import org.jnode.plugin.ExtensionPointListener;

/**
 * @author epr
 */
final class PartitionTableTypeManager implements ExtensionPointListener {
    private static final Logger log = Logger.getLogger(PartitionTableTypeManager.class);
    
    /** All registered types */
    private final HashMap<String, PartitionTableType> types =
            new HashMap<String, PartitionTableType>();
    
    /** The org.jnode.fs.types extension point */
    private final ExtensionPoint typesEP;

    /**
     * Create a new instance
     */
    protected PartitionTableTypeManager(ExtensionPoint typesEP) {
        this.typesEP = typesEP;
        if (typesEP == null) {
            throw new IllegalArgumentException("The types extension-point cannot be null");
        }
        refreshPartitionTableTypes();
    }

    /**
     * Gets all registered file system types. All instances of the returned
     * collection are instanceof FileSystemType.
     */
    public Collection<PartitionTableType> partitionTableTypes() {
        return Collections.unmodifiableCollection(types.values());
    }

    /**
     * Get a registered PartitionTableType byt its name
     * 
     * @param name the PartitionTableType name
     * @return null if it doesn't exists
     */
    public PartitionTableType getSystemType(String name) {
        return types.get(name);
    }

    /**
     * Load all known file system types.
     */
    protected synchronized void refreshPartitionTableTypes() {
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
     * 
     * @param types
     * @param element
     */
    private void createType(Map<String, PartitionTableType> types, ConfigurationElement element) {
        final String className = element.getAttribute("class");
        if (className != null) {
            try {
                final ClassLoader cl = Thread.currentThread().getContextClassLoader();
                final Object obj = cl.loadClass(className).newInstance();
                final PartitionTableType type = (PartitionTableType) obj;
                types.put(type.getName(), type);
            } catch (ClassCastException ex) {
                log.error("PartitionTableType " + className +
                        " does not implement PartitionTableType.");
            } catch (ClassNotFoundException ex) {
                log.error("Cannot load PartitionTableType " + className);
            } catch (IllegalAccessException ex) {
                log.error("No access to PartitionTableType " + className);
            } catch (InstantiationException ex) {
                log.error("Cannot instantiate PartitionTableType " + className);
            }
        }
    }

    /**
     * @see org.jnode.plugin.ExtensionPointListener#extensionAdded(org.jnode.plugin.ExtensionPoint,
     *      org.jnode.plugin.Extension)
     */
    public void extensionAdded(ExtensionPoint point, Extension extension) {
        refreshPartitionTableTypes();
    }

    /**
     * @see org.jnode.plugin.ExtensionPointListener#extensionRemoved(org.jnode.plugin.ExtensionPoint, 
     *      org.jnode.plugin.Extension)
     */
    public void extensionRemoved(ExtensionPoint point, Extension extension) {
        refreshPartitionTableTypes();
    }
}
