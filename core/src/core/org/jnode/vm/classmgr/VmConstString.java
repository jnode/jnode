/*
 * $Id$
 */
package org.jnode.vm.classmgr;

import org.jnode.vm.VmSystemObject;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VmConstString extends VmSystemObject implements VmStaticsEntry {

    /** Index in the statics table of the string constant. */
    private final int staticsIndex;
    
    /**
     * Initialize this instance.
     * @param staticsIndex
     */
    public VmConstString(int staticsIndex) {
        this.staticsIndex = staticsIndex;
    }
    
    /**
     * Gets the index in the statics table of the string constants.
     * @return Returns the staticsIndex.
     */
    public final int getStaticsIndex() {
        return this.staticsIndex;
    }
}
