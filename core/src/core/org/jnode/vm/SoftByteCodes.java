/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */

package org.jnode.vm;

import org.jnode.util.NumberUtils;
import org.jnode.vm.classmgr.VmArrayClass;
import org.jnode.vm.classmgr.VmClassLoader;
import org.jnode.vm.classmgr.VmConstClass;
import org.jnode.vm.classmgr.VmConstFieldRef;
import org.jnode.vm.classmgr.VmConstMethodRef;
import org.jnode.vm.classmgr.VmField;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.memmgr.VmHeapManager;
import org.vmmagic.pragma.LoadStaticsPragma;
import org.vmmagic.pragma.PrivilegedActionPragma;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.pragma.UninterruptiblePragma;

/**
 * Class with software implementations of "difficult" java bytecodes.
 * 
 * @author epr
 */
public class SoftByteCodes implements Uninterruptible {

    public static final int EX_NULLPOINTER = 0;

    public static final int EX_PAGEFAULT = 1;

    public static final int EX_INDEXOUTOFBOUNDS = 2;

    public static final int EX_DIV0 = 3;

    public static final int EX_ABSTRACTMETHOD = 4;

    public static final int EX_STACKOVERFLOW = 5;

    // public static final int EX_CLASSCAST = 6;
    public static final int EX_COPRO_OR = 7;

    public static final int EX_COPRO_ERR = 8;

    private static VmHeapManager heapManager;

