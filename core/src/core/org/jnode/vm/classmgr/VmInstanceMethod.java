/*
 * $Id$
 */
package org.jnode.vm.classmgr;

/**
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
	 * @param noArgs
	 * @param selectorMap
	 */
	public VmInstanceMethod(
		String name,
		String signature,
		int modifiers,
		VmType declaringClass,
		int noArgs,
		SelectorMap selectorMap,
		int staticsIdx) {
		super(name, signature, modifiers, declaringClass, noArgs, selectorMap, staticsIdx);
	}
	
	/**
	 * Create a clone of an abstract method.
	 * @param method
	 */
	public VmInstanceMethod(VmInstanceMethod method) {
		super(method.getName(), method.signature, method.getModifiers(), method.declaringClass, method.getNoArgs(), method.getSelector(), method.getStaticsIndex());
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
