/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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

import java.lang.reflect.InvocationTargetException;

import org.jnode.annotation.MagicPermission;
import org.jnode.annotation.PrivilegedActionPragma;
import org.jnode.vm.classmgr.VmConstString;
import org.jnode.vm.classmgr.VmField;
import org.jnode.vm.classmgr.VmInstanceField;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmStaticField;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.facade.VmHeapManager;
import org.jnode.vm.facade.VmUtils;
import org.jnode.vm.facade.VmWriteBarrier;
import org.jnode.vm.scheduler.VmProcessor;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.ObjectReference;

/**
 * <description>
 *
 * @author epr
 */
@MagicPermission
public final class VmReflection {

    public static Object getObject(VmField field, Object o) {
        if (field.isStatic()) {
            final VmStaticField sf = (VmStaticField) field;
            initialize(sf);
            Object obj = getStaticFieldAddress(sf).loadObjectReference().toObject();
            //handles the reflective access to static final String fields, which didn't work.
            if (obj != null && sf.isFinal() && field.getSignature().equals("Ljava/lang/String;")) {
                if (obj instanceof VmConstString) {
                    VmConstString cs = (VmConstString) obj;
                    obj = VmUtils.getVm().getSharedStatics().getStringEntry(cs.getSharedStaticsIndex());
                }
            }
            return obj;
        } else {
            final VmInstanceField inf = (VmInstanceField) field;
            return getInstanceFieldAddress(o, inf).loadObjectReference().toObject();
        }
    }

    public static boolean getBoolean(VmField field, Object o) {
        if (field.isStatic()) {
            final VmStaticField sf = (VmStaticField) field;
            initialize(sf);
            return (getStaticFieldAddress(sf).loadInt() != 0);
        } else {
            final VmInstanceField inf = (VmInstanceField) field;
            return (getInstanceFieldAddress(o, inf).loadByte() != 0);
        }
    }

    public static byte getByte(VmField field, Object o) {
        if (field.isStatic()) {
            final VmStaticField sf = (VmStaticField) field;
            initialize(sf);
            return (byte) getStaticFieldAddress(sf).loadInt();
        } else {
            final VmInstanceField inf = (VmInstanceField) field;
            return getInstanceFieldAddress(o, inf).loadByte();
        }
    }

    public static char getChar(VmField field, Object o) {
        if (field.isStatic()) {
            final VmStaticField sf = (VmStaticField) field;
            initialize(sf);
            return (char) getStaticFieldAddress(sf).loadInt();
        } else {
            final VmInstanceField inf = (VmInstanceField) field;
            return getInstanceFieldAddress(o, inf).loadChar();
        }
    }

    public static short getShort(VmField field, Object o) {
        if (field.isStatic()) {
            final VmStaticField sf = (VmStaticField) field;
            initialize(sf);
            return (short) getStaticFieldAddress(sf).loadInt();
        } else {
            final VmInstanceField inf = (VmInstanceField) field;
            return getInstanceFieldAddress(o, inf).loadShort();
        }
    }

    public static int getInt(VmField field, Object o) {
        if (field.isStatic()) {
            final VmStaticField sf = (VmStaticField) field;
            initialize(sf);
            return getStaticFieldAddress(sf).loadInt();
        } else {
            final VmInstanceField inf = (VmInstanceField) field;
            return getInstanceFieldAddress(o, inf).loadInt();
        }
    }

    public static float getFloat(VmField field, Object o) {
        if (field.isStatic()) {
            final VmStaticField sf = (VmStaticField) field;
            initialize(sf);
            return getStaticFieldAddress(sf).loadFloat();
        } else {
            final VmInstanceField inf = (VmInstanceField) field;
            return getInstanceFieldAddress(o, inf).loadFloat();
        }
    }

    public static long getLong(VmField field, Object o) {
        if (field.isStatic()) {
            final VmStaticField sf = (VmStaticField) field;
            initialize(sf);
            return getStaticFieldAddress(sf).loadLong();
        } else {
            final VmInstanceField inf = (VmInstanceField) field;
            return getInstanceFieldAddress(o, inf).loadLong();
        }
    }

    public static double getDouble(VmField field, Object o) {
        if (field.isStatic()) {
            final VmStaticField sf = (VmStaticField) field;
            initialize(sf);
            return getStaticFieldAddress(sf).loadDouble();
        } else {
            final VmInstanceField inf = (VmInstanceField) field;
            return getInstanceFieldAddress(o, inf).loadDouble();
        }
    }

