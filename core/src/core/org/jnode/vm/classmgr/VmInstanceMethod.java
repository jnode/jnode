/*
 * $Id$
 */
package org.jnode.vm.classmgr;

/**
 * VM representation of a non-static method.
 * 
 * @author epr
 */
public class VmInstanceMethod extends VmMethod {

	/** Offset of this method in the VMT of its declaring class */
	private int tibOffset;

	/**
	 * @param name
	 * @param signature
	 * @param modifiers
	 * @param declaringClass
	 */
	public VmInstanceMethod(
		String name,
		String signature,
		int modifiers,
		VmType declaringClass) {
		super(name, signature, modifiers, declaringClass);
	}
	
	/**
	 * Create a clone of an abstract method.
	 * @param method
	 */
	public VmInstanceMethod(VmInstanceMethod method) {
		super(method);
		if (!method.isAbstract()) {
			throw new IllegalArgumentException("Method must be abstract");
		}
	}

	/**
	 * Gets the offset of this method in the TIB of its declaring class
	 * @return offset
	 */
	public int getTibOffset() {
		return tibOffset;
	}

	/**
	 * Sets the offset of this method in the VMT of its declaring class
	 * @param offset
	 */
	protected void setTibOffset(int offset) {
		this.tibOffset = offset;
	}

}
