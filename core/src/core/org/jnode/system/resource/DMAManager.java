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
 
package org.jnode.system.resource;


/**
 * Interface of Manager or Direct Memory Access resources.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface DMAManager {

    /**
     * Name used to bind this service into the Initial Namespace
     */
    public static final Class<DMAManager> NAME = DMAManager.class; //"system/DMAService";

    /**
     * Claim a DMA channel identified by the given number.
     *
     * @param owner
     * @param dmanr
     * @return The claimed resource
     * @throws IllegalArgumentException Invalid dmanr
     * @throws ResourceNotFreeException Requested DMA channel is in use
     */
    public DMAResource claimDMAChannel(ResourceOwner owner, int dmanr)
        throws IllegalArgumentException, ResourceNotFreeException;


}
