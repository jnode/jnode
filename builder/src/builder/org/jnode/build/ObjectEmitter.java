/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.build;

import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Set;
import org.jnode.assembler.BootImageNativeStream;
import org.jnode.assembler.Label;
import org.jnode.assembler.NativeStream;
import org.jnode.assembler.x86.X86BinaryAssembler;
import org.jnode.system.BootLog;
import org.jnode.vm.BootableObject;
import org.jnode.vm.VmSystemObject;
import org.jnode.vm.classmgr.VmArrayClass;
import org.jnode.vm.classmgr.VmClassLoader;
import org.jnode.vm.classmgr.VmClassType;
import org.jnode.vm.classmgr.VmField;
import org.jnode.vm.classmgr.VmInstanceField;
import org.jnode.vm.classmgr.VmNormalClass;
import org.jnode.vm.classmgr.VmType;
import org.vmmagic.unboxed.UnboxedObject;

public class ObjectEmitter {

    private final VmClassLoader loaderContext;

    private final NativeStream os;

    private final BootImageNativeStream bis;

    private final PrintWriter debugWriter;

    private final Set<String> legalInstanceClasses;

    private final HashMap<String, FieldInfo> fieldInfos = new HashMap<String, FieldInfo>();

    /**
     * Construct a new ObjectEmitter *
     *
     * @param b
     * @param os
     * @param debug
     * @param legalInstanceClasses
     */
    public ObjectEmitter(VmClassLoader b, NativeStream os, PrintWriter debug,
                         Set<String> legalInstanceClasses) {
        this.loaderContext = b;
        this.os = os;
        this.bis = (BootImageNativeStream) os;
        this.legalInstanceClasses = legalInstanceClasses;
        this.debugWriter = debug;
    }

    /**
     * Write the header and the contents of an object to the native stream.
     *
     * @param obj
     * @throws BuildException
     * @throws ClassNotFoundException
     */
    public final void emitObject(Object obj) throws BuildException,
        ClassNotFoundException {
        if (obj == null) {
            return;
        }

        final Class<?> cls = obj.getClass();
        try {
            testForValidEmit(obj, cls.getName());
        } catch (JNodeClassNotFoundException ex) {
            throw new BuildException(ex);
        }

        if (debugWriter != null) {
            debugWriter.println("$" + Integer.toHexString(os.getLength()));
            if (obj instanceof char[]) {
                final char[] a = (char[]) obj;
                for (int i = 0; i < a.length; i++) {
                    debugWriter.print("'" + a[i] + "' ");
                }
                debugWriter.println();
            } else if (obj instanceof byte[]) {
                final byte[] a = (byte[]) obj;
                for (int i = 0; i < a.length; i++) {
                    debugWriter.print("" + a[i] + ' ');
                }
                debugWriter.println();
            } else {
                debugWriter.println(obj);
            }
        }

        if (obj instanceof VmSystemObject) {
            ((VmSystemObject) obj).verifyBeforeEmit();
        }

        // Writeout the header
        final VmClassType<?> vmClass = (VmClassType<?>) loaderContext
            .loadClass(cls.getName(), true);
        vmClass.incInstanceCount();
        final X86BinaryAssembler.ObjectInfo oInfo = os.startObject(vmClass);
        os.setObjectRef(obj);

        // If the object is a VmClass, force the loading of the
        // correspondig java.lang.Class
        if (cls.equals(VmType.class)) {
            final VmType<?> vmCls = (VmType) obj;
            String name = vmCls.getName().replace('/', '.');
            if (!name.startsWith("java.lang")) {
                vmCls.asClassDuringBootstrap();
            }
        }

        // Writeout object contents
        if (cls.equals(String.class)) {
            emitString((String) obj);
        } else if (cls.equals(Integer.class)) {
            emitInteger((Integer) obj);
        } else if (cls.equals(Long.class)) {
            emitLong((Long) obj);
        } else if (cls.equals(Class.class)) {
            emitClass((Class) obj);
        } else if (cls.isArray()) {
            emitArray(cls, obj, (VmArrayClass<?>) vmClass);
        } else {
            try {
                emitObject(cls, obj, (VmNormalClass<?>) vmClass);
            } catch (JNodeClassNotFoundException ex) {
                throw new BuildException(ex);
            }
        }
        oInfo.markEnd();

        if (debugWriter != null) {
            debugWriter.println();
        }
    }

