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
 
package org.jnode.build;

import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Set;

import org.jnode.assembler.BootImageNativeStream;
import org.jnode.assembler.NativeStream;
import org.jnode.assembler.x86.X86BinaryAssembler;
import org.jnode.system.BootLog;
import org.jnode.vm.BootableObject;
import org.jnode.vm.VmSystemObject;
import org.jnode.vm.classmgr.VmArrayClass;
import org.jnode.vm.classmgr.VmClassLoader;
import org.jnode.vm.classmgr.VmClassType;
import org.jnode.vm.classmgr.VmField;
import org.jnode.vm.classmgr.VmType;

public class ObjectEmitter {

	private final VmClassLoader loaderContext;
	private final NativeStream os;
	private final BootImageNativeStream bis;
	private final PrintWriter debugWriter;
	private final Set legalInstanceClasses;
	private final HashMap fieldInfos = new HashMap();

	/**
	 * Construct a new ObjectEmitter *
	 * 
	 * @param b
	 * @param os
	 * @param debug
	 * @param legalInstanceClasses
	 */
	public ObjectEmitter(VmClassLoader b, NativeStream os, PrintWriter debug, Set legalInstanceClasses) {
		this.loaderContext = b;
		this.os = os;
		this.bis = (BootImageNativeStream)os;
		this.legalInstanceClasses = legalInstanceClasses;
		this.debugWriter = debug;
	}

	/**
	 * Write the header and the contents of an object to the native stream.
	 * @param obj
	 * @throws BuildException
	 * @throws ClassNotFoundException
	 */
	public final void emitObject(Object obj) throws BuildException, ClassNotFoundException {
		if (obj == null) {
			return;
		}

		final Class cls = obj.getClass();
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
		final VmClassType vmClass = (VmClassType) loaderContext.loadClass(cls.getName(), true);
		vmClass.incInstanceCount();
		final X86BinaryAssembler.ObjectInfo oInfo = os.startObject(vmClass);
		os.setObjectRef(obj);

		// If the object is a VmClass, force the loading of the
		// correspondig java.lang.Class
		if (cls.equals(VmType.class)) {
			VmType vmCls = (VmType) obj;
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
		    emitArray(cls, obj, (VmArrayClass)vmClass);
		} else {
			try {
				emitObject(cls, obj);
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
	 * @throws BuildException
	 *             Is if not valid to emit the given object into the boot image.
	 */
	public final void testForValidEmit(Object object, String location) throws BuildException, JNodeClassNotFoundException {
		if (object == null) {
			return;
		} else if (object instanceof BootableObject) {
			return;
		} else if (object.getClass().isArray()) {
			return;
		} else if (object instanceof Class) {
		    return;
		} else {
			final String clsName = object.getClass().getName().replace('/', '.');
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
			    BootLog.warn("Use of in-exact matching class ("  + clsName + ") in bootimage at " + location);
			}
		    legalInstanceClasses.add(clsName);
		    return;
			
			/*
			 * final Class javaClass = object.getClass(); try { final VmClassType vmClass =
			 * (VmClassType)loaderContext.loadClass(javaClass.getName(), true);
			 * testClassCompatibility(javaClass, vmClass); System.out.println("Found compatible
			 * class " + clsName); legalInstanceClasses.add(clsName); } catch
			 * (ClassNotFoundException ex) { throw new BuildException("VmClass not found for " +
			 * clsName, ex);
			 */
//			throw new BuildException("Cannot emit object of type " + clsName);
		}
	}
	
	private void emitClass(Class c) throws BuildException {
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
			}
		} catch (ClassNotFoundException ex) {
			throw new BuildException("emitting object: [" + c + "]", ex);
		}
	}

	private void emitString(String s) throws BuildException {
		// This layout should match the order and type of fields
		// in java.lang.String
		bis.writeObjectRef(s.toCharArray()); // char[] value
		os.write32(s.length()); // int count
		os.write32(s.hashCode()); // int cachedHashCode
		os.write32(0); // int offset
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

	private void emitArray(Class cls, Object obj, VmArrayClass vmClass) {
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

	void emitObject(Class cls, Object obj) throws BuildException, ClassNotFoundException, JNodeClassNotFoundException {
		final Class sCls = cls.getSuperclass();
		if (sCls != null) {
			emitObject(sCls, obj);
		}
		try {
		    final FieldInfo fieldInfo = getFieldInfo(cls);
			//final Field fields[] = cls.getDeclaredFields();
		    final Field[] fields = fieldInfo.getJdkInstanceFields();
			final int len = fields.length;
			//AccessibleObject.setAccessible(fields, true);
			for (int i = 0; i < len; i++) {
			    final VmField jnodeField = fieldInfo.getJNodeInstanceField(i);
				final Field jdkField = fields[i];
				final int modifiers = jnodeField.getModifiers();

				if ((modifiers & Modifier.STATIC) != 0) {
				    throw new BuildException("Static field in instance list");
				}

				if ((jdkField == null) || ((modifiers & Modifier.TRANSIENT) != 0)) {
				    if (jnodeField.isWide()) {
						os.write64(0);
				    } else if (!jnodeField.isPrimitive()) {
				    	os.writeWord(0);
					} else {
						os.write32(0);
					}
					if (debugWriter != null) {
					    debugWriter.println(jnodeField.getName() + " transient: 0");
					}
				} else if (jnodeField.isPrimitive()) {
					final Class fType = jdkField.getType();
					if (debugWriter != null) {
					    debugWriter.println(jdkField.getName() + " " + jdkField.get(obj));
					}
					if (fType == byte.class) {
						os.write32(jdkField.getByte(obj));
					} else if (fType == boolean.class) {
						os.write32((jdkField.getBoolean(obj)) ? 1 : 0);
					} else if (fType == char.class) {
						os.write32(jdkField.getChar(obj));
					} else if (fType == short.class) {
						os.write32(jdkField.getShort(obj));
					} else if (fType == int.class) {
						os.write32(jdkField.getInt(obj));
					} else if (fType == float.class) {
						os.write32(Float.floatToIntBits(jdkField.getFloat(obj)));
					} else if (fType == long.class) {
						os.write64(jdkField.getLong(obj));
					} else if (fType == double.class) {
						os.write64(Double.doubleToLongBits(jdkField.getDouble(obj)));
					} else {
						throw new BuildException("Unknown primitive class " + fType.getName());
					}
				} else {
					Object value = jdkField.get(obj);
					try {
						testForValidEmit(value, cls.getName());
					} catch (BuildException ex) {
						throw new BuildException("Cannot emit field " + jdkField.getName() + " of class " + cls.getName(), ex);
					} catch (JNodeClassNotFoundException ex) {
						BootLog.warn("JNode class not found " + ex.getMessage());
						value = null;
					}
					bis.writeObjectRef(value);
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
	 * @param jdkType
	 * @throws ClassNotFoundException
	 */
	public FieldInfo getFieldInfo(Class jdkType) throws ClassNotFoundException, JNodeClassNotFoundException {
	    final String cname = jdkType.getName();
	    FieldInfo info = (FieldInfo)fieldInfos.get(cname);
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
