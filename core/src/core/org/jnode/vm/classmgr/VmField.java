/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
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
		super(name, signature, modifiers | (((Modifier.isPrimitive(signature) || Modifier.isAddressType(signature)) ? 0 : Modifier.ACC_OBJECTREF)), declaringClass);
		this.primitive = Modifier.isPrimitive(signature);
		this.typeSize = Modifier.getTypeSize(signature, slotSize);
	}

	/**
	 * Is this a field with a primitive type?
	 * @return boolean
	 */
	public final boolean isPrimitive() {
		return primitive;
	}
	
	/**
	 * Is this a field transient?
	 * @return boolean
	 */
	public final boolean isTransient() {
		return ((this.getModifiers() & Modifier.ACC_TRANSIENT) != 0);
	}
	
	/**
	 * Is the field of the type Address?
	 * @return boolean
	 */
	public boolean isAddressType() {
		return Modifier.isAddressType(signature);
	}

	/**
	 * Is the field a non-primitive field and not an address type?
	 * @return boolean
	 */
	public boolean isObjectRef() {
		return Modifier.isObjectRef(getModifiers());
	}

	/**
	 * Is this a field of double width (double, long)
	 * @return boolean
	 */
	public final boolean isWide() {
        return ((this.getModifiers() & Modifier.ACC_WIDE) != 0);
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
