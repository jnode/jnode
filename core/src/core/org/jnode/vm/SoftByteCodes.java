/**
 * $Id$
 */
package org.jnode.vm;

import org.jnode.util.NumberUtils;
import org.jnode.vm.classmgr.AbstractVmClassLoader;
import org.jnode.vm.classmgr.VmArrayClass;
import org.jnode.vm.classmgr.VmConstClass;
import org.jnode.vm.classmgr.VmConstFieldRef;
import org.jnode.vm.classmgr.VmConstMethodRef;
import org.jnode.vm.classmgr.VmField;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;

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
	public static final int EX_CLASSCAST = 6;
	
	private static VmHeapManager heapManager;

	/**
	 * Is the given object instance of the given class.
	 * @param object
	 * @param T
	 * @return boolean
	 * @throws PragmaUninterruptible
	 */
	public static boolean isInstanceof(Object object, VmType T) 
	throws PragmaUninterruptible {
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
	 * @param currentMethod
	 * @param fieldRef
	 * @param isStatic
	 * @return VmField
	 * @throws PragmaUninterruptible
	 */
	public static VmField resolveField(VmMethod currentMethod, VmConstFieldRef fieldRef, boolean isStatic) 
	throws PragmaUninterruptible {
		if (!fieldRef.getConstClass().isResolved()) {
			resolveClass(fieldRef.getConstClass());
		}
		VmField result;
		if (fieldRef.isResolved()) {
			result = fieldRef.getResolvedVmField();
		} else {
			VmType vmClass = fieldRef.getConstClass().getResolvedVmClass();
			vmClass.prepare();
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
	 * @param currentMethod
	 * @param methodRef
	 * @return VmMethod
	 * @throws PragmaUninterruptible
	 */
	public static VmMethod resolveMethod(VmMethod currentMethod, VmConstMethodRef methodRef) 
	throws PragmaUninterruptible {
		if (!methodRef.getConstClass().isResolved()) {
			resolveClass(methodRef.getConstClass());
		}
		if (methodRef.isResolved()) {
			return methodRef.getResolvedVmMethod();
		} else {
			VmType vmClass = methodRef.getConstClass().getResolvedVmClass();
			vmClass.prepare();
			
			// NEW
			AbstractVmClassLoader curLoader = currentMethod.getDeclaringClass().getLoader();
			methodRef.resolve(curLoader);
			return methodRef.getResolvedVmMethod();
			// END NEW 
			/*VmMethod method = vmClass.getMethod(methodRef);
			if (method == null) {
				String mname = methodRef.getName();
				String cname = methodRef.getClassName();
				Screen.debug("method not found ");
				Screen.debug(mname);
				Screen.debug(" in ");
				Screen.debug(cname);
				throw new NoSuchMethodError(cname);
			}

			methodRef.setResolvedVmMethod(method);
			return method;*/
		}
	}

	/**
	 * Resolve a const reference to a class to the actual class, in the context
	 * of the given current method.
	 * @param currentMethod
	 * @param classRef
	 * @return VmClass
	 * @throws PragmaUninterruptible
	 */
	public static VmType resolveClass(VmConstClass classRef) 
	throws PragmaUninterruptible {
		if (classRef.isResolved()) {
			return classRef.getResolvedVmClass();
		} else {
			AbstractVmClassLoader curLoader = VmSystem.getContextClassLoader();
			String cname = classRef.getClassName();
			try {
				Class cls = curLoader.asClassLoader().loadClass(cname);
				VmType vmClass = cls.getVmClass();
				
				/*VmClass vmClass = curLoader.loadClass(cname, true);
				//VmClass vmClass = Main.getBootClass(classRef);
				if (vmClass == null) {
					throw new NoClassDefFoundError(cname);
				}*/
				classRef.setResolvedVmClass(vmClass);
				return vmClass;
			} catch (ClassNotFoundException ex) {
				//ex.printStackTrace();
				//Unsafe.debug("resolve::CLASSNOTFOUND");
				throw new NoClassDefFoundError(cname);
			}
		}
	}

	/**
	 * Allocate a new object with a given class and a given size in bytes.
	 * If size &lt; 0, the objectsize from the given class is used. The given
	 * size does not include the length of the object header.
	 * @param vmClass
	 * @param size
	 * @return Object The new object
	 * @throws PragmaUninterruptible
	 */
	public static Object allocObject(VmType vmClass, int size) 
	throws PragmaUninterruptible {
		vmClass.prepare();

		//Screen.debug("ao cls{");
		//Screen.debug(vmClass.getName());

		VmHeapManager hm = heapManager;
		if (hm == null) {
			heapManager = hm = Vm.getVm().getHeapManager();
		}
		Object result;
		if (size < 0) {
			result = hm.newInstance(vmClass);
		} else {
			result = hm.newInstance(vmClass, size);
		}
		//Screen.debug("}");

		return result;
	}

	/**
	 * Allocate a multi dimensional array
	 * @param vmClass
	 * @param dimensions
	 * @return The allocated array
	 * @throws PragmaUninterruptible
	 */
	public static Object allocMultiArray(VmType vmClass, int[] dimensions) 
	throws PragmaUninterruptible {
		//Syslog.debug("allocMultiArray "); // + vmClass);
		return multinewarray_helper(dimensions, dimensions.length-1, (VmArrayClass)vmClass);
	}

	/**
	 * Allocates a multidimensional array of type a, with dimensions given in
	 * dims[ind] to dims[dims.length-1].  a must be of dimensionality at least
	 * dims.length-ind.
	 *
	 * @return allocated array object
	 * @param dims array of dimensions in reverse order
	 * @param ind start index in array dims
	 * @param a array type
	 * @throws NegativeArraySizeException if one of the array sizes in dims is negative
	 * @throws OutOfMemoryError if there is not enough memory to perform operation
	 * @throws PragmaUninterruptible
	 */    
	public static Object multinewarray_helper(int[] dims, int ind, VmArrayClass a)
	throws OutOfMemoryError, NegativeArraySizeException, PragmaUninterruptible {
		//Syslog.debug("multinewarray_helper "); //+ " cls=" + a);
		a.initialize();
		final int length = dims[ind];
		final Object o = allocArray(a, length);
		if (ind == 0) {
			return o;
		}
		final Object[] o2 = (Object[])o;
		final VmArrayClass a2 = (VmArrayClass)a.getComponentType();
		a2.initialize();
		for (int i=0; i<length; ++i) {
			o2[i] = multinewarray_helper(dims, ind-1, a2);
		}
		return o2;
	}

	/**
	 * Allocate a new array with a given class as component type and a given
	 * number of elements.
	 * @param currentMethod
	 * @param vmClass
	 * @param elements
	 * @return Object The new array
	 * @throws PragmaUninterruptible
	 */
	public static Object anewarray(VmMethod currentMethod, VmType vmClass, int elements) 
	throws PragmaUninterruptible {

		VmType arrCls;
		try {
			AbstractVmClassLoader curLoader = currentMethod.getDeclaringClass().getLoader();
			arrCls = curLoader.loadClass(vmClass.getArrayClassName(), true);
			//Screen.debug("an cls{");
			//Screen.debug(vmClass.getName());
			if (arrCls == null) {
				throw new NoClassDefFoundError();
			}
		} catch (ClassNotFoundException ex) {
			throw new NoClassDefFoundError();
		}

		VmHeapManager hm = heapManager;
		if (hm == null) {
			heapManager = hm = Vm.getVm().getHeapManager();
		}
		final Object result = hm.newArray((VmArrayClass)arrCls, elements);

		//Screen.debug("}");
		return result;
	}

	/**
	 * Allocate a new primivite array with a given arraytype and a given number of elements.
	 * @param atype
	 * @param elements
	 * @return Object The new array
	 * @throws PragmaUninterruptible
	 */
	public static Object allocPrimitiveArray(int atype, int elements) 
	throws PragmaUninterruptible {
		VmHeapManager hm = heapManager;
		if (hm == null) {
			heapManager = hm = Vm.getVm().getHeapManager();
		}
		final Object result = hm.newArray(VmType.getPrimitiveArrayClass(atype), elements);
		return result;
	}

	/**
	 * Allocate a new array with a given class and a given number of elements.
	 * @param vmClass
	 * @param elements
	 * @return Object The new array
	 * @throws PragmaUninterruptible
	 */
	public static Object allocArray(VmType vmClass, int elements) 
	throws PragmaUninterruptible {
		VmHeapManager hm = heapManager;
		if (hm == null) {
			heapManager = hm = Vm.getVm().getHeapManager();
		}
		final Object result = hm.newArray((VmArrayClass)vmClass, elements);
		return result;
	}

	/**
	 * Create an exception for a system-trapped situation.
	 * @param nr
	 * @param address
	 * @return Throwable
	 * @throws PragmaUninterruptible
	 */
	public static Throwable systemException(int nr, int address) 
	throws PragmaUninterruptible {
		//Unsafe.getCurrentProcessor().getArchitecture().getStackReader().debugStackTrace();
		String hexAddress = NumberUtils.hex(address, 8);
		switch (nr) {
			case EX_NULLPOINTER: return new NullPointerException("NPE at address " + hexAddress);
			case EX_PAGEFAULT: return new InternalError("Page fault at " + hexAddress);
			case EX_INDEXOUTOFBOUNDS: return new ArrayIndexOutOfBoundsException("Out of bounds at index " + address);
			case EX_DIV0: return new ArithmeticException("Division by zero at address " + hexAddress);
			case EX_ABSTRACTMETHOD: return new AbstractMethodError("Abstract method at " + hexAddress);
			case EX_STACKOVERFLOW: return new StackOverflowError();
			case EX_CLASSCAST: return new ClassCastException();
			default: return new UnknownError("Unknown system-exception at " + hexAddress);
		}
	}
	
	public static void unknownOpcode(int opcode, int pc) 
	throws PragmaUninterruptible {
		throw new Error("Unknown opcode " + opcode + " at pc " + pc);
	}
}
