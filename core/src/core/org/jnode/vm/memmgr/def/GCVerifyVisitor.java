/*
 * $Id$
 */
package org.jnode.vm.memmgr.def;

import org.jnode.vm.Monitor;
import org.jnode.vm.ObjectVisitor;
import org.jnode.vm.Unsafe;
import org.jnode.vm.VmArchitecture;
import org.jnode.vm.VmMagic;
import org.jnode.vm.classmgr.VmArrayClass;
import org.jnode.vm.classmgr.VmNormalClass;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.memmgr.HeapHelper;
import org.vmmagic.unboxed.ObjectReference;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
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
    
    public void reset() {
        errorCount = 0;
    }
    
    /**
     * @see org.jnode.vm.ObjectVisitor#visit(java.lang.Object)
     */
    public boolean visit(Object object) {
        final VmType vmClass = VmMagic.getObjectType(object);
        if (vmClass == null) {
            helper.die("GCVerifyError: vmClass");
        } else if (vmClass.isArray()) {
            if (!((VmArrayClass) vmClass).isPrimitiveArray()) {
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
        return true;
    }
    
    private void verifyArray(Object object) {
        final Object[] arr = (Object[]) object;
        final int length = arr.length;
        for (int i = 0; i < length; i++) {
            final Object child = arr[i];
            if (child != null) {
                verifyChild(child, object, "Object[]");
            }
        }
    
    }
    
    private void verifyObject(Object object, VmNormalClass vmClass) {
        final int[] referenceOffsets = vmClass.getReferenceOffsets();
        final int cnt = referenceOffsets.length;
        final int size = vmClass.getObjectSize();
        for (int i = 0; i < cnt; i++) {
            int offset = referenceOffsets[ i];
            if ((offset < 0) || (offset >= size)) {
                Unsafe.debug("reference offset out of range!");
                Unsafe.debug(vmClass.getName());
                helper.die("Class internal error");
            } else {
                final Object child = helper.getObject(object, offset);
                if (child != null) {
                    verifyChild(child, object, "object child");
                }
            }
        }
    }
    
    private void verifyChild(Object child, Object parent, String where) {
        if (child != null) {
            final ObjectReference childRef = ObjectReference.fromObject(child);
            if (!heapManager.isObject(childRef.toAddress())) {
                Unsafe.debug("GCVerifyError: in ");
                Unsafe.debug(where);
                Unsafe.debug(", parent type ");
                Unsafe.debug(VmMagic.getObjectType(parent).getName());
                Unsafe.debug("; child is not an object. ");
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
