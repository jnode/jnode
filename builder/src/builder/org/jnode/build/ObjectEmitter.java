/**
 * $Id$
 */

package org.jnode.build;

import java.io.PrintWriter;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;

import org.jnode.assembler.BootImageNativeStream;
import org.jnode.assembler.NativeStream;
import org.jnode.assembler.x86.X86Stream;
import org.jnode.vm.BootableObject;
import org.jnode.vm.VmSystemObject;
import org.jnode.vm.classmgr.AbstractVmClassLoader;
import org.jnode.vm.classmgr.VmClassType;
import org.jnode.vm.classmgr.VmType;

public class ObjectEmitter {

	private final AbstractVmClassLoader loaderContext;
	private final NativeStream os;
	private final BootImageNativeStream bis;
	private final PrintWriter debugWriter;
	private final Set legalInstanceClasses;

	/**
	 * Construct a new ObjectEmitter *
	 * 
	 * @param b
	 * @param os
	 * @param debug
	 * @param legalInstanceClasses
	 */
	public ObjectEmitter(AbstractVmClassLoader b, NativeStream os, PrintWriter debug, Set legalInstanceClasses) {
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
		testForValidEmit(obj);

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
			debugWriter.println();
		}

		if (obj instanceof VmSystemObject) {
			((VmSystemObject) obj).verifyBeforeEmit();
		}

		// Writeout the header
		final VmClassType vmClass = (VmClassType) loaderContext.loadClass(cls.getName(), true);
		vmClass.incInstanceCount();
		final X86Stream.ObjectInfo oInfo = os.startObject(vmClass);
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
			emitArray(cls, obj);
		} else {
			emitObject(cls, obj);
		}
		oInfo.markEnd();
	}

	/**
	 * Is it legal to emit the given object?
	 * 
	 * @param object
	 * @throws BuildException
	 *             Is if not valid to emit the given object into the boot image.
	 */
	private final void testForValidEmit(Object object) throws BuildException {
		if (object == null) {
			return;
		} else if (object instanceof BootableObject) {
			return;
		} else if (object.getClass().isArray()) {
			return;
		} else {
			final String clsName = object.getClass().getName().replace('/', '.');
			/*
			 * if (clsName.startsWith("org.jnode.")) { return;
			 */
			if (legalInstanceClasses.contains(clsName)) {
				return;
			}
			/*
			 * final Class javaClass = object.getClass(); try { final VmClassType vmClass =
			 * (VmClassType)loaderContext.loadClass(javaClass.getName(), true);
			 * testClassCompatibility(javaClass, vmClass); System.out.println("Found compatible
			 * class " + clsName); legalInstanceClasses.add(clsName); } catch
			 * (ClassNotFoundException ex) { throw new BuildException("VmClass not found for " +
			 * clsName, ex);
			 */
			throw new BuildException("Cannot emit object of type " + clsName);
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

	private void emitArray(Class cls, Object obj) {
		final Class cmpType = cls.getComponentType();
		final int len = Array.getLength(obj);
		os.write32(len);
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

	private void emitObject(Class cls, Object obj) throws BuildException {
		final Class sCls = cls.getSuperclass();
		if (sCls != null) {
			emitObject(sCls, obj);
		}
		try {
			final Field fields[] = cls.getDeclaredFields();
			final int len = fields.length;
			AccessibleObject.setAccessible(fields, true);
			for (int i = 0; i < len; i++) {
				final Field f = fields[i];
				final int modifiers = f.getModifiers();

				if ((modifiers & Modifier.STATIC) != 0) {
					continue;
				}

				final Class fType = f.getType();
				if ((modifiers & Modifier.TRANSIENT) != 0) {
					if ((fType == long.class) || (fType == double.class)) {
						os.write64(0);
					} else {
						os.write32(0);
					}
				} else if (fType.isPrimitive()) {
					if (fType == byte.class) {
						os.write32(f.getByte(obj));
					} else if (fType == boolean.class) {
						os.write32((f.getBoolean(obj)) ? 1 : 0);
					} else if (fType == char.class) {
						os.write32(f.getChar(obj));
					} else if (fType == short.class) {
						os.write32(f.getShort(obj));
					} else if (fType == int.class) {
						os.write32(f.getInt(obj));
					} else if (fType == float.class) {
						os.write32(Float.floatToIntBits(f.getFloat(obj)));
					} else if (fType == long.class) {
						os.write64(f.getLong(obj));
					} else if (fType == double.class) {
						os.write64(Double.doubleToLongBits(f.getDouble(obj)));
					} else {
						throw new BuildException("Unknown primitive class " + fType.getName());
					}
				} else {
					final Object value = f.get(obj);
					try {
						testForValidEmit(value);
					} catch (BuildException ex) {
						throw new BuildException("Cannot emit field " + f.getName() + " of class " + cls.getName(), ex);
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
}
