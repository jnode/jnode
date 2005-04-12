/*
 * $Id$
 */
package org.jnode.vm.classmgr;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class VmConstDouble extends VmConstObject {

    private double value;
    
    /**
     * Initialize this instance.
     * @param value
     */
    public VmConstDouble(double value) {
        this.value = value;
    }

    /**
     * @return Returns the value.
     */
    public final double doubleValue() {
        return value;
    }

    /**
     * @see org.jnode.vm.classmgr.VmConstObject#getConstType()
     */
    public final int getConstType() {
        return CONST_DOUBLE;
    }       
}
