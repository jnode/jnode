/*
 * $Id$
 */
package org.jnode.vm.memmgr.def;

import org.jnode.vm.PragmaUninterruptible;
import org.jnode.vm.classmgr.ObjectFlags;
import org.jnode.vm.memmgr.HeapHelper;
import org.jnode.vm.memmgr.VmWriteBarrier;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class DefaultWriteBarrier extends VmWriteBarrier {

    /** The heap helper */
    private final HeapHelper helper;

    /** Is the write barrier active? */
    private boolean active;
    
    /** Are there any object colors changed by the write barrier, since the last reset */
    private boolean changed;

    private int arrayCopyCount;

    private int arrayStoreCount;

    private int putFieldCount;

    private int putStaticCount;

    /**
     * Initialize this instance.
     */
    public DefaultWriteBarrier(HeapHelper helper) {
        this.helper = helper;
    }

    /**
     * @see org.jnode.vm.memmgr.VmWriteBarrier#arrayCopyWriteBarrier(java.lang.Object[],
     *      int, int)
     */
    public final void arrayCopyWriteBarrier(Object array, int start, int end)
            throws PragmaUninterruptible {
        // The source array is already reachable, so by definition, all
        // entries will be reachable.
        // So we do nothing here
        arrayCopyCount++;
    }

    /**
     * @see org.jnode.vm.memmgr.VmWriteBarrier#arrayStoreWriteBarrier(java.lang.Object,
     *      int, java.lang.Object)
     */
    public final void arrayStoreWriteBarrier(Object ref, int index, Object value)
            throws PragmaUninterruptible {
        if (active) {
            shade(value);
        }
        arrayStoreCount++;
    }

    /**
     * @see org.jnode.vm.memmgr.VmWriteBarrier#putfieldWriteBarrier(java.lang.Object,
     *      int, java.lang.Object)
     */
    public final void putfieldWriteBarrier(Object ref, int offset, Object value)
            throws PragmaUninterruptible {
        if (active) {
            shade(value);
        }
        putFieldCount++;
    }

    /**
     * @see org.jnode.vm.memmgr.VmWriteBarrier#putstaticWriteBarrier(int,
     *      java.lang.Object)
     */
    public final void putstaticWriteBarrier(int staticsIndex, Object value)
            throws PragmaUninterruptible {
        if (active) {
            shade(value);
        }
        putStaticCount++;
    }

    /**
     * Set the GC color of the given value to gray if it is white.
     * 
     * @param value
     */
    private final void shade(Object value) throws PragmaUninterruptible {
        if (value != null) {
            while (true) {
                final int gcColor = helper.getObjectColor(value);
                if (gcColor != ObjectFlags.GC_WHITE) { 
                // Not white, we're done
                return; }
                if (helper.atomicChangeObjectColor(value, gcColor, ObjectFlags.GC_GREY)) {
                    // Change to grey, we're done
                    changed = true;
                    return;
                }
            }
        }
    }

    public String toString() {
        return "arrayCopy: " + arrayCopyCount + ", arrayStore: "
                + arrayStoreCount + ", putField: " + putFieldCount
                + ", putStatic: " + putStaticCount;
    }

    /**
     * @param active
     *            The active to set.
     */
    final void setActive(boolean active) {
        this.changed = false;
        this.active = active;
    }
    
    /**
     * @return Returns the changed.
     */
    final boolean isChanged() {
        return this.changed;
    }

    /**
     * Reset the changed attribute to false.
     */
    final void resetChanged() {
        this.changed = false;
    }
}
