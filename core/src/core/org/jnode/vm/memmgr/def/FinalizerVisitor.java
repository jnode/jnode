/*
 * $Id$
 */
package org.jnode.vm.memmgr.def;

import org.jnode.vm.ObjectVisitor;
import org.jnode.vm.VmMagic;
import org.jnode.vm.classmgr.ObjectFlags;
import org.jnode.vm.classmgr.VmClassType;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.memmgr.HeapHelper;

final class FinalizerVisitor extends ObjectVisitor implements ObjectFlags {

    /** My heap helper */
    private final HeapHelper helper;

    /** The heap that is currently being visited */
    private VmAbstractHeap currentHeap;

    /**
     * Initialize this instance.
     * 
     * @param helper
     */
    public FinalizerVisitor(HeapHelper helper) {
        this.helper = helper;
    }

    /**
     * @see org.jnode.vm.ObjectVisitor#visit(java.lang.Object)
     */
    public boolean visit(Object object) {
        final int color = helper.getObjectColor(object);
        if (color == GC_YELLOW) {
            final VmClassType type = VmMagic.getObjectType(object);
            final VmMethod fm = type.getFinalizeMethod();
            if (fm != null) {
                try {
                    helper.invokeFinalizer(fm, object);
                } catch (Throwable ex) {
                    // Ignore error in finalize
                }
            }
            helper.setFinalized(object);
            helper.atomicChangeObjectColor(object, color, GC_WHITE);
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