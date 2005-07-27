/*
 * $Id$
 */
package org.jnode.system.repository;

import org.jnode.system.repository.spi.SystemRepositoryProvider;

public abstract class RepositoryPlugin {

    /** Next pointer in a linked list */
    private final RepositoryPlugin next;

    /**
     * Initialize this instance.
     * @param next
     */
    public RepositoryPlugin(RepositoryPlugin next) {
        this.next = next;
    }

    /**
     * Gets the next plugin.
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
     * @param providers The new list of providers
     * @param provider The added provider
     */
    protected void providerAdded(SystemRepositoryProvider[] providers,
            SystemRepositoryProvider provider) {
    }


    /**
     * This method is called when a system repository provider is removed.
     * @param providers The new list of providers
     * @param provider The removed provider
     */
    protected void providerRemoved(SystemRepositoryProvider[] providers,
            SystemRepositoryProvider provider) {
    }

}
