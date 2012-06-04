/*
 * $Id$
 *
 * Copyright (C) 2003-2012 JNode.org
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
 
package org.jnode.vm.x86;

import org.jnode.vm.facade.MemoryMapEntry;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class X86MemoryMapEntry implements MemoryMapEntry {

    private final Address start;
    private final Extent size;
    private final int type;

    /**
     * @param start
     * @param size
     * @param type
     */
    X86MemoryMapEntry(Address start, Extent size, int type) {
        this.start = start;
        this.size = size;
        this.type = type;
    }

    /**
     * @see org.jnode.vm.facade.MemoryMapEntry#getSize()
     */
    public Extent getSize() {
        return size;
    }

    /**
     * @see org.jnode.vm.facade.MemoryMapEntry#getStart()
     */
    public Address getStart() {
        return start;
    }

    /**
     * @see org.jnode.vm.facade.MemoryMapEntry#isAcpi()
     */
    public boolean isAcpi() {
        return (type == VmX86Architecture.MMAP_TYPE_ACPI);
    }

    /**
     * @see org.jnode.vm.facade.MemoryMapEntry#isAcpiNVS()
     */
    public boolean isAcpiNVS() {
        return (type == VmX86Architecture.MMAP_TYPE_NVS);
    }

    /**
     * @see org.jnode.vm.facade.MemoryMapEntry#isAvailable()
     */
    public boolean isAvailable() {
        return (type == VmX86Architecture.MMAP_TYPE_MEMORY);
    }
}
