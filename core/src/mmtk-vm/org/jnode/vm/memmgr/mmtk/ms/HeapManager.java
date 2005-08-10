/*
 * $Id$
 */
package org.jnode.vm.memmgr.mmtk.ms;

import org.jnode.vm.annotation.Inline;
import org.jnode.vm.classmgr.VmClassLoader;
import org.jnode.vm.memmgr.GCStatistics;
import org.jnode.vm.memmgr.HeapHelper;
import org.jnode.vm.memmgr.mmtk.BaseMmtkHeapManager;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.ObjectReference;

/**
 * MMTk NoGC based heap manager.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class HeapManager extends BaseMmtkHeapManager {

    /** My statistics */
    private final MarkSweepStatistics statistics;

    /**
     * @param loader
     * @param helper
     */
    public HeapManager(VmClassLoader loader, HeapHelper helper) {
        super(loader, helper);
        this.statistics = new MarkSweepStatistics();
    }

    /**
     * @see org.jnode.vm.memmgr.VmHeapManager#getStatistics()
     */
    public final GCStatistics getStatistics() {
        return statistics;
    }

    /**
     * @see org.jnode.vm.memmgr.mmtk.BaseMmtkHeapManager#bootPlan()
     */
    @Inline
    protected final void bootPlan() {
        Plan.boot();
    }

    /**
     * @see org.jnode.vm.memmgr.mmtk.BaseMmtkHeapManager#alloc(int, int, int,
     *      int)
     */
    @Inline
    protected final Address alloc(int bytes, int align, int offset,
            int allocator) {
        return Plan.getInstance().alloc(bytes, align, offset, allocator);
    }

    /**
     * @see org.jnode.vm.memmgr.mmtk.BaseMmtkHeapManager#postAlloc(org.vmmagic.unboxed.ObjectReference,
     *      org.vmmagic.unboxed.ObjectReference, int, int)
     */
    @Inline
    protected final void postAlloc(ObjectReference object,
            ObjectReference typeRef, int bytes, int allocator) {
        Plan.getInstance().postAlloc(object, typeRef, bytes, allocator);
    }

    /**
     * @see org.jnode.vm.memmgr.mmtk.BaseMmtkHeapManager#checkAllocator(int,
     *      int, int)
     */
    @Inline
    protected final int checkAllocator(int bytes, int align, int allocator) {
        return Plan.checkAllocator(bytes, align, allocator);
    }
}
