/*
 * $Id$
 */
package org.jnode.vm.classmgr;

import java.util.HashSet;
import java.util.Iterator;

/**
 * Class structure for array classes.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VmArrayClass extends VmClassType {

	/** The type of elements in an array class */
	private final VmType componentType;

	/**
	 * @param name
	 * @param loader
	 * @param primitive
	 * @param componentType
	 * @param typeSize
	 */
	VmArrayClass(String name, VmClassLoader loader, boolean primitive, VmType componentType, int typeSize) {
		super(name, getObjectClass(), loader, primitive, typeSize);
		this.componentType = componentType;
		testClassType();
	}

	/**
	 * Returns the componentType.
	 * 
	 * @return VmClass
	 */
	public VmType getComponentType() {
		return componentType;
	}

	/**
	 * Is this class an array of primitive types
	 * @return boolean
	 */
	public final boolean isPrimitiveArray() {
		if (isArray()) {
			return componentType.isPrimitive();
		} else {
			return false;
		}
	}

	/**
	 * Test if this class is using the right modifiers
	 * @throws RuntimeException
	 */
	private final void testClassType() throws RuntimeException {
		if (!isArray()) {
			throw new RuntimeException("Not an array class");
		}
		if (isInterface()) {
			throw new RuntimeException("Not an array class (interface-class)");
		}
	}

	/**
	 * @see org.jnode.vm.classmgr.VmType#prepareForInstantiation()
	 */
	protected void prepareForInstantiation() {
		// Nothing to do here
	}

	/**
	 * Create the list of super classes for this class.
	 * 
	 * @param allInterfaces
	 * @see org.jnode.vm.classmgr.VmType#createSuperClassesArray(java.util.HashSet)
	 * @return Super classes
	 */
	protected VmType[] createSuperClassesArray(HashSet allInterfaces) {

		final VmType[] compSuperClasses = componentType.getSuperClassesArray();
		final int compLength = compSuperClasses.length;

		final int length = compLength + 2 + allInterfaces.size();
		final VmType[] array = new VmType[length];
		array[0] = this;
		array[1] = this.getSuperClass();
		for (int i = 0; i < compLength; i++) {
			array[2 + i] = compSuperClasses[i].getArrayClass(false);
		}

		int index = compLength + 2;
		for (Iterator i = allInterfaces.iterator(); i.hasNext();) {
			final VmInterfaceClass intfClass = (VmInterfaceClass) i.next();
			array[index++] = intfClass;
		}

		return array;
	}

	/**
	 * @see org.jnode.vm.classmgr.VmType#prepare()
	 */
	void prepare() {
		componentType.prepare();
		super.prepare();
	}

	/**
	 * @see org.jnode.vm.classmgr.VmType#compile()
	 */
	void compile() {
		componentType.compile();
		super.compile();
	}

	/**
	 * @see org.jnode.vm.classmgr.VmType#verify()
	 */
	void verify() {
		componentType.verify();
		super.verify();
	}

	public final boolean isArray() {
		return true;
	}
}
