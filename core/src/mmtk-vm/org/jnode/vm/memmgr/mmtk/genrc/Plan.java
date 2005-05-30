/*
 * $Id$
 */
package org.jnode.vm.memmgr.mmtk.genrc;

import org.jnode.vm.Unsafe;
import org.jnode.vm.memmgr.HeapHelper;
import org.mmtk.plan.GenRC;
import org.vmmagic.pragma.InlinePragma;
import org.vmmagic.pragma.Uninterruptible;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class Plan extends GenRC implements Uninterruptible {

    /**
     * <code>true</code> if built with GCSpy
     */
    public static final boolean WITH_GCSPY = false;

    /** The heap helper */
    private final HeapHelper heapHelper;
    
    /**
     * Initialize this instance.
     * @param helper
     */
    public Plan(HeapHelper helper) {
        this.heapHelper = helper;
    }
    
    /**
     * Gets the plan instance associated with the current processor.
     * 
     * @return the plan instance for the current processor
     */
    public static Plan getInstance() 
    throws InlinePragma {
        return (Plan)Unsafe.getCurrentProcessor().getHeapData();
    }

    /**
     * @return Returns the heapHelper.
     */
    public final HeapHelper getHeapHelper() {
        return heapHelper;
    }    
}