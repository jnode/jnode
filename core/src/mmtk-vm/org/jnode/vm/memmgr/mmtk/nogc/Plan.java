/*
 * $Id$
 */
package org.jnode.vm.memmgr.mmtk.nogc;

import org.jnode.vm.VmProcessor;
import org.jnode.vm.annotation.Inline;
import org.jnode.vm.memmgr.HeapHelper;
import org.mmtk.plan.NoGC;
import org.vmmagic.pragma.Uninterruptible;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class Plan extends NoGC implements Uninterruptible {

    /**
     * <code>true</code> if built with GCSpy
     */
    public static final boolean WITH_GCSPY = false;

    /** The heap helper */
    private final HeapHelper heapHelper;

    /**
     * Initialize this instance.
     * 
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
    @Inline
    public static Plan getInstance() {
        return (Plan) VmProcessor.current().getHeapData();
    }

    /**
     * @return Returns the heapHelper.
     */
    @Inline
    public final HeapHelper getHeapHelper() {
        return heapHelper;
    }
}