    /**
     * Is it legal to emit the given object?
     *
     * @param object
     * @throws BuildException Is if not valid to emit the given object into the boot image.
     */
    public final void testForValidEmit(Object object, String location)
        throws BuildException, JNodeClassNotFoundException {
        if (object == null) {
            return;
        } else if (object instanceof BootableObject) {
            return;
        } else if (object.getClass().isArray()) {
            return;
        } else if (object instanceof Class) {
            return;
        } else {
            final String clsName = object.getClass().getName()
                .replace('/', '.');
            /*
             * if (clsName.startsWith("org.jnode.")) { return;
             */
            if (legalInstanceClasses.contains(clsName)) {
                return;
            }

            final FieldInfo fieldInfo;
            try {
                fieldInfo = getFieldInfo(object.getClass());
            } catch (ClassNotFoundException ex) {
                throw new BuildException(ex);
            }
            if (!fieldInfo.isExact()) {
                BootLog.warn("Use of in-exact matching class (" + clsName
                    + ") in bootimage at " + location);
            }
            legalInstanceClasses.add(clsName);
            return;

            /*
             * final Class javaClass = object.getClass(); try { final
             * VmClassType vmClass =
             * (VmClassType)loaderContext.loadClass(javaClass.getName(), true);
             * testClassCompatibility(javaClass, vmClass);
             * System.out.println("Found compatible class " + clsName);
             * legalInstanceClasses.add(clsName); } catch
             * (ClassNotFoundException ex) { throw new BuildException("VmClass
             * not found for " + clsName, ex);
             */
            // throw new BuildException("Cannot emit object of type " +
            // clsName);
        }
    }

    private void emitClass(Class<?> c) throws BuildException {
        try {
            if (!c.isPrimitive()) {
                // This layout should match the order and type of fields
                // in java.lang.Class

                // vmClass
                bis.writeObjectRef(loaderContext.loadClass(c.getName(), true));
                // declaredConstructors
                bis.writeObjectRef(null);
                // declaredFields;
                bis.writeObjectRef(null);
                // declaredMethods;
                bis.writeObjectRef(null);
                // fields;
                bis.writeObjectRef(null);
                // methods;
                bis.writeObjectRef(null);
                // interfaces;
                bis.writeObjectRef(null);
                // constructors;
                bis.writeObjectRef(null);
                // defaultConstructor;
                bis.writeObjectRef(null);
                // name
                bis.writeObjectRef(null);
                // enumConstants
                bis.writeObjectRef(null);
                // enumConstantsDirectory
                bis.writeObjectRef(null);
                // annotationType
                bis.writeObjectRef(null);

                //see the fields of java.lang.Class
                bis.writeObjectRef(null);
                bis.writeObjectRef(null);
                bis.writeObjectRef(null);
                bis.writeObjectRef(null);
                bis.writeObjectRef(null);
                bis.writeObjectRef(null);
                bis.writeObjectRef(null);
            }
        } catch (ClassNotFoundException ex) {
            throw new BuildException("emitting object: [" + c + "]", ex);
        }
    }

    private void emitString(String s) throws BuildException {
        // This layout should match the order and type of fields
        // in java.lang.String
        bis.writeObjectRef(s.toCharArray()); // char[] value
        os.write32(0); // int offset
        os.write32(s.length()); // int count
        os.write32(s.hashCode()); // int cachedHashCode        
    }

    private void emitInteger(Integer i) throws BuildException {
        // This layout should match the order and type of fields
        // in java.lang.Integer
        os.write32(i.intValue()); // int value
    }

    private void emitLong(Long l) throws BuildException {
        // This layout should match the order and type of fields
        // in java.lang.Long
        os.write64(l.longValue()); // long value
    }

