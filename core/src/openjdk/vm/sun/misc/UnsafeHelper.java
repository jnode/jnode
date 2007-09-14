/*
 * $Id$
 */
package sun.misc;

import java.lang.reflect.Field;
import org.jnode.vm.classmgr.VmInstanceField;
import org.vmmagic.unboxed.ObjectReference;

/**
 * @author Levente S\u00e1ntha
 */
class UnsafeHelper {
    private static final int OFFSET_OF_vmField_IN_Field =
            ((VmInstanceField) Field.class.getVmClass().getField("vmField")).getOffset();
    private static final int OFFSET_OF_offset_IN_VmInstanceField =
            ((VmInstanceField) VmInstanceField.class.getVmClass().getField("offset")).getOffset();

    static long objectFieldOffset(Field f) {
        return ObjectReference.fromObject(f).toAddress().
                add(OFFSET_OF_vmField_IN_Field).loadAddress().
                add(OFFSET_OF_offset_IN_VmInstanceField).loadInt();
    }
}
