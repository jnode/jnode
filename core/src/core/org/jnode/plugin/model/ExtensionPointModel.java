/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.plugin.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jnode.nanoxml.XMLElement;
import org.jnode.plugin.Extension;
import org.jnode.plugin.ExtensionPoint;
import org.jnode.plugin.ExtensionPointListener;
import org.jnode.plugin.PluginException;

/**
 * @author epr
 */
final class ExtensionPointModel extends PluginModelObject implements
    ExtensionPoint {

    private final String id;

    private final String uniqueId;

    private final String name;

    private List<ExtensionPointListener> listeners;

    private Extension[] extensionArray;

    private transient List<Extension> extensionsCache;

    public ExtensionPointModel(PluginDescriptorModel plugin, XMLElement e)
        throws PluginException {
        super(plugin);
        id = getAttribute(e, "id", true);
        name = getAttribute(e, "name", true);
        uniqueId = plugin.getId() + '.' + id;
        if (id.indexOf('.') >= 0) {
            throw new PluginException("id cannot contain a '.'");
        }
    }

    /**
     * Resolve all references to (elements of) other plugin descriptors
     *
     * @throws PluginException
     */
    protected void resolve(PluginRegistryModel registry) throws PluginException {
        registry.registerExtensionPoint(this);
    }

    /**
     * Remove all references to (elements of) other plugin descriptors
     *
     * @throws PluginException
     */
    protected void unresolve(PluginRegistryModel registry)
        throws PluginException {
        registry.unregisterExtensionPoint(this);
    }

    /**
     * Returns the simple identifier of this extension point. This identifier is
     * a non-empty string containing no period characters ('.') and is
     * guaranteed to be unique within the defining plug-in.
     */
    public String getSimpleIdentifier() {
        return id;
    }

    /**
     * Returns the unique identifier of this extension point. This identifier is
     * unique within the plug-in registry, and is composed of the identifier of
     * the plug-in that declared this extension point and this extension point's
     * simple identifier.
     */
    public String getUniqueIdentifier() {
        return uniqueId;
    }

    /**
     * Gets the human readable name of this extensionpoint
     */
    public String getName() {
        return name;
    }

    /**
     * Gets all extensions configured to this extensionpoint.
     */
    public Extension[] getExtensions() {
        if (extensionArray == null) {
            final List<Extension> cache = getExtensionsCache();
            extensionArray = (Extension[]) cache.toArray(new Extension[cache
                .size()]);
        }
        return extensionArray;
    }

    /**
     * Add a listener
     *
     * @param listener
     */
    public void addListener(ExtensionPointListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<ExtensionPointListener>();
        }
        listeners.add(listener);
    }

    /**
     * Add a listener to the front of the listeners list.
     *
     * @param listener
     */
    public void addPriorityListener(ExtensionPointListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<ExtensionPointListener>();
        }
        listeners.add(0, listener);
    }

    /**
     * Remove a listener
     *
     * @param listener
     */
    public void removeListener(ExtensionPointListener listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    /**
     * Add an extension to this point.
     *
     * @param extension
     */
    protected void add(Extension extension) {
        final List<Extension> extensions = getExtensionsCache();
        if (!extensions.contains(extension)) {
            extensions.add(extension);
            // Re-create the array
            extensionArray = null;
            fireExtensionAdded(extension);
        }
    }

    /**
     * Add an extension to this point.
     *
     * @param extension
     */
    protected void remove(Extension extension) {
        final List<Extension> extensions = getExtensionsCache();
        extensions.remove(extension);
        extensionArray = null;
        fireExtensionRemoved(extension);
    }

    /**
     * Fire and extensionAdded event to all listeners
     *
     * @param extension
     */
    protected void fireExtensionAdded(Extension extension) {
        if (listeners != null) {
            for (ExtensionPointListener l : listeners) {
                l.extensionAdded(this, extension);
            }
        }
    }

    /**
     * Fire and extensionRemoved event to all listeners
     *
     * @param extension
     */
    protected void fireExtensionRemoved(Extension extension) {
        if (listeners != null) {
            for (ExtensionPointListener l : listeners) {
                l.extensionRemoved(this, extension);
            }
        }
    }

    /**
     * Gets the extension cache. This will re-create the cache from the
     * extensionArray if needed.
     */
    private List<Extension> getExtensionsCache() {
        if (extensionsCache == null) {
            extensionsCache = new ArrayList<Extension>();
            if (extensionArray != null) {
                Collections.addAll(extensionsCache, extensionArray);
            }
        }
        return extensionsCache;
    }

    /**
     * @see org.jnode.vm.objects.VmSystemObject#verifyBeforeEmit()
     */
    public void verifyBeforeEmit() {
        super.verifyBeforeEmit();
        // System.out.println("Cache->Array " + extensionsCache);
        if (extensionsCache != null) {
            extensionArray = (Extension[]) extensionsCache
                .toArray(new Extension[extensionsCache.size()]);
        }
    }
}