    public static void setObject(VmField field, Object o, Object value) {
        if (field.isStatic()) {
            final VmStaticField sf = (VmStaticField) field;
            initialize(sf);
            getStaticFieldAddress(sf).store(ObjectReference.fromObject(value));
            final VmWriteBarrier wb = VmUtils.getVm().getHeapManager().getWriteBarrier();
            if (wb != null) {
                if (sf.isShared()) {
                    wb.putstaticWriteBarrier(true, sf.getSharedStaticsIndex(), value);
                } else {
                    wb.putstaticWriteBarrier(true, sf.getIsolatedStaticsIndex(), value);
                }
            }
        } else {
            final VmInstanceField inf = (VmInstanceField) field;
            final int offset = inf.getOffset();
            getInstanceFieldAddress(o, inf).store(ObjectReference.fromObject(value));
            final VmWriteBarrier wb = VmUtils.getVm().getHeapManager().getWriteBarrier();
            if (wb != null) {
                wb.putfieldWriteBarrier(o, offset, value);
            }
        }
    }

    public static void setBoolean(VmField field, Object o, boolean value) {
        if (field.isStatic()) {
            final VmStaticField sf = (VmStaticField) field;
            initialize(sf);
            getStaticFieldAddress(sf).store((int) (value ? 1 : 0));
        } else {
            final VmInstanceField inf = (VmInstanceField) field;
            getInstanceFieldAddress(o, inf).store((byte) (value ? 1 : 0));
        }
    }

    public static void setByte(VmField field, Object o, byte value) {
        if (field.isStatic()) {
            final VmStaticField sf = (VmStaticField) field;
            initialize(sf);
            getStaticFieldAddress(sf).store((int) value);
        } else {
            final VmInstanceField inf = (VmInstanceField) field;
            getInstanceFieldAddress(o, inf).store((byte) value);
        }
    }

    public static void setChar(VmField field, Object o, char value) {
        if (field.isStatic()) {
            final VmStaticField sf = (VmStaticField) field;
            initialize(sf);
            getStaticFieldAddress(sf).store((int) value);
        } else {
            final VmInstanceField inf = (VmInstanceField) field;
            getInstanceFieldAddress(o, inf).store((char) value);
        }
    }

    public static void setShort(VmField field, Object o, short value) {
        if (field.isStatic()) {
            final VmStaticField sf = (VmStaticField) field;
            initialize(sf);
            getStaticFieldAddress(sf).store((int) value);
        } else {
            final VmInstanceField inf = (VmInstanceField) field;
            getInstanceFieldAddress(o, inf).store((short) value);
        }
    }

    public static void setInt(VmField field, Object o, int value) {
        if (field.isStatic()) {
            final VmStaticField sf = (VmStaticField) field;
            initialize(sf);
            getStaticFieldAddress(sf).store(value);
        } else {
            final VmInstanceField inf = (VmInstanceField) field;
            getInstanceFieldAddress(o, inf).store(value);
        }
    }

    public static void setFloat(VmField field, Object o, float value) {
        if (field.isStatic()) {
            final VmStaticField sf = (VmStaticField) field;
            initialize(sf);
            getStaticFieldAddress(sf).store(value);
        } else {
            final VmInstanceField inf = (VmInstanceField) field;
            getInstanceFieldAddress(o, inf).store(value);
        }
    }

    public static void setLong(VmField field, Object o, long value) {
        if (field.isStatic()) {
            final VmStaticField sf = (VmStaticField) field;
            initialize(sf);
            getStaticFieldAddress(sf).store(value);
        } else {
            final VmInstanceField inf = (VmInstanceField) field;
            getInstanceFieldAddress(o, inf).store(value);
        }
    }

    public static void setDouble(VmField field, Object o, double value) {
        if (field.isStatic()) {
            final VmStaticField sf = (VmStaticField) field;
            initialize(sf);
            getStaticFieldAddress(sf).store(value);
        } else {
            final VmInstanceField inf = (VmInstanceField) field;
            getInstanceFieldAddress(o, inf).store(value);
        }
    }

    /**
     * Gets the address of the static field data (in the statics table)
     *
     * @param sf
     * @return The address of the static field data
     */
    private static final Address getStaticFieldAddress(VmStaticField sf) {
        final VmProcessor proc = VmProcessor.current();
        final Address tablePtr;
        final int offset;
        if (sf.isShared()) {
            offset = sf.getSharedStaticsIndex() << 2;
            tablePtr = VmMagic.getArrayData(proc.getSharedStaticsTable());
        } else {
            offset = sf.getIsolatedStaticsIndex() << 2;
            tablePtr = VmMagic.getArrayData(proc.getIsolatedStaticsTable());
        }
        return tablePtr.add(offset);
    }

