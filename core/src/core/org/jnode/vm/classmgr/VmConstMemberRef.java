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


/**
 * Abstract entry of a constantpool describing an member (method or field) reference.
 * 
 * @author epr
 */
abstract class VmConstMemberRef extends VmResolvableConstObject {
	
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
