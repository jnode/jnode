package java.io;

import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.classmgr.VmMethod;

/**
 * @see java.io.ObjectStreamClass
 * @author Levente S\u00e1ntha
 */
class NativeObjectStreamClass {
    /**
     * @see java.io.ObjectStreamClass#initNative()
     */
    private static void initNative() {
        //empty
    }
    /**
     * @see java.io.ObjectStreamClass#hasStaticInitializer(java.lang.Class)
     */
    private static boolean hasStaticInitializer(Class clazz) {
        VmType vmt = VmType.fromClass(clazz);
        VmMethod met = vmt.getDeclaredMethod("<clinit>", "()V");
        return met != null && met.isStatic();
    }
}