    /**
     * Gets the address of the instance field data (in the given object)
     *
     * @param f
     * @return The address of the instance field data in the given object.
     */
    private static final Address getInstanceFieldAddress(Object object, VmInstanceField f) {
        final Address objectPtr = ObjectReference.fromObject(object).toAddress();
        return objectPtr.add(f.getOffset());
    }

    /**
     * Invoke the given method, which must be static and have no arguments
     *
     * @param method
     * @throws InvocationTargetException
     */
    public static void invokeStatic(VmMethod method)
        throws InvocationTargetException {
        try {
            Unsafe.invokeVoid(method);
        } catch (Throwable ex) {
            throw new InvocationTargetException(ex);
        }
    }

    /**
     * Invoke the given method on the given object with the given arguments.
     *
     * @param method
     * @param o
     * @param args
     * @return Object
     * @throws InvocationTargetException
     */
    @PrivilegedActionPragma
    //todo verify this wrt. security implications
    public static Object invoke(VmMethod method, Object o, Object[] args)
        throws InvocationTargetException {
        int argCount = method.getNoArguments();
        int argsLength = (args == null) ? 0 : args.length;
        if (argCount != argsLength) {
            throw new IllegalArgumentException("Invalid number of arguments");
        }

        if (!method.isStatic()) {
            if (o == null) {
                throw new NullPointerException();
            }

            //todo should we make this check here or on a higher level?
            Class declaringClass = method.getDeclaringClass().asClass();
            if (!declaringClass.isInstance(o)) {
                throw new IllegalArgumentException("Target object arg is not an instance of " +
                    declaringClass.getName());
            }

            Unsafe.pushObject(o);
            if (!method.isConstructor()) {
                //todo implement dynamic method lookup according to JLS 15.12.4.4
                if (method.isAbstract())
                    method = VmType.fromClass(o.getClass()).getMethod(method.getName(), method.getSignature());
                else if (java.lang.reflect.Proxy.isProxyClass(o.getClass())) {
                    method = VmType.fromClass(o.getClass()).getMethod(method.getName(), method.getSignature());
                }
            }
        } else {
            method.getDeclaringClass().initialize();
        }

        for (int i = 0; i < argCount; i++) {
            final VmType<?> argType = method.getArgumentType(i);
            final Object arg = args[i];
            if (argType.isPrimitive()) {
                if (arg == null)
                    throw new IllegalArgumentException();

                int v = 0;
                long lv = 0;
                boolean wide = false;
                switch (argType.getJvmType()) {
                    case JvmType.BOOLEAN: {
                        if (arg instanceof Boolean) {
                            v = (Boolean) arg ? 1 : 0;
                        } else {
                            throw new IllegalArgumentException("argument type mismatch");
                        }
                        break;
                    }
                    case JvmType.INT: {
                        if (arg instanceof Integer) {
                            v = (Integer) arg;
                        } else if (arg instanceof Byte) {
                            v = (Byte) arg;
                        } else if (arg instanceof Character) {
                            v = (Character) arg;
                        } else if (arg instanceof Short) {
                            v = (Short) arg;
                        } else {
                            throw new IllegalArgumentException("argument type mismatch");
                        }
                        break;
                    }
                    case JvmType.CHAR: {
                        if (arg instanceof Character) {
                            v = (Character) arg;
                        } else {
                            throw new IllegalArgumentException("argument type mismatch");
                        }
                        break;
                    }
                    case JvmType.BYTE: {
                        if (arg instanceof Byte) {
                            v = (Byte) arg;
                        } else {
                            throw new IllegalArgumentException("argument type mismatch");
                        }
                        break;
                    }
                    case JvmType.SHORT: {
                        if (arg instanceof Short) {
                            v = (Short) arg;
                        } else if (arg instanceof Byte) {
                            v = (Byte) arg;
                        } else {
                            throw new IllegalArgumentException("argument type mismatch");
                        }
                        break;
                    }
                    case JvmType.FLOAT: {
                        if (arg instanceof Float) {
                            v = Float.floatToRawIntBits((Float) arg);
                        } else if (arg instanceof Byte) {
                            v = Float.floatToRawIntBits((Byte) arg);
                        } else if (arg instanceof Short) {
                            v = Float.floatToRawIntBits((Short) arg);
                        } else if (arg instanceof Integer) {
                            v = Float.floatToRawIntBits((Integer) arg);
                        } else if (arg instanceof Character) {
                            v = Float.floatToRawIntBits((Character) arg);
                        } else {
                            throw new IllegalArgumentException("argument type mismatch");
                        }
                        break;
                    }
                    case JvmType.LONG: {
                        wide = true;
                        if (arg instanceof Long) {
                            lv = (Long) arg;
                        } else if (arg instanceof Integer) {
                            lv = (Integer) arg;
                        } else if (arg instanceof Byte) {
                            lv = (Byte) arg;
                        } else if (arg instanceof Short) {
                            lv = (Short) arg;
                        } else if (arg instanceof Character) {
                            lv = (Character) arg;
                        } else {
                            throw new IllegalArgumentException("argument type mismatch");
                        }
                        break;
                    }
                    case JvmType.DOUBLE: {
                        wide = true;
                        if (arg instanceof Double) {
                            lv = Double.doubleToRawLongBits((Double) arg);
                        } else if (arg instanceof Integer) {
                            lv = Double.doubleToRawLongBits((Integer) arg);
                        } else if (arg instanceof Byte) {
                            lv = Double.doubleToRawLongBits((Byte) arg);
                        } else if (arg instanceof Short) {
                            lv = Double.doubleToRawLongBits((Short) arg);
                        } else if (arg instanceof Character) {
                            lv = Double.doubleToRawLongBits((Character) arg);
                        } else if (arg instanceof Float) {
                            lv = Double.doubleToRawLongBits((Float) arg);
                        } else if (arg instanceof Long) {
                            lv = Double.doubleToRawLongBits((Long) arg);
                        } else {
                            throw new IllegalArgumentException("argument type mismatch");
                        }
                        break;
                    }
                    default: {
                        throw new RuntimeException("invalid argument type: " + argType);
                    }
                }

                final Class argClass = argType.asClass();
                if ((argClass == Long.TYPE) || (argClass == Double.TYPE)) {
                    // Wide argument
                    if (wide) {
                        Unsafe.pushLong(lv);
                    } else {
                        Unsafe.pushLong(v);
                    }
                } else {
                    // Normal argument
                    if (wide) {
                        Unsafe.pushInt((int) lv);
                    } else {
                        Unsafe.pushInt(v);
                    }
                }
            } else {
                if (arg != null) {
                    if (!argType.isAssignableFrom(VmType.fromClass(arg.getClass()))) {
                        throw new IllegalArgumentException("argument type mismatch");
                    }
                }
                // Non-primitive argument
                Unsafe.pushObject(arg);
            }
        }

        try {
            if (method.isReturnVoid()) {
                Unsafe.invokeVoid(method);
                return null;
            } else if (method.isReturnObject()) {
                return Unsafe.invokeObject(method);
            } else if (method.isReturnWide()) {
                long rc = Unsafe.invokeLong(method);
                final Class<?> retType = method.getReturnType().asClass();
                if (Long.TYPE == retType) {
                    return rc;
                } else {
                    return Double.longBitsToDouble(rc);
                }
            } else {
                int rc = Unsafe.invokeInt(method);
                final Class retType = method.getReturnType().asClass();
                if (Byte.TYPE == retType) {
                    return (byte) rc;
                } else if (Boolean.TYPE == retType) {
                    return rc != 0;
                } else if (Character.TYPE == retType) {
                    return (char) rc;
                } else if (Short.TYPE == retType) {
                    return (short) rc;
                } else if (Float.TYPE == retType) {
                    return Float.intBitsToFloat(rc);
                } else {
                    return rc;
                }
            }
        } catch (Throwable ex) {
            throw new InvocationTargetException(ex);
        }
    }

