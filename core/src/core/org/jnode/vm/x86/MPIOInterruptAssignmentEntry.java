/*
 * $Id$
 */
package org.jnode.vm.x86;

import org.jnode.system.MemoryResource;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class MPIOInterruptAssignmentEntry extends MPInterruptAssignmentEntry {

    /**
     * @param mem
     */
    MPIOInterruptAssignmentEntry(MemoryResource mem) {
        super(mem);
    }
    
    /**
     * @see org.jnode.vm.x86.MPEntry#getEntryTypeName()
     */
    public String getEntryTypeName() {
        return "I/O Int.Ass.";
    }
}