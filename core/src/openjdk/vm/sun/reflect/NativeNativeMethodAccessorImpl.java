package sun.reflect;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.VmReflection;

/**
 * @see sun.reflect.NativeMethodAccessorImpl
 */
class NativeNativeMethodAccessorImpl {
    /**
     * @see sun.reflect.NativeMethodAccessorImpl#invoke0(java.lang.reflect.Method, java.lang.Object, java.lang.Object[])
     */
    private static Object invoke0(Method arg1, Object arg2, Object[] arg3) throws IllegalArgumentException,
        InvocationTargetException {
        VmType vmt = VmType.fromClass((Class<?>) arg1.getDeclaringClass());
        VmMethod vmm = vmt.getDeclaredMethod(arg1.getSlot());
        return VmReflection.invoke(vmm, arg2, arg3);
    }
}