    /**
     * Create and return a new object using the given constructor and arguments
     *
     * @param constructor
     * @param args
     * @return Object
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static Object newInstance(VmMethod constructor, Object[] args)
        throws InstantiationException, IllegalAccessException,
        InvocationTargetException {
        final VmHeapManager hm = VmUtils.getVm().getHeapManager();
        final Object obj = hm.newInstance(constructor.getDeclaringClass());
        invoke(constructor, obj, args);
        return obj;
    }

    /**
     * Create and return a new object using the given constructor no arguments
     *
     * @param constructor
     * @return Object
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static Object newInstance(VmMethod constructor)
        throws InstantiationException, IllegalAccessException,
        InvocationTargetException {
        final VmHeapManager hm = VmUtils.getVm().getHeapManager();
        final Object obj = hm.newInstance(constructor.getDeclaringClass());
        Unsafe.pushObject(obj);
        Unsafe.invokeVoid(constructor);
        return obj;
    }

    /**
     * Initialize the class that declared this field if needed.
     *
     * @param sf
     */
    private static final void initialize(VmStaticField sf) {
        final VmType<?> declClass = sf.getDeclaringClass();
        if (!declClass.isAlwaysInitialized()) {
            if (!(sf.isPrimitive() && sf.isFinal())) {
                sf.getDeclaringClass().initialize();
            }
        }
    }
}
