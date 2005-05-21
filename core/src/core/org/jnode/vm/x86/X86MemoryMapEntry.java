/*
 * $Id$
 */
package org.jnode.vm.x86;

import org.jnode.vm.MemoryMapEntry;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class X86MemoryMapEntry extends MemoryMapEntry {

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
     * @see org.jnode.vm.MemoryMapEntry#getSize()
     */
    public Extent getSize() {
        return size;
    }

    /**
     * @see org.jnode.vm.MemoryMapEntry#getStart()
     */
    public Address getStart() {
        return start;
    }

    /**
     * @see org.jnode.vm.MemoryMapEntry#isAcpi()
     */
    public boolean isAcpi() {
        return (type == VmX86Architecture.MMAP_TYPE_ACPI);
    }

    /**
     * @see org.jnode.vm.MemoryMapEntry#isAcpiNVS()
     */
    public boolean isAcpiNVS() {
        return (type == VmX86Architecture.MMAP_TYPE_NVS);
    }

    /**
     * @see org.jnode.vm.MemoryMapEntry#isAvailable()
     */
    public boolean isAvailable() {
        return (type == VmX86Architecture.MMAP_TYPE_MEMORY);
    }
}
