package org.jnode.vm;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Set of methods used to access methods and fields that should be accessible by reflection.
 * <p/>
 * TODO optimize these methods (use VmReflection ...) and restrict the access to this class and its methods.
 * The current (unoptimized) implementation need some permissions if the SecurityManager is enabled.
 *
 * @author Fabien DUMINY (fduminy at jnode.org)
 */
public final class NativeHelper {
    private static void NativeHelper() {
    }

    public static final <T> T newInstance(Class<T> clazz, Class[] parameterTypes, Object[] parameterValues) {
        boolean noParameters = (parameterTypes == null) && (parameterValues == null);
        try {
            if (noParameters) {
                return clazz.newInstance();
            } else {
                Constructor<T> constructor = clazz.getConstructor(parameterTypes);

                try {
                    constructor.setAccessible(true);
                    return constructor.newInstance(parameterValues);
                } finally {
                    constructor.setAccessible(false);
                }
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T callGetter(Class<T> clazz, Object object, String methodName) {
        try {
            Method method = object.getClass().getMethod(methodName);
            try {
                method.setAccessible(true);
                return clazz.cast(method.invoke(object));
            } finally {
                method.setAccessible(false);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T getFieldValue(Class<T> clazz, Object object, String methodName) {
        try {
            Field field = object.getClass().getField(methodName);
            try {
                field.setAccessible(true);
                return clazz.cast(field.get(object));
            } finally {
                field.setAccessible(false);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
//
//    public static final <T> void setStaticFieldValue(Class<T> clazz, String fieldName, Object fieldValue) {
//        try {
//            Field field = clazz.getField(fieldName);
//            try {
//                field.setAccessible(true);
//                field.set(null, fieldValue);
//            } finally {
//                field.setAccessible(false);
//            }
//        } catch (IllegalAccessException e) {
//            throw new RuntimeException(e);
//        } catch (NoSuchFieldException e) {
//            throw new RuntimeException(e);
//        }
//    }
}
