/*
 * $Id$
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
     * @return
     */
    public abstract Address getStart();
    
    /** 
     * Gets the size of this entry.
     * @return
     */
    public abstract Extent getSize();
    
    /**
     * Is this a memory region available to the OS.
     * @return
     */
    public abstract boolean isAvailable();
    
    /**
     * Is this a memory region containing reclaimable ACPI data.
     * @return
     */
    public abstract boolean isAcpi();
    
    /**
     * Is this a memory region containing ACPI NVS data.
     * @return
     */
    public abstract boolean isAcpiNVS();    
}
