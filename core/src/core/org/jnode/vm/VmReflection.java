/**
 * $Id$
 */
package org.jnode.vm;

import java.lang.reflect.InvocationTargetException;

import org.jnode.vm.classmgr.VmField;
import org.jnode.vm.classmgr.VmInstanceField;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmStaticField;
import org.jnode.vm.classmgr.VmType;

/**
 * <description>
 * 
 * @author epr
 */
public class VmReflection {
	
	public static Object getObject(VmField field, Object o) {
		if (field.isStatic()) {
			final VmStaticField sf = (VmStaticField)field;
			initialize(sf);
			return Unsafe.getObject(getStaticFieldAddress(sf));
		} else {
			final VmInstanceField inf = (VmInstanceField)field;
			return Unsafe.getObject(o, inf.getOffset());
		}
	}
	
	public static boolean getBoolean(VmField field, Object o) {
		if (field.isStatic()) {
			final VmStaticField sf = (VmStaticField)field;
			initialize(sf);
			return (Unsafe.getInt(getStaticFieldAddress(sf)) != 0);
		} else {
			final VmInstanceField inf = (VmInstanceField)field;
			return Unsafe.getBoolean(o, inf.getOffset());
		}
	}
	
	public static byte getByte(VmField field, Object o) {
		if (field.isStatic()) {
			final VmStaticField sf = (VmStaticField)field;
			initialize(sf);
			return (byte)Unsafe.getInt(getStaticFieldAddress(sf));
		} else {
			final VmInstanceField inf = (VmInstanceField)field;
			return Unsafe.getByte(o, inf.getOffset());
		}
	}
	
	public static char getChar(VmField field, Object o) {
		if (field.isStatic()) {
			final VmStaticField sf = (VmStaticField)field;
			initialize(sf);
			return (char)Unsafe.getInt(getStaticFieldAddress(sf));
		} else {
			final VmInstanceField inf = (VmInstanceField)field;
			return Unsafe.getChar(o, inf.getOffset());
		}
	}
	
	public static short getShort(VmField field, Object o) {
		if (field.isStatic()) {
			final VmStaticField sf = (VmStaticField)field;
			initialize(sf);
			return (short)Unsafe.getInt(getStaticFieldAddress(sf));
		} else {
			final VmInstanceField inf = (VmInstanceField)field;
			return Unsafe.getShort(o, inf.getOffset());
		}
	}
	
	public static int getInt(VmField field, Object o) {
		if (field.isStatic()) {
			final VmStaticField sf = (VmStaticField)field;
			initialize(sf);
			return Unsafe.getInt(getStaticFieldAddress(sf));
		} else {
			final VmInstanceField inf = (VmInstanceField)field;
			return Unsafe.getInt(o, inf.getOffset());
		}
	}
	
	public static float getFloat(VmField field, Object o) {
		if (field.isStatic()) {
			final VmStaticField sf = (VmStaticField)field;
			initialize(sf);
			return Unsafe.getFloat(getStaticFieldAddress(sf));
		} else {
			final VmInstanceField inf = (VmInstanceField)field;
			return Unsafe.getFloat(o, inf.getOffset());
		}
	}
	
	public static long getLong(VmField field, Object o) {
		if (field.isStatic()) {
			final VmStaticField sf = (VmStaticField)field;
			initialize(sf);
			return Unsafe.getLong(getStaticFieldAddress(sf));
		} else {
			final VmInstanceField inf = (VmInstanceField)field;
			return Unsafe.getLong(o, inf.getOffset());
		}
	}
	
	public static double getDouble(VmField field, Object o) {
		if (field.isStatic()) {
			final VmStaticField sf = (VmStaticField)field;
			initialize(sf);
			return Unsafe.getDouble(getStaticFieldAddress(sf));
		} else {
			final VmInstanceField inf = (VmInstanceField)field;
			return Unsafe.getDouble(o, inf.getOffset());
		}
	}
	
