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
import org.jnode.vm.annotation.NoInline;
import org.jnode.vm.classmgr.ObjectFlags;
import org.jnode.vm.classmgr.VmNormalClass;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.memmgr.HeapHelper;
import org.jnode.vm.scheduler.Monitor;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.ObjectReference;
import org.vmmagic.unboxed.Offset;
import org.vmmagic.unboxed.Word;

/**
 * @author epr
 */
@MagicPermission
final class GCMarkVisitor extends ObjectVisitor implements ObjectFlags,
    Uninterruptible {

    /**
     * The marking stack
     */
    private final GCStack stack;

    /**
     * The number of marked objects.
     */
    private int markedObjects;

    /**
     * If true, all white and grey objects will be marked, otherwise only the
     * grey objects will be marked
     */
    private boolean rootSet;

    private final VmArchitecture arch;

//    private final int slotSize;

    private final DefaultHeapManager heapManager;

    private final HeapHelper helper;

//    private final ProcessChildVisitor processChildVisitor = new ProcessChildVisitor();

    /**
     * Create a new instance
     *
     * @param stack
     */
    public GCMarkVisitor(DefaultHeapManager heapManager, VmArchitecture arch,
                         GCStack stack) {
        this.heapManager = heapManager;
        this.stack = stack;
        this.markedObjects = 0;
        this.rootSet = false;
        this.arch = arch;
        this.helper = heapManager.getHelper();
//        this.slotSize = arch.getReferenceSize();
    }

    /**
     * @param object
     * @return boolean
     * @see org.jnode.vm.ObjectVisitor#visit(java.lang.Object)
     */
    public boolean visit(Object object) {

        // Check the current color first, since a stackoverflow of
        // the mark stack results in another iteration of visits.
        final int gcColor = VmMagic.getObjectColor(object);
        if (gcColor == GC_BLACK) {
            return true;
        } else if (rootSet || (gcColor == GC_GREY)) {
            switch (gcColor) {
                case GC_YELLOW:

                    //Avoid telling such stories in a garbage collector!!! 
                    //Unsafe.debug("Yellow Object in the rootset.\n");
                    //Unsafe.debug("Perhaps corrupted Heap or bad luck.\n");
                    //Unsafe.debug("Continue as long as we don't have a");
                    //Unsafe.debug("gc map for the stack.\n Panic JNode once");
                    //Unsafe.debug("we have an exact GC.\n");

                    //Unsafe.die("Corrupted Heap\n");
                    return true;
                case GC_WHITE: {
                    final boolean ok;
                    ok = helper.atomicChangeObjectColor(object, gcColor,
                        GC_GREY);
                    if (!ok) {
                        Unsafe.debug("Could not change object color. ");
                    }
                    break;
                }                
                case GC_GREY:
                    break;
                default: {
                    Unsafe.debug("color");
                    Unsafe.debug(gcColor);
                    helper.die("Unknown GC color on object");
                }
            }
            stack.push(object);
            mark();
        }

        final boolean rc = (!stack.isOverflow());
        return rc;
    }

    /**
     * Reset this visitor to its original state.
     */
    @Inline
    public void reset() {
        this.markedObjects = 0;
    }

    /**
     * Process all objects on the markstack, until the markstack is empty.
     */
    @NoInline
    protected final void mark() {
        while (!stack.isEmpty()) {
            final Object object = stack.pop();
            markedObjects++;
            VmType vmClass;
            try {
                vmClass = VmMagic.getObjectType(object);
            } catch (NullPointerException ex) {
                // This is a symptom of heap corruption
                if (object == null) {
                    helper.die("GCMarkError: null object");
                } else {
                    final Address objAddr = ObjectReference.fromObject(object).toAddress();
                    Unsafe.debug("Object address is ");
                    Unsafe.debug(objAddr);
                    Unsafe.debug(", tib is ");
                    Object[] tib = VmMagic.getTIB(object);
                    Unsafe.debug(ObjectReference.fromObject(tib).toAddress());
                    Unsafe.debug(", flags word is ");
                    Word flags = VmMagic.getObjectFlags(object);
                    Unsafe.debug(flags);
                    Unsafe.debug(", markedObjects is ");
                    Unsafe.debug(markedObjects);
                    Unsafe.debug('\n');
                    helper.die("GCMarkError: NPE");
                }
                throw ex;
            }
            if (vmClass == null) {
                Unsafe.debug("Oops vmClass == null in (");
                Unsafe.debug(markedObjects);
                Unsafe.debug(")");
                helper.die("vmClass == null in mark()");
            } else if (vmClass.isArray()) {
                if (!vmClass.isPrimitiveArray()) {
                    markArray(object);
                }
            } else {
                markObject(object, (VmNormalClass) vmClass);
            }
            processChild(VmMagic.getTIB(object));
            final Monitor monitor = helper.getInflatedMonitor(object, arch);
            if (monitor != null) {
                processChild(monitor);
            }
            final int gcColor = VmMagic.getObjectColor(object);
            helper.atomicChangeObjectColor(object, gcColor, GC_BLACK);
        }
    }

    /**
     * Mark all elements in the given array. The array must contain references
     * only.
     *
     * @param object
     */
    @Inline
    private void markArray(Object object) {
        try {
            final Object[] arr = (Object[]) object;
            final int length = arr.length;
            for (int i = 0; i < length; i++) {
                final Object child = arr[i];
                if (child != null) {
                    // Enable the following in the case of heap corruption
                    if (true) {
                        verifyChild(child, object, "array child", i, i);
                    }
                    
                    processChild(child);
                }
            }
        } catch (ClassCastException ex) {
            System.out.println("object.class=" + object.getClass().getName());
            throw ex;
        }
    }

    /**
     * Mark all instance variables of the given object.
     *
     * @param object
     * @param vmClass
     */
    @Inline
    private void markObject(Object object, VmNormalClass vmClass) {
        final int[] referenceOffsets = vmClass.getReferenceOffsets();
        final int cnt = referenceOffsets.length;
        if (cnt == 0) {
            return;
        }

        final int size = vmClass.getObjectSize();
        final Address objAddr = ObjectReference.fromObject(object).toAddress();

        for (int i = 0; i < cnt; i++) {
            final int offset = referenceOffsets[i];
            if ((offset < 0) || (offset >= size)) {
                Unsafe.debug("reference offset out of range!");
                Unsafe.debug(vmClass.getName());
                helper.die("Class internal error");
            } else {
                final ObjectReference child = objAddr.loadObjectReference(Offset.fromIntZeroExtend(offset));
                if (child != null) {
                    // Enable the following in the case of heap corruption
                    if (false) {
                        verifyChild(child, object, "object child", i, offset);
                    }
                    
                    processChild(child);
                }
            }
        }
    }

    @Inline
    private final void verifyChild(Object child, Object parent, String where, int i, int offset) {
        if (child != null) {
            final ObjectReference childRef = ObjectReference.fromObject(child);
            if (!heapManager.isObject(childRef.toAddress())) {
                Unsafe.debug("GCMarkError: in ");
                Unsafe.debug(where);
                Unsafe.debug(", i ");
                Unsafe.debug(i);
                Unsafe.debug(", offset ");
                Unsafe.debug(offset);
                Unsafe.debug(", parent type ");
                Unsafe.debug(VmMagic.getObjectType(parent).getName());
                Unsafe.debug(VmMagic.getObjectColor(parent));
                Unsafe.debug("; child (");
                Unsafe.debug(childRef.toAddress().toInt());
                Unsafe.debug(") is not an object ");
                Unsafe.debug(VmMagic.getObjectColor(childRef));
                helper.die("Corrupted heap");
            }
        }
    }

    /**
     * Process a child of an object (this child is a reference).
     *
     * @param child
     */
    @Inline
    final void processChild(Object child) {
        final int gcColor = VmMagic.getObjectColor(child);
        if (gcColor <= GC_WHITE) {
            // Yellow or White
            helper.atomicChangeObjectColor(child, gcColor, GC_GREY);
            try {
                // TEST for a valid vmclass.
                stack.push(child);
            } catch (NullPointerException ex) {
                Unsafe.debug("\nObject address ");
                Unsafe.debug(ObjectReference.fromObject(child).toAddress().toInt());
                Unsafe.debug("\nObject TIB ");
                Unsafe.debug(ObjectReference.fromObject(VmMagic.getTIB(child)).toAddress().toInt());
                helper.die("NPE in processChild; probably corrupted heap");
            }
        }
    }

    /**
     * Gets the number of objects marked by this visitor.
     *
     * @return int
     */
    @Inline
    public int getMarkedObjects() {
        return markedObjects;
    }

    /**
     * Gets the rootSet attribute.
     *
     * @return boolean
     */
    @Inline
    public boolean isRootSet() {
        return rootSet;
    }

    /**
     * Sets the rootSet attribute.
     *
     * @param b If true, all white and grey objects will be marked, otherwise
     *          only the grey objects will be marked.
     */
    @Inline
    public void setRootSet(boolean b) {
        rootSet = b;
    }
}
