/*
 * $Id$
 */
package org.jnode.vm.classmgr;

/**
 * Implemented by objects that are on the isolated statics table.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface VmIsolatedStaticsEntry {

    /**
     * Gets the index in the isolated statics table of this entry.
     * @return The (array) index of this entry in the isolated statics table. 
     */
    public int getIsolatedStaticsIndex();
}
