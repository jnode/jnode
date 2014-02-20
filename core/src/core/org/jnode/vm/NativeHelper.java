package org.jnode.vm;

import java.lang.reflect.InvocationTargetException;
import org.jnode.vm.classmgr.VmField;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;

/**
 * Set of methods used to access methods and fields (that are not normally accessible) by reflection.
 * <p/>
 * TODO restrict the access to this class and its methods.
 *
 * @author Fabien DUMINY (fduminy at jnode.org)
 */
public final class NativeHelper {
    public static final Object[] NO_ARGS = new Object[0];

    private static void NativeHelper() {
    }

    public static final <T> VmMethod findConstructor(Class<T> clazz, String signature) {
        VmMethod method = VmType.fromClass(clazz).getMethod("<init>", signature);
        if (method == null) {
            throw new RuntimeException(
                "Can't find constructor " + clazz.getName() + '.' + method.getName() + method.getSignature());
        }
        return method;
    }

    public static <T> VmMethod findDeclaredMethod(Class<T> clazz, String methodName, String signature) {
        VmMethod method = VmType.fromClass(clazz).getDeclaredMethod(methodName, signature);
        if (method == null) {
            throw new RuntimeException("Can't find declared method " + methodName + " in class " + clazz.getName());
        }
        return method;
    }

    public static <T> T invoke(Class<T> resultClass, Object object, VmMethod method) {
        try {
            return resultClass.cast(VmReflection.invoke(method, object, NO_ARGS));
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getTargetException());
        }
    }

    public static <T> VmField findDeclaredField(Class<T> clazz, String fieldName) {
        VmField field = VmType.fromClass(clazz).getDeclaredField(fieldName);
        if (field == null) {
            throw new RuntimeException("Can't find declared field " + fieldName + " in class " + clazz.getName());
        }
        return field;
    }
}
