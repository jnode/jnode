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
 
package org.jnode.vm;

import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;

/**
 * Class used to describe memory map elements.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class MemoryMapEntry {

    /**
     * Gets the start of this entry.
     *
     * @return the entry's start address.
     */
    public abstract Address getStart();

    /**
     * Gets the size of this entry.
     *
     * @return the entries size
     */
    public abstract Extent getSize();

    /**
     * Test if a memory region is available to the OS.
     *
     * @return {@code true} if the region is available, otherwise {@code false}.
     */
    public abstract boolean isAvailable();

    /**
     * Is this a memory region containing reclaimable ACPI data.
     *
     * @return {@code true} if the region contains reclaimable ACPI data, otherwise {@code false}.
     */
    public abstract boolean isAcpi();

    /**
     * Is this a memory region containing ACPI NVS data.
     *
     * @return {@code true} if the region contains ACPI NVS data, otherwise {@code false}.
     */
    public abstract boolean isAcpiNVS();
}
