/*
 * $Id$
 */
package org.jnode.vm.x86;

import org.vmmagic.pragma.Uninterruptible;

/**
 * Wrapper around a Model Specific Register.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class MSR implements Uninterruptible {

    /** Number of the MSR */
    private final int id;
    
    /** Last read value of the MSR */
    private long value;
    
    public MSR(int id) {
        this(id, 0);
    }

    public MSR(int id, long value) {
        this.id = id;
        this.value = value;
    }

    /**
     * Gets the last known value of this MSR.
     * @return Returns the value.
     */
    public final long getValue() {
        return value;
    }

    /**
     * Sets the value of this MSR.
     */
    final void setValue(long value) {
        this.value = value;
    }
}
