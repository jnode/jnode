/*
 * $Id$
 */

package org.jnode.vm.classmgr;

import java.lang.reflect.Field;

public abstract class VmField extends VmMember {

	/** java.lang.reflect.Field corresponding to this field */
	private Field javaField;
	/** Type of this field */
	private VmType type;
	/** Is the type of this field primitive? */
	private final boolean primitive;
	/** The size of this field in bytes */
	private final byte typeSize;

	/**
	 * Create a new instance
	 * @param name
	 * @param signature
	 * @param modifiers
	 * @param declaringClass
	 * @param slotSize
	 */
	protected VmField(
		String name,
		String signature,
		int modifiers,
		VmType declaringClass,
		int slotSize) {
		super(name, signature, modifiers, declaringClass);
		this.primitive = Modifier.isPrimitive(signature);
		this.typeSize = Modifier.getTypeSize(signature, slotSize);
	}

	/**
	 * Is this a field with a primitive type?
	 * @return boolean
	 */
	public boolean isPrimitive() {
		return primitive;
	}
	
	/**
	 * Is the field of the type Address?
	 * @return boolean
	 */
	public boolean isAddressType() {
		return Modifier.isAddressType(signature);
	}

	/**
	 * Is this a field of double width (double, long)
	 * @return boolean
	 */
	public boolean isWide() {
		return wide;
	}

	/**
	 * Return me as java.lang.reflect.Field
	 * @return Field
	 */
	public Field asField() {
		if (javaField == null) {
			javaField = new Field(this);
		}
		return javaField;
	}

	/**
	 * Resolve the type of this field
	 * @param cl
	 */
	protected synchronized void resolve(VmClassLoader cl) {
		try {
			type = new Signature(getSignature(), declaringClass.getLoader()).getType();
		} catch (ClassNotFoundException ex) {
			throw (Error)new NoClassDefFoundError().initCause(ex);
		}
	}

	public String toString() {
		return getMangledName();
	}

	public String getMangledName() {
		return mangleClassName(declaringClass.getName())
			+ mangle("." + getName() + '.' + getSignature());
	}

	/**
	 * Returns the type.
	 * @return VmClass
	 */
	public VmType getType() {
		return type;
	}
	
	/**
	 * Gets the size of this field in bytes [1..8].
	 * @return size of this field
	 */
	public byte getTypeSize() {
		return typeSize;
	}
}