	public static void setObject(VmField field, Object o, Object value) {
		if (field.isStatic()) {
			final VmStaticField sf = (VmStaticField)field;
			initialize(sf);
			Unsafe.setObject(getStaticFieldAddress(sf), value);
		} else {
			final VmInstanceField inf = (VmInstanceField)field;
			Unsafe.setObject(o, inf.getOffset(), value);
		}
	}
	
	public static void setBoolean(VmField field, Object o, boolean value) {
		if (field.isStatic()) {
			final VmStaticField sf = (VmStaticField)field;
			initialize(sf);
			Unsafe.setInt(getStaticFieldAddress(sf), value ? 1 : 0);
		} else {
			final VmInstanceField inf = (VmInstanceField)field;
			Unsafe.setBoolean(o, inf.getOffset(), value);
		}
	}
	
	public static void setByte(VmField field, Object o, byte value) {
		if (field.isStatic()) {
			final VmStaticField sf = (VmStaticField)field;
			initialize(sf);
			Unsafe.setInt(getStaticFieldAddress(sf), value);
		} else {
			final VmInstanceField inf = (VmInstanceField)field;
			Unsafe.setByte(o, inf.getOffset(), value);
		}
	}
	
	public static void setChar(VmField field, Object o, char value) {
		if (field.isStatic()) {
			final VmStaticField sf = (VmStaticField)field;
			initialize(sf);
			Unsafe.setInt(getStaticFieldAddress(sf), value);
		} else {
			final VmInstanceField inf = (VmInstanceField)field;
			Unsafe.setChar(o, inf.getOffset(), value);
		}
	}
	
	public static void setShort(VmField field, Object o, short value) {
		if (field.isStatic()) {
			final VmStaticField sf = (VmStaticField)field;
			initialize(sf);
			Unsafe.setInt(getStaticFieldAddress(sf), value);
		} else {
			final VmInstanceField inf = (VmInstanceField)field;
			Unsafe.setShort(o, inf.getOffset(), value);
		}
	}
	
	public static void setInt(VmField field, Object o, int value) {
		if (field.isStatic()) {
			final VmStaticField sf = (VmStaticField)field;
			initialize(sf);
			Unsafe.setInt(getStaticFieldAddress(sf), value);
		} else {
			final VmInstanceField inf = (VmInstanceField)field;
			Unsafe.setInt(o, inf.getOffset(), value);
		}
	}
	
	public static void setFloat(VmField field, Object o, float value) {
		if (field.isStatic()) {
			final VmStaticField sf = (VmStaticField)field;
			initialize(sf);
			Unsafe.setFloat(getStaticFieldAddress(sf), value);
		} else {
			final VmInstanceField inf = (VmInstanceField)field;
			Unsafe.setFloat(o, inf.getOffset(), value);
		}
	}
	
	public static void setLong(VmField field, Object o, long value) {
		if (field.isStatic()) {
			final VmStaticField sf = (VmStaticField)field;
			initialize(sf);
			Unsafe.setLong(getStaticFieldAddress(sf), value);
		} else {
			final VmInstanceField inf = (VmInstanceField)field;
			Unsafe.setLong(o, inf.getOffset(), value);
		}
	}
	
	public static void setDouble(VmField field, Object o, double value) {
		if (field.isStatic()) {
			final VmStaticField sf = (VmStaticField)field;
			initialize(sf);
			Unsafe.setDouble(getStaticFieldAddress(sf), value);
		} else {
			final VmInstanceField inf = (VmInstanceField)field;
			Unsafe.setDouble(o, inf.getOffset(), value);
		}
	}
	
	/**
	 * Gets the address of the static field data (in the statics table)
	 * @param sf
	 * @return The address of the static field data
	 */
	private static final Address getStaticFieldAddress(VmStaticField sf) {
		final VmProcessor proc = Unsafe.getCurrentProcessor();
		final Address tablePtr = Address.addressOfArrayData(proc.getStaticsTable());
		final int offset = sf.getStaticsIndex() << 2;
		return Unsafe.add(tablePtr, offset);
	}
	
