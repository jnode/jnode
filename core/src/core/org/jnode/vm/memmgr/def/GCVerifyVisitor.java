/*
 * $Id$
 *
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
import org.jnode.vm.Unsafe;
import org.jnode.vm.VmArchitecture;
import org.jnode.vm.VmMagic;
import org.jnode.vm.annotation.Inline;
import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.classmgr.ObjectFlags;
import org.jnode.vm.classmgr.VmNormalClass;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.memmgr.HeapHelper;
import org.jnode.vm.scheduler.Monitor;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.ObjectReference;
import org.vmmagic.unboxed.Offset;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@MagicPermission
final class GCVerifyVisitor extends ObjectVisitor {

    private final HeapHelper helper;
    private final DefaultHeapManager heapManager;
    private final VmArchitecture arch;
    private int errorCount;

    public GCVerifyVisitor(DefaultHeapManager heapManager, VmArchitecture arch) {
        this.helper = heapManager.getHelper();
        this.heapManager = heapManager;
        this.arch = arch;
    }

    public final void reset() {
        errorCount = 0;
    }

    /**
     * @see org.jnode.vm.ObjectVisitor#visit(java.lang.Object)
     */
    public final boolean visit(Object object) {
        final int color = VmMagic.getObjectColor(object);
        if (color == ObjectFlags.GC_YELLOW) {
            // Ignore objects that need to be finalized.
            return true;
        }

        if (VmMagic.isFinalized(object)) {
            // Ignore finalized objects that need to be freed
            return true;
        }

        VmType vmClass;
        try {        
            vmClass = VmMagic.getObjectType(object);
        } catch (NullPointerException ex) {
            if (object == null) {
                helper.die("GCVerifyError: null object");
            } else if (VmMagic.getTIB(object) == null) {
                helper.die("GCVerifyError: null TIB");
            } else {
                helper.die("GCVerifyError: other NPE");
            }
            /* not reached */
            throw ex;
        }
        if (vmClass == null) {
            helper.die("GCVerifyError: vmClass");
        } else if (vmClass.isArray()) {
            if (!vmClass.isPrimitiveArray()) {
                verifyArray(object);
            }
        } else {
            verifyObject(object, (VmNormalClass) vmClass);
        }
        verifyChild(VmMagic.getTIB(object), object, "tib");
        final Monitor monitor = helper.getInflatedMonitor(object, arch);
        if (monitor != null) {
            verifyChild(monitor, object, "monitor");
        }
        return (errorCount == 0);
    }

    @Inline
    private final void verifyArray(Object object) {
        final Object[] arr = (Object[]) object;
        final int length = arr.length;
        for (int i = 0; i < length; i++) {
            final Object child = arr[i];
            if (child != null) {
                verifyChild(child, object, "Object[]");
            }
        }

    }

    @Inline
    private final void verifyObject(Object object, VmNormalClass<?> vmClass) {
        final int[] referenceOffsets = vmClass.getReferenceOffsets();
        final int cnt = referenceOffsets.length;
        final int size = vmClass.getObjectSize();
        final Address ptr = ObjectReference.fromObject(object).toAddress();
        for (int i = 0; i < cnt; i++) {
            int offset = referenceOffsets[i];
            if ((offset < 0) || (offset >= size)) {
                Unsafe.debug("reference offset out of range!");
                Unsafe.debug(vmClass.getName());
                helper.die("Class internal error");
            } else {
                final Object child = ptr.loadObjectReference(Offset.fromIntZeroExtend(offset)).toObject();
                if (child != null) {
                    verifyChild(child, object, "object child");
                }
            }
        }
    }

    @Inline
    private final void verifyChild(Object child, Object parent, String where) {
        if (child != null) {
            final ObjectReference childRef = ObjectReference.fromObject(child);
            if (!heapManager.isObject(childRef.toAddress())) {
                Unsafe.debug("GCVerifyError: in ");
                Unsafe.debug(where);
                Unsafe.debug(", parent type ");
                Unsafe.debug(VmMagic.getObjectType(parent).getName());
                Unsafe.debug(VmMagic.getObjectColor(parent));
                Unsafe.debug("; child (");
                Unsafe.debug(childRef.toAddress().toInt());
                Unsafe.debug(") is not an object ");
                Unsafe.debug(VmMagic.getObjectColor(childRef));
                errorCount++;
            }
        }
    }

    /**
     * @return Returns the errorCount.
     */
    public final int getErrorCount() {
        return this.errorCount;
    }
}
