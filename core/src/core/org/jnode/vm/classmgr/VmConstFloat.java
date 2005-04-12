/*
 * $Id$
 */
package org.jnode.vm.classmgr;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class VmConstFloat extends VmConstObject {

    private float value;
    
    /**
     * Initialize this instance.
     * @param value
     */
    VmConstFloat(float value) {
        this.value = value;
    }

    /**
     * @return Returns the value.
     */
    public final float floatValue() {
        return value;
    }

    /**
     * @see org.jnode.vm.classmgr.VmConstObject#getConstType()
     */
    public final int getConstType() {
        return CONST_FLOAT;
    }       

}
