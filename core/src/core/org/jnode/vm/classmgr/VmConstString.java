/*
 * $Id$
 */
package org.jnode.vm.classmgr;



/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VmConstString extends VmConstObject implements VmStaticsEntry {

    /** Index in the statics table of the string constant. */
    private final int staticsIndex;
    
    /**
     * Initialize this instance.
     * @param staticsIndex
     */
    public VmConstString(VmCP cp, int staticsIndex) {
        super(cp);
        this.staticsIndex = staticsIndex;
    }
    
    /**
     * Gets the index in the statics table of the string constants.
     * @return Returns the staticsIndex.
     */
    public final int getStaticsIndex() {
        return this.staticsIndex;
    }
    /**
     * @see org.jnode.vm.classmgr.VmConstObject#doResolve(org.jnode.vm.classmgr.VmClassLoader)
     */
    protected void doResolve(VmClassLoader clc) {
        // Nothing to do
    }
}