    /**
     * Is the given object instance of the given class.
     * 
     * @param object
     * @param T
     * @return boolean
     * @throws UninterruptiblePragma
     */
    public static boolean isInstanceof(Object object, VmType T)
            throws UninterruptiblePragma {
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
     * @throws UninterruptiblePragma
     */
    public static VmField resolveField(VmMethod currentMethod,
            VmConstFieldRef fieldRef, boolean isStatic)
            throws UninterruptiblePragma {
        if (!fieldRef.getConstClass().isResolved()) {
            resolveClass(fieldRef.getConstClass());
        }
        VmField result;
        if (fieldRef.isResolved()) {
            result = fieldRef.getResolvedVmField();
        } else {
            VmType vmClass = fieldRef.getConstClass().getResolvedVmClass();
            vmClass.link();
            VmField field = vmClass.getField(fieldRef);
            if (field == null) {
                throw new NoSuchFieldError();
            }

            fieldRef.setResolvedVmField(field);
            result = field;
        }
        VmType declClass = result.getDeclaringClass();
        if ((isStatic) && (!declClass.isInitialized())) {
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
     * @throws UninterruptiblePragma
     */
    public static VmMethod resolveMethod(VmMethod currentMethod,
            VmConstMethodRef methodRef) throws UninterruptiblePragma {
        if (!methodRef.getConstClass().isResolved()) {
            resolveClass(methodRef.getConstClass());
        }
        if (methodRef.isResolved()) {
            return methodRef.getResolvedVmMethod();
        } else {
            VmType vmClass = methodRef.getConstClass().getResolvedVmClass();
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
     * @throws UninterruptiblePragma
     */
    public static VmType resolveClass(VmConstClass classRef)
            throws UninterruptiblePragma, PrivilegedActionPragma {
        if (classRef.isResolved()) {
            return classRef.getResolvedVmClass();
        } else {
            VmClassLoader curLoader = VmSystem.getContextClassLoader();
            String cname = classRef.getClassName();
            try {
                Class cls = curLoader.asClassLoader().loadClass(cname);
                VmType vmClass = cls.getVmClass();

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
     * @throws UninterruptiblePragma
     */
    public static Object allocObject(VmType vmClass, int size)
            throws UninterruptiblePragma {
        vmClass.link();

        // Screen.debug("ao cls{");
        // Screen.debug(vmClass.getName());

        VmHeapManager hm = heapManager;
        if (hm == null) {
            heapManager = hm = Vm.getHeapManager();
        }
        Object result;
        if (size < 0) {
            result = hm.newInstance(vmClass);
        } else {
            result = hm.newInstance(vmClass, size);
        }
        // Screen.debug("}");

        return result;
    }

    /**
     * Allocate a multi dimensional array
     * 
     * @param vmClass
     * @param dimensions
     * @return The allocated array
     * @throws UninterruptiblePragma
     */
    public static Object allocMultiArray(VmType vmClass, int[] dimensions)
            throws UninterruptiblePragma {
        // Syslog.debug("allocMultiArray "); // + vmClass);
        return multinewarray_helper(dimensions, dimensions.length - 1,
                (VmArrayClass) vmClass);
    }

    /**
     * Allocates a multidimensional array of type a, with dimensions given in
     * dims[ind] to dims[dims.length-1]. a must be of dimensionality at least
     * dims.length-ind.
     * 
     * @return allocated array object
     * @param dims
     *            array of dimensions in reverse order
     * @param ind
     *            start index in array dims
     * @param a
     *            array type
     * @throws NegativeArraySizeException
     *             if one of the array sizes in dims is negative
     * @throws OutOfMemoryError
     *             if there is not enough memory to perform operation
     * @throws UninterruptiblePragma
     */
    public static Object multinewarray_helper(int[] dims, int ind,
            VmArrayClass a) throws OutOfMemoryError,
            NegativeArraySizeException, UninterruptiblePragma {
        // Syslog.debug("multinewarray_helper "); //+ " cls=" + a);
        a.initialize();
        final int length = dims[ind];
        final Object o = allocArray(a, length);
        if (ind == 0) {
            return o;
        }
        final Object[] o2 = (Object[]) o;
        final VmArrayClass a2 = (VmArrayClass) a.getComponentType();
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
     * @param currentMethod
     * @param vmClass
     * @param elements
     * @return Object The new array
     * @throws UninterruptiblePragma
     */
    public static Object anewarray(VmType vmClass, int elements)
            throws UninterruptiblePragma {

        final VmArrayClass arrCls = vmClass.getArrayClass();
        VmHeapManager hm = heapManager;
        if (hm == null) {
            heapManager = hm = Vm.getHeapManager();
        }
        final Object result = hm.newArray(arrCls, elements);

        // Screen.debug("}");
        return result;
    }

    /**
     * Allocate a new primivite array with a given arraytype and a given number
     * of elements.
     * 
     * @param atype
     * @param elements
     * @return Object The new array
     * @throws UninterruptiblePragma
     */
    public static Object allocPrimitiveArray(int atype, int elements)
            throws UninterruptiblePragma {
        VmHeapManager hm = heapManager;
        if (hm == null) {
            heapManager = hm = Vm.getHeapManager();
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
     * @throws UninterruptiblePragma
     */
    public static Object allocArray(VmType vmClass, int elements)
            throws UninterruptiblePragma {
        VmHeapManager hm = heapManager;
        if (hm == null) {
            heapManager = hm = Vm.getHeapManager();
        }
        final Object result = hm.newArray((VmArrayClass) vmClass, elements);
        return result;
    }

    /**
     * Throw a classcast exception.
     */
    public static void classCastFailed(Object object) {
        if (object == null) {
            throw new ClassCastException("Object is null");
        } else {
            throw new ClassCastException(object.getClass().getName());
        }
    }
    
    /**
     * Gets the Class that corresponds to the given VmType.
     * @param type
     * @return
     */
    public static Class getClassForVmType(VmType type) {
        return type.asClass();
    }

    /**
     * Create an exception for a system-trapped situation.
     * 
     * @param nr
     * @param address
     * @return Throwable
     * @throws UninterruptiblePragma
     */
    public static Throwable systemException(int nr, int address)
            throws UninterruptiblePragma, LoadStaticsPragma,
            PrivilegedActionPragma {
        // if (VmSystem.debug > 0) {
        // Unsafe.getCurrentProcessor().getArchitecture().getStackReader().debugStackTrace();
        // }
        if (false) {
            Unsafe.debug(nr);
            Unsafe.debug(address);
            Unsafe.die("System exception");
        }
        // Unsafe.debug(nr); Unsafe.debug(address);
        final String hexAddress = NumberUtils.hex(address, 8);
        final VmThread current = Unsafe.getCurrentProcessor()
                .getCurrentThread();
        // final String state = " (" + current.getReadableErrorState() + ")";
        final String state = "";
        // Mark a system exception, so the stacktrace uses the exception frame
        // instead of the current frame.
        current.setInSystemException();
        switch (nr) {
        case EX_NULLPOINTER:
            return new NullPointerException("NPE at address " + hexAddress
                    + state);
        case EX_PAGEFAULT:
            return new InternalError("Page fault at " + hexAddress + state);
        case EX_INDEXOUTOFBOUNDS:
            return new ArrayIndexOutOfBoundsException("Out of bounds at index "
                    + address + state);
        case EX_DIV0:
            return new ArithmeticException("Division by zero at address "
                    + hexAddress + state);
        case EX_ABSTRACTMETHOD:
            return new AbstractMethodError("Abstract method at " + hexAddress
                    + state);
        case EX_STACKOVERFLOW:
            return new StackOverflowError();
        // case EX_CLASSCAST:
        // if (false) {
        // Unsafe.getCurrentProcessor().getArchitecture().getStackReader().debugStackTrace();
        // Unsafe.die("Classcast failed");
        // }
        // return new ClassCastException();
        case EX_COPRO_OR:
            throw new ArithmeticException("Coprocessor overrun");
        case EX_COPRO_ERR:
            throw new ArithmeticException("Coprocessor error");
        default:
            return new UnknownError("Unknown system-exception at " + hexAddress
                    + state);
        }
    }

    /**
     * Throw an array index out of bounds exception.
     * 
     * @param array
     * @param index
     */
    public static void throwArrayOutOfBounds(Object array, int index)
            throws UninterruptiblePragma {
        throw new ArrayIndexOutOfBoundsException(index);
    }

    public static void unknownOpcode(int opcode, int pc)
            throws UninterruptiblePragma, LoadStaticsPragma,
            PrivilegedActionPragma {
        throw new Error("Unknown opcode " + opcode + " at pc " + pc);
    }
}
