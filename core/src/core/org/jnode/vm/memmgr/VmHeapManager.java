/*
 * $Id$
 */
package org.jnode.vm.memmgr;

import org.jnode.vm.Address;
import org.jnode.vm.Unsafe;
import org.jnode.vm.VmSystemObject;
import org.jnode.vm.classmgr.VmArray;
import org.jnode.vm.classmgr.VmArrayClass;
import org.jnode.vm.classmgr.VmClassType;
import org.jnode.vm.classmgr.VmNormalClass;
import org.jnode.vm.classmgr.VmType;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class VmHeapManager extends VmSystemObject {

	/** The boot heap */
	protected final VmBootHeap bootHeap;
	/** Has this manager been initialized yet */
	private boolean inited = false;
	protected OutOfMemoryError OOME;
	protected final HeapHelper helper;
	
	/**
	 * Initialize this instance
	 */
	public VmHeapManager(HeapHelper helper) {
		this.bootHeap = new VmBootHeap(helper);
		this.helper = helper;
	}
	
	/**
	 * Initialize this manager.
	 * This method is called before the first call to allocObject.
	 */
	protected abstract void initialize();
	
	/**
	 * Start any threads needed by this manager.
	 */
	public abstract void start();
	
	/**
	 * Create a new instance of a given class
	 * 
	 * @param cls
	 * @return The new instance
	 */
	public final Object newInstance(VmType cls) {
		cls.link();
		return newInstance(cls, ((VmNormalClass)cls).getObjectSize());
	}

	/**
	 * Create a new instance of a given class with a given object size (in bytes)
	 * 
	 * @param cls
	 * @param size
	 * @return The new instance
	 */
	public final Object newInstance(VmType cls, int size) {
		testInited();
		if (cls.isArray()) {
			throw new IllegalArgumentException("Cannot instantiate an array like this");
		}
		if (cls.isInterface()) {
			throw new IllegalArgumentException("Cannot instantiate an interface");
		}
		
		final Object obj = allocObject((VmNormalClass) cls, size);
		if (obj == null) {
			Unsafe.debug("Out of memory");
			throw OOME;
		}
		return obj;
	}

	/**
	 * Create a new array
	 * 
	 * @param arrayCls
	 * @param elements
	 * @return The new instance
	 */
	public final Object newArray(VmArrayClass arrayCls, int elements) {
		testInited();
		if (elements < 0) {
			throw new NegativeArraySizeException("elements must be greater or equal to 0");
		}
		if (!arrayCls.isArray()) {
			throw new IllegalArgumentException("Cannot instantiate a non-array like this [" + arrayCls.getName() + "]");
		}

		final int slotSize = Unsafe.getCurrentProcessor().getArchitecture().getReferenceSize();
		final int elemSize;
		if (arrayCls.isPrimitiveArray()) {
			switch (arrayCls.getSecondNameChar()) {
				case 'B' : // byte
				case 'Z' : // boolean
					elemSize = 1;
					break;
				case 'C' : // char
				case 'S' : // short
					elemSize = 2;
					break;
				case 'I' : // int
				case 'F' : // float
					elemSize = 4;
					break;
				case 'D' : // double
				case 'J' : // long
					elemSize = 8;
					break;
				default :
					throw new IllegalArgumentException(arrayCls.getName());
			}
		} else {
			elemSize = slotSize;
		}

		final Object obj = newArray0(arrayCls, elemSize, elements, slotSize);
		if (obj == null) {
			Unsafe.debug("Out of memory");
			throw OOME;
		}

		return obj;
	}

	/**
	 * Is the system low on memory?
	 * 
	 * @return boolean
	 */
	public abstract boolean isLowOnMemory();

	/**
	 * Start a garbage collection process
	 */
	public abstract void gc();

	/**
	 * Create an exact clone of the given object
	 * 
	 * @param object
	 * @return Object
	 */
	public final Object clone(Object object) {
		testInited();
		final VmClassType objectClass = helper.getVmClass(object);
		final Address objectAddr = helper.addressOf(object);
		final int size;
		if (objectClass.isArray()) {
			final int slotSize = Unsafe.getCurrentProcessor().getArchitecture().getReferenceSize();
			final VmArrayClass arrayClass = (VmArrayClass) objectClass;
			final int length = helper.getInt(object, VmArray.LENGTH_OFFSET * slotSize);
			final int elemSize = arrayClass.getComponentType().getTypeSize();
			size = (VmArray.DATA_OFFSET * slotSize) * (length * elemSize);
		} else {
			final VmNormalClass normalClass = (VmNormalClass) objectClass;
			size = normalClass.getObjectSize();
		}
		final Object newObj = allocObject(objectClass, size);
		helper.copy(objectAddr, helper.addressOf(newObj), size);
		return newObj;
	}

	/**
	 * Gets the size of free memory in bytes.
	 * 
	 * @return long
	 */
	public abstract long getFreeMemory();

	/**
	 * Gets the size of all memory in bytes.
	 * 
	 * @return the size of all memory in bytes
	 */
	public abstract long getTotalMemory();

	/**
	 * Allocate a new instance for the given class. Not that this method cannot be synchronized,
	 * since obtaining a monitor might require creating one, which in turn needs this method.
	 * 
	 * @param vmClass
	 * @param size
	 * @return Object
	 */
	protected abstract Object allocObject(VmClassType vmClass, int size);

	/**
	 * Create a new instance of an array with a given class, no constructor will be called, the
	 * object will be filled with zeros.
	 * 
	 * @param vmClass
	 * @param elemSize
	 *            The length in bytes of each element
	 * @param elements
	 *            The number of elements
	 * @param slotSize
	 * @return The new instance
	 */
	private final Object newArray0(VmClassType vmClass, int elemSize, int elements, int slotSize) {
		final int size = (VmArray.DATA_OFFSET * slotSize) + (elemSize * elements);
		final Object array = allocObject(vmClass, size);
		final Address arrayPtr = helper.addressOf(array);
		helper.setInt(arrayPtr, (VmArray.LENGTH_OFFSET * slotSize), elements);
		return array;
	}
	
	private final void testInited() {
		if (!inited) {
			//Unsafe.debug("testInitid.initialize");
			initialize();
			inited = true;
			OOME = new OutOfMemoryError();
			//Unsafe.debug("eo-testInitid.initialize");
		}
	}
}
