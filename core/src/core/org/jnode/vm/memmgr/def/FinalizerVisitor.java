/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.vm.memmgr.def;

import org.jnode.vm.ObjectVisitor;
import org.jnode.vm.VmMagic;
import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.classmgr.ObjectFlags;
import org.jnode.vm.classmgr.VmClassType;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.memmgr.HeapHelper;

@MagicPermission
final class FinalizerVisitor extends ObjectVisitor implements ObjectFlags {

    /**
     * My heap helper
     */
    private final HeapHelper helper;

    /**
     * The heap that is currently being visited
     */
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
        final int color = VmMagic.getObjectColor(object);
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
     * @param currentHeap The currentHeap to set.
     */
    public final void setCurrentHeap(VmAbstractHeap currentHeap) {
        this.currentHeap = currentHeap;
    }
}
