/*
 * $Id$
 */
package org.jnode.vm.classmgr;

/**
 * @author epr
 */
public final class VmStaticField extends VmField {

	/** The index in the statics table */
	private final int staticsIndex;

	/**
	 * @param name
	 * @param signature
	 * @param modifiers
	 * @param staticsIndex
	 * @param declaringClass
	 * @param slotSize
	 */
	public VmStaticField(
		String name,
		String signature,
		int modifiers,
		int staticsIndex,
		VmType declaringClass,
		int slotSize) {
		super(name, signature, modifiers, declaringClass, slotSize);
		if (!Modifier.isStatic(modifiers)) {
			throw new IllegalArgumentException("Instance field in VmStaticField");
		}
		VmStatics.staticFieldCount++;
		this.staticsIndex = staticsIndex;
	}

	/**
	 * Gets the indexe of this field in the statics table.
	 * @return Returns the staticsIndex.
	 */
	public final int getStaticsIndex() {
		return this.staticsIndex;
	}
}