    private void emitArray(Class<?> cls, Object obj, VmArrayClass vmClass) {
        final Class cmpType = cls.getComponentType();
        final int len = Array.getLength(obj);
        vmClass.incTotalLength(len);
        os.writeWord(len);
        if (cmpType == byte.class) {
            final byte[] a = (byte[]) obj;
            os.write(a, 0, len);
        } else if (cmpType == boolean.class) {
            final boolean[] a = (boolean[]) obj;
            for (int i = 0; i < len; i++) {
                os.write8(a[i] ? 1 : 0);
            }
        } else if (cmpType == char.class) {
            final char[] a = (char[]) obj;
            for (int i = 0; i < len; i++) {
                os.write16(a[i]);
            }
        } else if (cmpType == short.class) {
            final short[] a = (short[]) obj;
            for (int i = 0; i < len; i++) {
                os.write16(a[i]);
            }
        } else if (cmpType == int.class) {
            final int[] a = (int[]) obj;
            for (int i = 0; i < len; i++) {
                os.write32(a[i]);
            }
        } else if (cmpType == long.class) {
            final long[] a = (long[]) obj;
            for (int i = 0; i < len; i++) {
                os.write64(a[i]);
            }
        } else if (cmpType == float.class) {
            final float[] a = (float[]) obj;
            for (int i = 0; i < len; i++) {
                os.write32(Float.floatToRawIntBits(a[i]));
            }
        } else if (cmpType == double.class) {
            final double[] a = (double[]) obj;
            for (int i = 0; i < len; i++) {
                os.write64(Double.doubleToRawLongBits(a[i]));
            }
        } else {
            final Object[] a = (Object[]) obj;
            for (int i = 0; i < len; i++) {
                bis.writeObjectRef(a[i]);
            }
        }
    }

    /**
     * Allocate and write and object of a given type.
     *
     * @param <T>
     * @param cls
     * @param obj
     * @param vmType
     * @throws BuildException
     * @throws ClassNotFoundException
     * @throws JNodeClassNotFoundException
     */
    private void emitObject(Class<?> cls, Object obj,
                            VmNormalClass<?> vmType) throws BuildException,
        ClassNotFoundException, JNodeClassNotFoundException {
        final int objectOffset = bis.allocate(vmType.getObjectSize());
        storeObject(objectOffset, cls, obj, vmType);
    }

