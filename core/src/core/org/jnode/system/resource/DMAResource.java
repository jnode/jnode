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
 * Direct Memory Access resource.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface DMAResource extends Resource {

    /**
     * I/O to memory
     */
    public static final int MODE_READ = 1;
    /**
     * Memory to I/O
     */
    public static final int MODE_WRITE = 2;

    /**
     * Prepare this channel for a data transfer.
     *
     * @param address
     * @param length
     * @param mode
     * @throws IllegalArgumentException
     * @throws DMAException
     */
    public void setup(MemoryResource address, int length, int mode)
        throws IllegalArgumentException, DMAException;

    /**
     * Enable the datatransfer of this channel. This may only be called
     * after a succesful call to setup.
     *
     * @throws DMAException
     */
    public void enable()
        throws DMAException;

    /**
     * Gets the remaining length for this channel
     *
     * @return The remaining length
     * @throws DMAException
     */
    public int getLength()
        throws DMAException;
}
