/**
 * $Id$
 */
package org.jnode.vm.classmgr;

/**
 * Entry of a constantpool describing a method reference.
 * 
 * @author epr
 */
public class VmConstMethodRef extends VmConstMemberRef {

	/** The resolved method */
	private VmMethod vmMethod;

	/**
	 * Constructor for VmMethodRef.
	 * @param cp
	 * @param classIndex
	 * @param nameTypeIndex
	 */
	public VmConstMethodRef(VmCP cp, int classIndex, int nameTypeIndex) {
		super(cp, classIndex, nameTypeIndex);
	}

	/**
	 * Resolve the references of this constant to loaded VmXxx objects.
	 * @param clc
	 */
	protected void doResolveMember(AbstractVmClassLoader clc) {
		final VmType vmClass = getConstClass().getResolvedVmClass();
		if (vmClass.isInterface()) {
			throw new IncompatibleClassChangeError(getClassName() + " must be a class");
		}
		final VmMethod vmMethod = vmClass.getMethod(getName(), getSignature());	
		if (vmMethod == null) {
			throw new NoSuchMethodError(toString() + " in class " + getClassName());
		}
		if (vmMethod.isAbstract() && !vmClass.isAbstract()) {
			throw new AbstractMethodError("Abstract method " + toString() + " in class " + getClassName());
		}
		this.vmMethod = vmMethod;
	}
	
	/**
	 * Returns the resolved method.
	 * @return VmMethod
	 */
	public VmMethod getResolvedVmMethod() {
		if (vmMethod == null) {
			throw new RuntimeException("vmMethod is not yet resolved");
		} else {
			return vmMethod;
		}
	}
	
	/**
	 * Sets the resolved vmMethod. If the resolved vmMethod was already set,
	 * any call to this method is silently ignored.
	 * @param vmMethod The vmMethod to set
	 */
	public void setResolvedVmMethod(VmMethod vmMethod) {
		if (this.vmMethod == null) {
			this.vmMethod = vmMethod;
		}
	}

}
