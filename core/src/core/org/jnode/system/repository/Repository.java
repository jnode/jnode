/*
 * $Id$
 *
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
 
package org.jnode.system.repository;

import java.util.ArrayList;
import java.util.List;

import org.jnode.system.repository.spi.SystemRepositoryProvider;

/**
 * Implementation of the system repository.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class Repository implements SystemRepository {

    /**
     * All providers
     */
    private final List<SystemRepositoryProvider> providerList = new ArrayList<SystemRepositoryProvider>();

    /**
     * Array of all providers (duplicate of providerList)
     */
    private SystemRepositoryProvider[] providers;

    /**
     * The linked list of plugins
     */
    private final RepositoryPlugin plugins;

    /**
     * Initialize this instance.
     *
     * @param plugins
     */
    public Repository(RepositoryPlugin plugins) {
        this.plugins = plugins;
    }

    /**
     * Start this repository
     */
    public void start() {
        RepositoryPlugin p = plugins;
        while (p != null) {
            p.start();
            p = p.getNext();
        }
    }

    /**
     * Start this repository
     */
    public void stop() {
        RepositoryPlugin p = plugins;
        while (p != null) {
            p.stop();
            p = p.getNext();
        }
    }

    /**
     * @see org.jnode.system.repository.SystemRepository
     * #addProvider(org.jnode.system.repository.spi.SystemRepositoryProvider)
     */
    public void addProvider(SystemRepositoryProvider provider) {
        SystemRepositoryProvider[] arr;
        synchronized (this) {
            providerList.remove(provider);
            arr = getProviderArray();
        }

        RepositoryPlugin p = plugins;
        while (p != null) {
            p.providerAdded(arr, provider);
            p = p.getNext();
        }
    }

    /**
     * @see org.jnode.system.repository.SystemRepository
     * #removeProvider(org.jnode.system.repository.spi.SystemRepositoryProvider)
     */
    public void removeProvider(SystemRepositoryProvider provider) {
        SystemRepositoryProvider[] arr;
        synchronized (this) {
            providerList.remove(provider);
            arr = getProviderArray();
        }

        RepositoryPlugin p = plugins;
        while (p != null) {
            p.providerAdded(arr, provider);
            p = p.getNext();
        }
    }

    private final SystemRepositoryProvider[] getProviderArray() {
        SystemRepositoryProvider[] arr = this.providers;
        if (arr == null) {
            arr = new SystemRepositoryProvider[providerList.size()];
            providerList.toArray(arr);
            this.providers = arr;
        }
        return arr;
    }
}