	/**
	 * Invoke the given method, which must be static and have no arguments
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
	 * @param method
	 * @param o
	 * @param args
	 * @return Object
	 * @throws InvocationTargetException
	 */
	public static Object invoke(VmMethod method, Object o, Object[] args) 
	throws InvocationTargetException {
		int argCount = method.getNoArguments();
		int argsLength = (args == null) ? 0 : args.length;
		if (argCount != argsLength) {
			throw new IllegalArgumentException("Invalid number of arguments");
		}
		
		if (!method.isStatic()) {
			Unsafe.pushObject(o);
		}
		for (int i = 0; i < argCount; i++) {
			final VmType argType = method.getArgumentType(i);
			final Object arg = args[i];
			if (argType.isPrimitive()) {
				int v = 0;
				long lv = 0;
				boolean wide = false;
				if (arg == null) {
					/* do nothing */
				} else if (arg instanceof Boolean) {
					v = ((Boolean)arg).booleanValue() ? 1 : 0;
				} else if (arg instanceof Byte) {
					v = ((Byte)arg).byteValue();
				} else if (arg instanceof Character) {
					v = ((Character)arg).charValue();
				} else if (arg instanceof Short) {
					v = ((Short)arg).shortValue();
				} else if (arg instanceof Integer) {
					v = ((Integer)arg).intValue();
				} else if (arg instanceof Long) {
					lv = ((Long)arg).longValue();
					wide = true;
				} else if (arg instanceof Float) {
					v = Float.floatToRawIntBits(((Float)arg).floatValue());
				} else if (arg instanceof Double) {
					lv = Double.doubleToRawLongBits(((Double)arg).doubleValue());
					wide = true;
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
						Unsafe.pushInt((int)lv);
					} else {
						Unsafe.pushInt(v);
					}					
				}
			} else {
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
				final Class retType = method.getReturnType().asClass();
				if (Long.TYPE == retType) {
					return new Long(rc);
				} else {
					return new Double(Double.longBitsToDouble(rc));
				}
			} else {
				int rc = Unsafe.invokeInt(method);
				final Class retType = method.getReturnType().asClass();
				if (Byte.TYPE == retType) {
					return new Byte((byte)rc);
				} else if (Boolean.TYPE == retType) {
					return Boolean.valueOf(rc != 0);
				} else if (Character.TYPE == retType) {
					return new Character((char)rc);
				} else if (Short.TYPE == retType) {
					return new Short((short)rc);
				} else if (Float.TYPE == retType) {
					return new Float(Float.intBitsToFloat(rc));
				} else {
					return new Integer(rc);
				}
			}
		} catch (Throwable ex) {
			throw new InvocationTargetException(ex);
		}
	}
	
	/**
	 * Create and return a new object using the given constructor and arguments
	 * @param constructor
	 * @param args
	 * @return Object
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static Object newInstance(VmMethod constructor, Object[] args) throws InstantiationException, IllegalAccessException, InvocationTargetException {
		final VmHeapManager hm = Vm.getVm().getHeapManager();
		final Object obj = hm.newInstance(constructor.getDeclaringClass());
		invoke(constructor, obj, args);
		return obj;
	}
	
	/**
	 * Create and return a new object using the given constructor no arguments
	 * @param constructor
	 * @return Object
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static Object newInstance(VmMethod constructor) throws InstantiationException, IllegalAccessException, InvocationTargetException {
		final VmHeapManager hm = Vm.getVm().getHeapManager();
		final Object obj = hm.newInstance(constructor.getDeclaringClass());
		Unsafe.pushObject(obj);
		Unsafe.invokeVoid(constructor);
		return obj;
	}

	/**
	 * Initialize the class that declared this field if needed.
	 * @param sf
	 */
	private static void initialize(VmStaticField sf) {
		final VmType declClass = sf.getDeclaringClass();
		if (!declClass.isInitialized()) {
			if (!(sf.isPrimitive() && sf.isFinal())) {
				sf.getDeclaringClass().initialize();
			}
		}
	}
}
