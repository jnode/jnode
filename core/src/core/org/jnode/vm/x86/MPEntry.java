/*
 * $Id$
 */
package org.jnode.vm.x86;

import org.jnode.system.MemoryResource;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class MPEntry {

    protected final MemoryResource mem;

    public MPEntry(MemoryResource mem) {
        this.mem = mem;
    }
    
    public int getEntryType() {
        return mem.getByte(0) & 0xFF;
    }
    
    public abstract String getEntryTypeName();
    
    public String toString() {
        return getEntryTypeName();
    }

}