/*
 * $Id$
 */
package org.jnode.vm.memmgr.def;

import org.jnode.vm.ObjectVisitor;
import org.jnode.vm.VmMagic;
import org.jnode.vm.classmgr.ObjectFlags;
import org.jnode.vm.classmgr.VmClassType;
import org.jnode.vm.memmgr.HeapHelper;
import org.vmmagic.pragma.Uninterruptible;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class GCSweepVisitor extends ObjectVisitor implements ObjectFlags,
        Uninterruptible {

    private final HeapHelper helper;

    /** The heap that is currently being visited */
    private VmAbstractHeap currentHeap;

    public GCSweepVisitor(DefaultHeapManager heapMgr) {
        this.helper = heapMgr.getHelper();
    }

    /**
     * Mark every visited object white.
     * 
     * @param object
     * @return boolean
     */
    public final boolean visit(Object object) {
        final int gcColor = helper.getObjectColor(object);

        if (gcColor == GC_WHITE) {
            final boolean finalized = helper.isFinalized(object);
            if (finalized) {
                // Already finalized, we can free it now
                currentHeap.free(object);
            } else {
                final VmClassType vmClass = VmMagic.getObjectType(object);
                if (!vmClass.hasFinalizer()) {
                    // No finalizer, we can free it now
                    currentHeap.free(object);
                } else {
                    // Mark object for invoke of finalizer on other thread.
                    helper.atomicChangeObjectColor(object, gcColor, GC_YELLOW);
                }
            }
        } else if (gcColor != GC_YELLOW) {
            helper.atomicChangeObjectColor(object, gcColor, GC_WHITE);
        }
        return true;
    }

    /**
     * @return Returns the currentHeap.
     */
    public final VmAbstractHeap getCurrentHeap() {
        return this.currentHeap;
    }

    /**
     * @param currentHeap
     *            The currentHeap to set.
     */
    public final void setCurrentHeap(VmAbstractHeap currentHeap) {
        this.currentHeap = currentHeap;
    }
}
