/*
 * $Id$
 */
package org.jnode.vm.memmgr.def;

import org.jnode.vm.Monitor;
import org.jnode.vm.ObjectVisitor;
import org.jnode.vm.Unsafe;
import org.jnode.vm.VmArchitecture;
import org.jnode.vm.VmThread;
import org.jnode.vm.classmgr.ObjectFlags;
import org.jnode.vm.classmgr.VmArrayClass;
import org.jnode.vm.classmgr.VmNormalClass;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.memmgr.HeapHelper;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.ObjectReference;
import org.vmmagic.unboxed.Offset;

/**
 * @author epr
 */
final class GCMarkVisitor extends ObjectVisitor implements ObjectFlags,
        Uninterruptible {

    /** The marking stack */
    private final GCStack stack;

    /** The number of marked objects. */
    private int markedObjects;

    /**
     * If true, all white and grey objects will be marked, otherwise only the
     * grey objects will be marked
     */
    private boolean rootSet;

    private final VmArchitecture arch;

    private final int slotSize;

    private final DefaultHeapManager heapManager;

    private final HeapHelper helper;
    
    private final ProcessChildVisitor processChildVisitor = new ProcessChildVisitor();

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
        this.slotSize = arch.getReferenceSize();
    }

    /**
     * @param object
     * @see org.jnode.vm.ObjectVisitor#visit(java.lang.Object)
     * @return boolean
     */
    public boolean visit(Object object) {

        // Be very paranoia for now
        /*
         * if (!heapManager.isObject(helper.addressOf(object))) {
         * Unsafe.debug("visit got non-object");
         * Unsafe.debug(helper.addressToLong(helper.addressOf(object)));
         * Unsafe.getCurrentProcessor().getArchitecture().getStackReader()
         * .debugStackTrace(); helper.die("Internal error"); return false;
         */
        try {
            // TEST for a valid vmclass.
            helper.getVmClass(object);
        } catch (NullPointerException ex) {
            Unsafe.debug("\nObject address ");
            Unsafe.debug(helper.addressOf32(object));
            Unsafe.debug("\nObject TIB ");
            Unsafe.debug(helper.addressOf32(helper.getTib(object)));
            helper.die("NPE in processChild; probably corrupted heap");
        }

        //testObject(object, Unsafe.getVmClass(object));
        // Check the current color first, since a stackoverflow of
        // the mark stack results in another iteration of visits.
        final int gcColor = helper.getObjectColor(object);
        if (gcColor == GC_BLACK) {
            return true;
        } else if (rootSet || (gcColor == GC_GREY)) {
            switch (gcColor) {
            case GC_WHITE:
            case GC_YELLOW:
                {
                    final boolean ok;
                    ok = helper.atomicChangeObjectColor(object, gcColor,
                            GC_GREY);
                    if (!ok) {
                        Unsafe.debug("Could not change object color. ");
                    }
                }
                break;
            case GC_GREY:
                break;
            default:
                {
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
    public void reset() {
        this.markedObjects = 0;
    }

    /**
     * Process all objects on the markstack, until the markstack is empty.
     */
    protected void mark() {
        while (!stack.isEmpty()) {
            final Object object = stack.pop();
            markedObjects++;
            final VmType vmClass = helper.getVmClass(object);
            if (vmClass == null) {
                Unsafe.debug("Oops vmClass == null in (");
                Unsafe.debug(markedObjects);
                Unsafe.debug(")");
                helper.die("vmClass == null in mark()");
            } else if (vmClass.isArray()) {
                if (!((VmArrayClass) vmClass).isPrimitiveArray()) {
                    markArray(object);
                }
            } else {
                markObject(object, (VmNormalClass) vmClass);
                if (object instanceof VmThread) {
                    try {
                        markThreadStack((VmThread) object);
                    } catch (ClassCastException ex) {
                        Unsafe.debug("VmThread");
                        Unsafe.debug(object.getClass().getName());
                        helper.die("GCMarkVisitor.mark");
                    }
                }
            }
            processChild(helper.getTib(object));
            final Monitor monitor = helper.getInflatedMonitor(object, arch);
            if (monitor != null) {
                processChild(monitor);
            }
            final int gcColor = helper.getObjectColor(object);
            helper.atomicChangeObjectColor(object, gcColor, GC_BLACK);
        }
    }

    /**
     * Mark all elements in the given array. The array must contain references
     * only.
     * 
     * @param object
     */
    private void markArray(Object object) {
        try {
            final Object[] arr = (Object[]) object;
            final int length = arr.length;
            for (int i = 0; i < length; i++) {
                final Object child = arr[ i];
                if (child != null) {
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
    private void markObject(Object object, VmNormalClass vmClass) {
        final int[] referenceOffsets = vmClass.getReferenceOffsets();
        final int cnt = referenceOffsets.length;
        if (cnt == 0) {
            return;
        }

        final int size = vmClass.getObjectSize();
        final Address objAddr = ObjectReference.fromObject(object).toAddress();
        
        for (int i = 0; i < cnt; i++) {
            final int offset = referenceOffsets[ i];
            if ((offset < 0) || (offset >= size)) {
                Unsafe.debug("reference offset out of range!");
                Unsafe.debug(vmClass.getName());
                helper.die("Class internal error");
            } else {
                final Object child = objAddr.loadObjectReference(Offset.fromIntZeroExtend(offset));
                //final Object child = helper.getObject(object, offset);
                if (child != null) {
                    try {
                        // TEST for a valid vmclass.
                        helper.getVmClass(child);
                    } catch (NullPointerException ex) {
                        Unsafe.debug("\nObject type  ");
                        Unsafe.debug(vmClass.getName());
                        Unsafe.debug("\nChild addr   ");
                        Unsafe.debug(helper.addressOf32(child));
                        Unsafe.debug("\nField offset ");
                        Unsafe.debug(offset);
                        Unsafe.debug("\nC.IsObject?  ");
                        Unsafe.debug(heapManager.isObject(helper.addressOf(child)) ? "Yes" : "No");
                        Unsafe.debug("\nO.IsObject?  ");
                        Unsafe.debug(heapManager.isObject(helper.addressOf(object)) ? "Yes" : "No");
                        Unsafe.debug('\n');
                        helper.die("NPE in processChild; probably corrupted heap");
                    }
                    processChild(child);
                }
            }
        }
    }

    /**
     * Mark all objects on the stack of the given thread
     * 
     * @param thread
     */
    private void markThreadStack(VmThread thread) {
        // For now do it stupid, but safe, just scan the whole stack.
//        final int stackSize = thread.getStackSize();
//        final Object stack = helper.getStack(thread);
//        if (stack != null) {
//            for (int i = 0; i < stackSize; i += slotSize) {
//                final Address child = helper.getAddress(stack, i);
//                if (child != null) {
//                    if (heapManager.isObject(child)) {
//                        processChild(child);
//                    }
//                }
//            }
//        }
        thread.visit(processChildVisitor, heapManager, helper);
    }

    /**
     * Process a child of an object (this child is a reference).
     * 
     * @param child
     */
    final void processChild(Object child) {
        final int gcColor = helper.getObjectColor(child);
        if (gcColor <= GC_WHITE) {
            // Yellow or White
            helper.atomicChangeObjectColor(child, gcColor, GC_GREY);
            try {
                // TEST for a valid vmclass.
                helper.getVmClass(child);
                stack.push(child);
            } catch (NullPointerException ex) {
                Unsafe.debug("\nObject address ");
                Unsafe.debug(helper.addressOf32(child));
                Unsafe.debug("\nObject TIB ");
                Unsafe.debug(helper.addressOf32(helper.getTib(child)));
                helper.die("NPE in processChild; probably corrupted heap");
            }
        }
    }

    /**
     * Gets the number of objects marked by this visitor.
     * 
     * @return int
     */
    public int getMarkedObjects() {
        return markedObjects;
    }

    /**
     * Gets the rootSet attribute.
     * 
     * @return boolean
     */
    public boolean isRootSet() {
        return rootSet;
    }

    /**
     * Sets the rootSet attribute.
     * 
     * @param b
     *            If true, all white and grey objects will be marked, otherwise
     *            only the grey objects will be marked.
     */
    public void setRootSet(boolean b) {
        rootSet = b;
    }
    
    private class ProcessChildVisitor extends ObjectVisitor implements Uninterruptible {
        
            public boolean visit(Object object) {
                processChild(object);
                return true;
            }
    }
}
