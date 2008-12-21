package sun.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.VmReflection;

/**
 * @see sun.reflect.NativeConstructorAccessorImpl
 */
class NativeNativeConstructorAccessorImpl {
    /**
     * @see sun.reflect.NativeConstructorAccessorImpl#newInstance0(java.lang.reflect.Constructor, java.lang.Object[])
     */
    private static Object newInstance0(Constructor arg1, Object[] arg2) throws InstantiationException,
               IllegalArgumentException,
        InvocationTargetException{
        VmType vmt = VmType.fromClass(arg1.getDeclaringClass());
        VmMethod vmm = vmt.getDeclaredMethod(arg1.getSlot());
        try {
            return VmReflection.newInstance(vmm, arg2);
        } catch (IllegalAccessException iae)  { //todo| this should not happen, fix VmReflection.newInstance() to not
                                                //todo| throw this exception
            throw new InstantiationException("Unexpected IllegalAccessException");
        }
    }
}
