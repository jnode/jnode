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
 
package org.jnode.system.repository;

import org.jnode.system.repository.spi.SystemRepositoryProvider;

public abstract class RepositoryPlugin {

    /**
     * Next pointer in a linked list
     */
    private final RepositoryPlugin next;

    /**
     * Initialize this instance.
     *
     * @param next
     */
    public RepositoryPlugin(RepositoryPlugin next) {
        this.next = next;
    }

    /**
     * Gets the next plugin.
     *
     * @return
     */
    final RepositoryPlugin getNext() {
        return next;
    }

    /**
     * This method is called when the repository starts.
     */
    protected void start() {
    }

    /**
     * This method is called when the repository stops.
     */
    protected void stop() {
    }

    /**
     * This method is called when a system repository provider is added.
     *
     * @param providers The new list of providers
     * @param provider  The added provider
     */
    protected void providerAdded(SystemRepositoryProvider[] providers,
                                 SystemRepositoryProvider provider) {
    }


    /**
     * This method is called when a system repository provider is removed.
     *
     * @param providers The new list of providers
     * @param provider  The removed provider
     */
    protected void providerRemoved(SystemRepositoryProvider[] providers,
                                   SystemRepositoryProvider provider) {
    }

}
