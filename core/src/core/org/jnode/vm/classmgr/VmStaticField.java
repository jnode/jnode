/*
 * $Id$
 */
package org.jnode.vm.classmgr;

/**
 * @author epr
 */
public class VmStaticField extends VmField {

	/** Static data (used for static fields only) */
	private final Object staticData;

	/**
	 * @param name
	 * @param signature
	 * @param modifiers
	 * @param staticData
	 * @param declaringClass
	 * @param slotSize
	 */
	public VmStaticField(
		String name,
		String signature,
		int modifiers,
		Object staticData,
		VmType declaringClass,
		int slotSize) {
		super(name, signature, modifiers, declaringClass, slotSize);
		if (!Modifier.isStatic(modifiers)) {
			throw new IllegalArgumentException("Instance field in VmStaticField");
		}
		this.staticData = staticData;
	}

	/**
	 * Gets the value of this field if it is a static field.
	 * @return Object
	 */
	public Object getStaticData() {
		return staticData;
	}
}
