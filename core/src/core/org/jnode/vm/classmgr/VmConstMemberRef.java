/**
 * $Id$
 */
package org.jnode.vm.classmgr;


/**
 * Abstract entry of a constantpool describing an member (method or field) reference.
 * 
 * @author epr
 */
abstract class VmConstMemberRef extends VmConstObject {
	
	private final int cachedHashCode;
	private final VmConstClass constClass;
	private final String name;
	private final String descriptor;
	
	public VmConstMemberRef(VmConstClass constClass, String name, String descriptor) {
		this.constClass = constClass;
		this.name = name;
		this.descriptor = descriptor;
		this.cachedHashCode = VmMember.calcHashCode(name, descriptor);
	}
	
	/**
	 * Gets the ConstClass this member constants refers to.
	 * @return VmConstClass
	 */
	public final VmConstClass getConstClass() {
		return constClass;
	}
	
	/**
	 * Gets the name of the class of the members this constants refers to.
	 * @return String
	 */
	public final String getClassName() {
		return getConstClass().getClassName();
	}
	
	/**
	 * Gets the name of the members this constants refers to.
	 * @return String
	 */
	public final String getName() {
		return name;
	}
	
	/**
	 * Gets the type descriptor of the members this constants refers to.
	 * @return String
	 */
	public final String getSignature() {
		return descriptor;
	}
	
	/**
	 * Resolve the references of this constant to loaded VmXxx objects.
	 * @param clc
	 */
	protected final void doResolve(VmClassLoader clc) {
		getConstClass().resolve(clc);
		doResolveMember(clc);
	}

	/**
	 * Resolve the references of this constant to loaded VmXxx objects.
	 * @param clc
	 */
	protected abstract void doResolveMember(VmClassLoader clc);

	/**
	 * Convert myself into a String representation 
	 * @see java.lang.Object#toString()
	 * @return String
	 */
	public final String toString() {
		String type = getClass().getName();
		type = type.substring(type.lastIndexOf('.') + 1 + 2);
		return type + ": " + getClassName() + "." + getName() + " [" + getSignature() + "]";
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 * @return int
	 */
	public final int getMemberHashCode() {
		return cachedHashCode;
	}
}
