/**
 * $Id$
 */
package org.jnode.vm.classmgr;

/**
 * Entry of a constantpool describing an interface method reference.
 * 
 * @author epr
 */
public final class VmConstIMethodRef extends VmConstMethodRef {

	/** The selector of this methods name&type */
	private int selector = -1;

	/**
	 * Constructor for VmIMethodRef.
	 * @param cp
	 * @param classIndex
	 * @param nameTypeIndex
	 */
	public VmConstIMethodRef(VmConstClass constClass, String name, String descriptor) {
		super(constClass, name, descriptor);
	}

	/**
	 * Resolve the references of this constant to loaded VmXxx objects.
	 * @param clc
	 */
	protected void doResolveMember(VmClassLoader clc) {
		final VmType vmClass = getConstClass().getResolvedVmClass();
		if (!vmClass.isInterface()) {
			throw new IncompatibleClassChangeError(getClassName() + " must be an interface");
		}
		final VmMethod vmMethod;
		vmMethod = vmClass.getMethod(getName(), getSignature());	
		if (vmMethod == null) {
			throw new NoSuchMethodError(toString() + " in class " + getClassName());
		}
		this.selector = vmMethod.getSelector();
		setResolvedVmMethod(vmMethod);
	}
	
	/** 
	 * Gets the selector of this methods name&amp;type
	 * @return int
	 */
	public int getSelector() {
		return selector;
	}
}
