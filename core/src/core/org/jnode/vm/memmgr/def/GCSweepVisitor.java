/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
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
    private VmDefaultHeap currentHeap;

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
        final int gcColor = VmMagic.getObjectColor(object);

        if (gcColor == GC_WHITE) {
            final boolean finalized = VmMagic.isFinalized(object);
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
    public final VmDefaultHeap getCurrentHeap() {
        return this.currentHeap;
    }

    /**
     * @param currentHeap
     *            The currentHeap to set.
     */
    public final void setCurrentHeap(VmDefaultHeap currentHeap) {
        this.currentHeap = currentHeap;
    }
}
