/*
 * $Id$
 */
package org.jnode.vm.classmgr;

/**
 * @author epr
 */
public class VmInstanceField extends VmField {

	/** Offset of this field in an object (used for instance fields only) */
	private int offset;

	/**
	 * @param name
	 * @param signature
	 * @param modifiers
	 * @param offset
	 * @param declaringClass
	 * @param slotSize
	 */
	public VmInstanceField(
		String name,
		String signature,
		int modifiers,
		int offset,
		VmType declaringClass,
		int slotSize) {
		super(name, signature, modifiers, declaringClass, slotSize);
		if (Modifier.isStatic(modifiers)) {
			throw new IllegalArgumentException("Static field in VmInstanceField");
		}
		this.offset = offset;
	}

	/** 
	 * Gets the offset of this field in the object
	 * @return int
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * Resolve the offset on this field in a class.
	 * @param classOffset
	 */
	protected void resolveOffset(int classOffset) {
		offset += classOffset;
	}

}
