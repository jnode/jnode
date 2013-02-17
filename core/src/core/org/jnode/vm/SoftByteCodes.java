/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.vm;

import org.jnode.annotation.LoadStatics;
import org.jnode.annotation.MagicPermission;
import org.jnode.annotation.PrivilegedActionPragma;
import org.jnode.annotation.Uninterruptible;
import org.jnode.vm.classmgr.TIBLayout;
import org.jnode.vm.classmgr.VmArrayClass;
import org.jnode.vm.classmgr.VmClassLoader;
import org.jnode.vm.classmgr.VmConstClass;
import org.jnode.vm.classmgr.VmConstFieldRef;
import org.jnode.vm.classmgr.VmConstMethodRef;
import org.jnode.vm.classmgr.VmField;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.facade.VmHeapManager;
import org.jnode.vm.facade.VmUtils;


/**
 * Class with software implementations of "difficult" java bytecodes.
 *
 * @author epr
 */
@Uninterruptible
@MagicPermission
public final class SoftByteCodes {

    private static VmHeapManager heapManager;

    /**
     * Is the given object instance of the given class.
     *
     * @param object
     * @param T
     * @return boolean
     */
    public static boolean isInstanceof(Object object, VmType T) {
        if (object == null) {
            return false;
        } else {
            final VmType[] superClasses = Unsafe.getSuperClasses(object);
            final int length = superClasses.length;
            for (int i = 0; i < length; i++) {
                if (superClasses[i] == T) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Resolve a const reference to a field to the actual field, in the context
     * of the given current method.
     *
     * @param currentMethod
     * @param fieldRef
     * @param isStatic
     * @return VmField
     */
    public static VmField resolveField(VmMethod currentMethod,
                                       VmConstFieldRef fieldRef, boolean isStatic) {
        if (!fieldRef.getConstClass().isResolved()) {
            resolveClass(fieldRef.getConstClass());
        }
        VmField result;
        if (fieldRef.isResolved()) {
            result = fieldRef.getResolvedVmField();
        } else {
            VmType<?> vmClass = fieldRef.getConstClass().getResolvedVmClass();
            vmClass.link();
            VmField field = vmClass.getField(fieldRef);
            if (field == null) {
                throw new NoSuchFieldError();
            }

            fieldRef.setResolvedVmField(field);
            result = field;
        }
        VmType<?> declClass = result.getDeclaringClass();
        if ((isStatic) && (!declClass.isAlwaysInitialized())) {
            if (!(result.isPrimitive() && result.isFinal())) {
                declClass.initialize();
            }
        }
        return result;
    }

    /**
     * Resolve a const reference to a method to the actual method, in the
     * context of the given current method.
     *
     * @param currentMethod
     * @param methodRef
     * @return VmMethod
     */
    public static VmMethod resolveMethod(VmMethod currentMethod,
                                         VmConstMethodRef methodRef) {
        if (!methodRef.getConstClass().isResolved()) {
            resolveClass(methodRef.getConstClass());
        }
        if (methodRef.isResolved()) {
            return methodRef.getResolvedVmMethod();
        } else {
            VmType<?> vmClass = methodRef.getConstClass()
                .getResolvedVmClass();
            vmClass.link();

            // NEW
            VmClassLoader curLoader = currentMethod.getDeclaringClass()
                .getLoader();
            methodRef.resolve(curLoader);
            return methodRef.getResolvedVmMethod();
            // END NEW
            /*
             * VmMethod method = vmClass.getMethod(methodRef); if (method ==
             * null) { String mname = methodRef.getName(); String cname =
             * methodRef.getClassName(); Screen.debug("method not found ");
             * Screen.debug(mname); Screen.debug(" in "); Screen.debug(cname);
             * throw new NoSuchMethodError(cname); }
             * 
             * methodRef.setResolvedVmMethod(method);
             */
        }
    }

    /**
     * Resolve a const reference to a class to the actual class, in the context
     * of the given current method.
     *
     * @param classRef
     * @return VmClass
     */
    @PrivilegedActionPragma
    public static VmType resolveClass(VmConstClass classRef) {
        if (classRef.isResolved()) {
            return classRef.getResolvedVmClass();
        } else {
            VmClassLoader curLoader = VmSystem.getContextClassLoader();
            String cname = classRef.getClassName();
            try {
                Class<?> cls = curLoader.asClassLoader().loadClass(cname);
                VmType<?> vmClass = VmType.fromClass(cls);

                /*
                 * VmClass vmClass = curLoader.loadClass(cname, true); //VmClass
                 * vmClass = Main.getBootClass(classRef); if (vmClass == null) {
                 * throw new NoClassDefFoundError(cname);
                 */
                classRef.setResolvedVmClass(vmClass);
                return vmClass;
            } catch (ClassNotFoundException ex) {
                // ex.printStackTrace();
                // Unsafe.debug("resolve::CLASSNOTFOUND");
                throw new NoClassDefFoundError(cname);
            }
        }
    }

    /**
     * Allocate a new object with a given class and a given size in bytes. If
     * size &lt; 0, the objectsize from the given class is used. The given size
     * does not include the length of the object header.
     *
     * @param vmClass
     * @param size
     * @return Object The new object
     */
    public static Object allocObject(VmType<?> vmClass, int size) {
        VmHeapManager hm = heapManager;
        if (hm == null) {
            heapManager = hm = VmUtils.getVm().getHeapManager();
        }
        final Object result;
        if (size < 0) {
            result = hm.newInstance(vmClass);
        } else {
            result = hm.newInstance(vmClass, size);
        }
        return result;
    }

    /**
     * Allocate a multi dimensional array
     *
     * @param vmClass
     * @param dimensions
     * @return The allocated array
     */
    public static Object allocMultiArray(VmType vmClass, int[] dimensions) {
        // Syslog.debug("allocMultiArray "); // + vmClass);
        return multinewarray_helper(dimensions, dimensions.length - 1,
            (VmArrayClass) vmClass);
    }

    /**
     * Allocates a multidimensional array of type a, with dimensions given in
     * dims[ind] to dims[dims.length-1]. a must be of dimensionality at least
     * dims.length-ind.
     *
     * @param dims array of dimensions in reverse order
     * @param ind  start index in array dims
     * @param a    array type
     * @return allocated array object
     * @throws NegativeArraySizeException if one of the array sizes in dims is negative
     * @throws OutOfMemoryError           if there is not enough memory to perform operation
     */
    public static Object multinewarray_helper(int[] dims, int ind,
                                              VmArrayClass<?> a) throws OutOfMemoryError,
        NegativeArraySizeException {
        // Syslog.debug("multinewarray_helper "); //+ " cls=" + a);
        a.initialize();
        final int length = dims[ind];
        final Object o = allocArray(a, length);
        if (ind == 0) {
            return o;
        }
        final Object[] o2 = (Object[]) o;
        final VmArrayClass<?> a2 = (VmArrayClass<?>) a.getComponentType();
        a2.initialize();
        for (int i = 0; i < length; ++i) {
            o2[i] = multinewarray_helper(dims, ind - 1, a2);
        }
        return o2;
    }

    /**
     * Allocate a new array with a given class as component type and a given
     * number of elements.
     *
     * @param vmClass
     * @param elements
     * @return Object The new array
     */
    public static Object anewarray(VmType<?> vmClass, int elements) {

        final VmArrayClass<?> arrCls = vmClass.getArrayClass();
        VmHeapManager hm = heapManager;
        if (hm == null) {
            heapManager = hm = VmUtils.getVm().getHeapManager();
        }
        final Object result = hm.newArray(arrCls, elements);

        // Screen.debug("}");
        return result;
    }

    /**
     * Allocate a new primivite array with a given arraytype and a given number
     * of elements.
     *
     * @param currentClass
     * @param atype
     * @param elements
     * @return Object The new array
     */
    public static Object allocPrimitiveArray(VmType<?> currentClass,
                                             int atype, int elements) {
        VmHeapManager hm = heapManager;
        if (hm == null) {
            heapManager = hm = VmUtils.getVm().getHeapManager();
        }
        if (false) {
            if (atype == 5) {
                if (VmSystem.isInitialized()) {
                    // Trace new char[]
                    VmUtils.getVm().getCounter(currentClass.getName()).add(elements);
                }
            }
        }
        final Object result = hm.newArray(VmType.getPrimitiveArrayClass(atype),
            elements);
        return result;
    }

    /**
     * Allocate a new array with a given class and a given number of elements.
     *
     * @param vmClass
     * @param elements
     * @return Object The new array
     */
    public static Object allocArray(VmType vmClass, int elements) {
        VmHeapManager hm = heapManager;
        if (hm == null) {
            heapManager = hm = VmUtils.getVm().getHeapManager();
        }
        final Object result = hm.newArray((VmArrayClass) vmClass, elements);
        return result;
    }

    /**
     * Throw a classcast exception.
     *
     * @param object
     * @param expected
     */
    public static void classCastFailed(Object object, VmType<?> expected) {
        if (object == null) {
            throw new ClassCastException("Object is null");
        } else if (true) {
            final Object[] tib = VmMagic.getTIB(object);
            if (tib == null) {
                throw new ClassCastException(object.getClass().getName()
                    + " tib==null");
            }
            final Object[] superClasses = (Object[]) tib[TIBLayout.SUPERCLASSES_INDEX];
            if (superClasses == null) {
                throw new ClassCastException(object.getClass().getName()
                    + " superClasses==null");
            }
            
            final StringBuilder sb = new StringBuilder("expected : ");
            sb.append(expected.getName());
            
            sb.append(" actual class : ");            
            sb.append(object.getClass().getName());
            
            sb.append(" superClasses : ");
            for (Object sc : superClasses) {
                sb.append(',');
                sb.append(sc);
                if (sc == expected) {
                    sb.append(" FOUND IT !!!! ");
                }
            }
            throw new ClassCastException(sb.toString());
        } else {
            throw new ClassCastException(object.getClass().getName());
        }
    }

    /**
     * Gets the Class that corresponds to the given VmType.
     *
     * @param <T>
     * @param type
     * @return the Class that corresponds to the given VmType
     */
    public static <T> Class<T> getClassForVmType(VmType<T> type) {
        return type.asClass();
    }

    /**
     * Throw an array index out of bounds exception.
     *
     * @param array
     * @param index
     */
    public static void throwArrayOutOfBounds(Object array, int index) {
        throw new ArrayIndexOutOfBoundsException(index);
    }

    /**
     * An unknown CPU opcode is execute.
     *
     * @param opcode
     * @param pc
     */
    @LoadStatics
    @PrivilegedActionPragma
    public static void unknownOpcode(int opcode, int pc) {
        throw new Error("Unknown opcode " + opcode + " at pc " + pc);
    }
}
