/**
 * $Id$
 */
package org.jnode.vm.classmgr;


/**
 * Abstract entry of a constantpool describing an member (method or field) reference.
 * 
 * @author epr
 */
public abstract class VmConstMemberRef extends VmConstObject {
	
	private int classIndex;
	private int nameTypeIndex;
	private int cachedHashCode;
	
	public VmConstMemberRef(VmCP cp, int cpIdx, int classIndex, int nameTypeIndex) {
		super(cp, cpIdx);
		this.classIndex = classIndex;
		this.nameTypeIndex = nameTypeIndex;
	}
	
	/**
	 * Gets the ConstClass this member constants refers to.
	 * @return VmConstClass
	 */
	public VmConstClass getConstClass() {
		return cp.getConstClass(classIndex);
	}
	
	/**
	 * Gets the ConstNameType this member constants refers to.
	 * @return VmConstNameAndType
	 */
	private VmConstNameAndType getNameAndType() {
		return cp.getConstNameAndType(nameTypeIndex);
	}
	
	/**
	 * Gets the name of the class of the members this constants refers to.
	 * @return String
	 */
	public String getClassName() {
		return getConstClass().getClassName();
	}
	
	/**
	 * Gets the name of the members this constants refers to.
	 * @return String
	 */
	public String getName() {
		return getNameAndType().getName();
	}
	
	/**
	 * Gets the type descriptor of the members this constants refers to.
	 * @return String
	 */
	public String getSignature() {
		return getNameAndType().getDescriptor();
	}
	
	/**
	 * Resolve the references of this constant to loaded VmXxx objects.
	 * @param clc
	 */
	protected void doResolve(VmClassLoader clc) {
		getConstClass().resolve(clc);
		doResolveMember(clc);
		cachedHashCode = VmMember.calcHashCode(getName(), getSignature());
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
	public String toString() {
		String type = getClass().getName();
		type = type.substring(type.lastIndexOf('.') + 1 + 2);
		return type + ": " + getClassName() + "." + getName() + " [" + getSignature() + "]";
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 * @return int
	 */
	public int getMemberHashCode() {
		if (cachedHashCode == 0) {
			cachedHashCode = VmMember.calcHashCode(getName(), getSignature());
		}
		return cachedHashCode;
	}
}
