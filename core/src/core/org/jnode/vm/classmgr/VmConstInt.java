/*
 * $Id$
 */
package org.jnode.vm.classmgr;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VmConstInt extends VmConstObject {

    private int value;
    
    /**
     * Initialize this instance.
     * @param value
     */
    VmConstInt(int value) {
        this.value = value;
    }

    /**
     * @return Returns the value.
     */
    public final int intValue() {
        return value;
    }

    /**
     * @see org.jnode.vm.classmgr.VmConstObject#getConstType()
     */
    public final int getConstType() {
        return CONST_INT;
    }       
}
