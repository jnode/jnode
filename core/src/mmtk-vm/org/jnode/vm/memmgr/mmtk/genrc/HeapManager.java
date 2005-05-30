/*
 * $Id$
 */
package org.jnode.vm.memmgr.mmtk.genrc;

import org.jnode.vm.classmgr.VmClassLoader;
import org.jnode.vm.memmgr.GCStatistics;
import org.jnode.vm.memmgr.HeapHelper;
import org.jnode.vm.memmgr.mmtk.BaseMmtkHeapManager;
import org.jnode.vm.memmgr.mmtk.genrc.Plan;
import org.vmmagic.pragma.InlinePragma;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.ObjectReference;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class HeapManager extends BaseMmtkHeapManager {

    /** My statistics */
    private final GenRCStatistics statistics;

    /**
     * @param loader
     * @param helper
     */
    public HeapManager(VmClassLoader loader, HeapHelper helper) {
        super(loader, helper);
        this.statistics = new GenRCStatistics();
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
    protected final void bootPlan() throws InlinePragma {
        Plan.boot();
    }

    /**
     * @see org.jnode.vm.memmgr.mmtk.BaseMmtkHeapManager#alloc(int, int, int,
     *      int)
     */
    protected final Address alloc(int bytes, int align, int offset,
            int allocator) throws InlinePragma {
        return Plan.getInstance().alloc(bytes, align, offset, allocator);
    }

    /**
     * @see org.jnode.vm.memmgr.mmtk.BaseMmtkHeapManager#postAlloc(org.vmmagic.unboxed.ObjectReference,
     *      org.vmmagic.unboxed.ObjectReference, int, int)
     */
    protected final void postAlloc(ObjectReference object,
            ObjectReference typeRef, int bytes, int allocator)
            throws InlinePragma {
        Plan.getInstance().postAlloc(object, typeRef, bytes, allocator);
    }

    /**
     * @see org.jnode.vm.memmgr.mmtk.BaseMmtkHeapManager#checkAllocator(int,
     *      int, int)
     */
    protected final int checkAllocator(int bytes, int align, int allocator)
            throws InlinePragma {
        return Plan.checkAllocator(bytes, align, allocator);
    }
}
