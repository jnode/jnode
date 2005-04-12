/*
 * $Id$
 */
package org.jnode.vm.classmgr;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class VmConstLong extends VmConstObject {

    private long value;
    
    /**
     * Initialize this instance.
     * @param value
     */
    public VmConstLong(long value) {
        this.value = value;
    }

    /**
     * @return Returns the value.
     */
    public final long longValue() {
        return value;
    }

    /**
     * @see org.jnode.vm.classmgr.VmConstObject#getConstType()
     */
    public final int getConstType() {
        return CONST_LONG;
    }       

}
