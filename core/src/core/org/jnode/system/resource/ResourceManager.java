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

import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;

/**
 * Interface or Manager of all system resources.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface ResourceManager {

    /**
     * Name used to bind the ResourceManager under in the InitialNaming namespace.
     */
    public static final Class<ResourceManager> NAME = ResourceManager.class;

    /**
     * Allocate memory anywhere in the address space
     */
    public static final int MEMMODE_NORMAL = 0x00;

    /**
     * Allocate memory compatible with DMA rules
     */
    public static final int MEMMODE_ALLOC_DMA = 0x01;

    /**
     * Claim a range of IO ports.
     * <p/>
     * This method will requires a ResourcePermission("ioports").
     *
     * @param owner
     * @param startPort
     * @param length
     * @return The claimed resource
     * @throws ResourceNotFreeException
     */
    public IOResource claimIOResource(ResourceOwner owner, int startPort, int length) throws ResourceNotFreeException;

    /**
     * Claim a memory region
     *
     * @param owner
     * @param start
     * @param size
     * @param mode
     * @return The claimed resource
     * @throws ResourceNotFreeException
     */
    public MemoryResource claimMemoryResource(ResourceOwner owner, Address start, Extent size, int mode)
        throws ResourceNotFreeException;

    /**
     * Claim a memory region
     *
     * @param owner
     * @param start
     * @param size
     * @param mode
     * @return The claimed resource
     * @throws ResourceNotFreeException
     */
    public MemoryResource claimMemoryResource(ResourceOwner owner, Address start, int size, int mode)
        throws ResourceNotFreeException;

    /**
     * Register an interrupt handler for a given irq number.
     *
     * @param owner
     * @param irq
     * @param handler
     * @param shared
     * @return The claimed resource
     * @throws ResourceNotFreeException
     */
    public IRQResource claimIRQ(ResourceOwner owner, int irq, IRQHandler handler, boolean shared)
        throws ResourceNotFreeException;

    /**
     * Create a MemoryResource wrapper around a given byte-array.
     *
     * @param data
     * @return The claimed resource
     */
    public MemoryResource asMemoryResource(byte[] data);

    /**
     * Create a MemoryResource wrapper around a given char-array.
     *
     * @param data
     * @return The claimed resource
     */
    public MemoryResource asMemoryResource(char[] data);

    /**
     * Create a MemoryResource wrapper around a given short-array.
     *
     * @param data
     * @return The claimed resource
     */
    public MemoryResource asMemoryResource(short[] data);

    /**
     * Create a MemoryResource wrapper around a given int-array.
     *
     * @param data
     * @return The claimed resource
     */
    public MemoryResource asMemoryResource(int[] data);

    /**
     * Create a MemoryResource wrapper around a given long-array.
     *
     * @param data
     * @return The claimed resource
     */
    public MemoryResource asMemoryResource(long[] data);

    /**
     * Create a MemoryResource wrapper around a given float-array.
     *
     * @param data
     * @return The claimed resource
     */
    public MemoryResource asMemoryResource(float[] data);

    /**
     * Create a MemoryResource wrapper around a given double-array.
     *
     * @param data
     * @return The claimed resource
     */
    public MemoryResource asMemoryResource(double[] data);

    /**
     * Gets the memory scanner.
     * This method will requires a ResourcePermission("memoryScanner").
     */
    public MemoryScanner getMemoryScanner();
}