    /**
     * Store an object at a given offset.
     *
     * @param <T>
     * @param offset The offset of the start of the object.
     * @param cls
     * @param obj
     * @param vmType
     * @throws BuildException
     * @throws ClassNotFoundException
     * @throws JNodeClassNotFoundException
     */
    private void storeObject(int offset, Class<?> cls, Object obj,
                             VmClassType<?> vmType) throws BuildException,
        ClassNotFoundException, JNodeClassNotFoundException {
        final Class<?> sCls = cls.getSuperclass();
        if (sCls != null) {
            final VmClassType<?> vmSuperType = (VmClassType<?>) loaderContext
                .loadClass(sCls.getName(), true);
            storeObject(offset, sCls, obj, vmSuperType);
        }
        try {
            final FieldInfo fieldInfo = getFieldInfo(cls);
            // final Field fields[] = cls.getDeclaredFields();
            final Field[] fields = fieldInfo.getJdkInstanceFields();
            final int len = fields.length;
            // AccessibleObject.setAccessible(fields, true);
            for (int i = 0; i < len; i++) {
                final VmField jnodeField = fieldInfo.getJNodeInstanceField(i);
                final Field jdkField = fields[i];
                final int modifiers = jnodeField.getModifiers();

                if ((modifiers & Modifier.STATIC) != 0) {
                    throw new BuildException("Static field in instance list");
                }

                final int fldOffset = offset
                    + ((VmInstanceField) jnodeField).getOffset();
                if ((jdkField == null)
                    || ((modifiers & Modifier.TRANSIENT) != 0)) {
                    if (jnodeField.isWide()) {
                        os.set64(fldOffset, 0);
                    } else if (!jnodeField.isPrimitive()) {
                        os.setWord(fldOffset, 0);
                    } else {
                        switch (jnodeField.getTypeSize()) {
                            case 1:
                                os.set8(fldOffset, 0);
                                break;
                            case 2:
                                os.set16(fldOffset, 0);
                                break;
                            case 4:
                                os.set32(fldOffset, 0);
                                break;
                            default:
                                throw new BuildException("Invalid typesize in: " + jnodeField);
                        }
                    }
                    if (debugWriter != null) {
                        debugWriter.println(jnodeField.getName()
                            + " transient: 0");
                    }
                } else if (jnodeField.isPrimitive()) {
                    final Class<?> fType = jdkField.getType();
                    if (debugWriter != null) {
                        debugWriter.println(jdkField.getName() + " "
                            + jdkField.get(obj));
                    }
                    if (fType == byte.class) {
                        os.set8(fldOffset, jdkField.getByte(obj));
                    } else if (fType == boolean.class) {
                        os.set8(fldOffset, (jdkField.getBoolean(obj)) ? 1 : 0);
                    } else if (fType == char.class) {
                        os.set16(fldOffset, jdkField.getChar(obj));
                    } else if (fType == short.class) {
                        os.set16(fldOffset, jdkField.getShort(obj));
                    } else if (fType == int.class) {
                        os.set32(fldOffset, jdkField.getInt(obj));
                    } else if (fType == float.class) {
                        os.set32(fldOffset, Float.floatToIntBits(jdkField
                            .getFloat(obj)));
                    } else if (fType == long.class) {
                        os.set64(fldOffset, jdkField.getLong(obj));
                    } else if (fType == double.class) {
                        os.set64(fldOffset, Double.doubleToLongBits(jdkField
                            .getDouble(obj)));
                    } else {
                        throw new BuildException("Unknown primitive class "
                            + fType.getName());
                    }
                } else if (jnodeField.isAddressType()) {
                    final Object value = jdkField.get(obj);
                    if (value == null) {
                        os.setWord(fldOffset, 0);
                    } else if (value instanceof UnboxedObject) {
                        final UnboxedObject uobj = (UnboxedObject) value;
                        os.setWord(fldOffset, uobj.toLong());
                    } else if (value instanceof Label) {
                        final Label lbl = (Label) value;
                        bis.setObjectRef(fldOffset, lbl);
                    } else {
                        throw new BuildException("Cannot handle magic type " + value.getClass().getName());
                    }
                } else {
                    Object value = jdkField.get(obj);
                    try {
                        testForValidEmit(value, cls.getName());
                    } catch (BuildException ex) {
                        throw new BuildException("Cannot emit field "
                            + jdkField.getName() + " of class "
                            + cls.getName(), ex);
                    } catch (JNodeClassNotFoundException ex) {
                        BootLog
                            .warn("JNode class not found "
                                + ex.getMessage());
                        value = null;
                    }
                    bis.setObjectRef(fldOffset, value);
                }
            }
        } catch (IllegalAccessException ex) {
            throw new BuildException("emitting object: [" + obj + "]", ex);
        } catch (SecurityException ex) {
            throw new BuildException("emitting object: [" + obj + "]", ex);
        }
    }

    /**
     * Gets the field information for the given type.
     *
     * @param jdkType
     * @throws ClassNotFoundException
     */
    public FieldInfo getFieldInfo(Class<?> jdkType)
        throws ClassNotFoundException, JNodeClassNotFoundException {
        final String cname = jdkType.getName();
        FieldInfo info = fieldInfos.get(cname);
        if (info == null) {
            VmType jnodeType = null;
            try {
                jnodeType = loaderContext.loadClass(cname, true);
            } catch (ClassNotFoundException ex) {
                throw new JNodeClassNotFoundException(cname);
            }
            info = new FieldInfo(jdkType, jnodeType);
            fieldInfos.put(cname, info);
        }
        return info;
    }
}
