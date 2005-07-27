/*
 * $Id$
 */
package org.jnode.system.repository;

import org.jnode.system.repository.spi.SystemRepositoryProvider;

public interface SystemRepository {

    public static final Class<SystemRepository> NAME = SystemRepository.class;
    
    /**
     * Add a provider
     * @param provider
     */
    public void addProvider(SystemRepositoryProvider provider);
    
    /**
     * Remove a provider
     * @param provider
     */
    public void removeProvider(SystemRepositoryProvider provider);
    
}
