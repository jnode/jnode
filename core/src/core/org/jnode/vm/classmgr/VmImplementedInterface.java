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

import org.jnode.vm.VmSystemObject;

/**
 * Element of a class that represents a single implemented interface.
 * 
 * @author epr
 */
public final class VmImplementedInterface extends VmSystemObject {
	
	/** The name of the interface class */
	private final String className;
	/** The resolved interface class */
	private VmInterfaceClass resolvedClass;

	/**
	 * Create a new instance
	 * @param className
	 */
	protected VmImplementedInterface(String className) {
		if (className == null) {
			throw new IllegalArgumentException("className cannot be null");
		}
		this.className = className;
		this.resolvedClass = null;
	}

	/**
	 * Create a new instance
	 * @param vmClass
	 */
	protected VmImplementedInterface(VmType vmClass) {
		if (vmClass == null) {
			throw new IllegalArgumentException("vmClass cannot be null");
		}
		if (vmClass instanceof VmInterfaceClass) {
			this.className = vmClass.getName();
			this.resolvedClass = (VmInterfaceClass)vmClass;
		} else {
			throw new IllegalArgumentException("vmClass must be an interface class");
		}
	}

	/**
	 * Gets the resolved interface class.
	 * @return The resolved class
	 */
	public VmInterfaceClass getResolvedVmClass() 
	throws NotResolvedYetException {
		if (resolvedClass == null) {
			throw new NotResolvedYetException(className);
		}
		return resolvedClass;
	}

	/**
	 * Resolve the members of this object.
	 * @param clc
	 * @throws ClassNotFoundException
	 */
	protected void resolve(VmClassLoader clc)
		throws ClassNotFoundException {
		if (resolvedClass == null) {
			final VmType type = clc.loadClass(className, true);
			if (type instanceof VmInterfaceClass) {
				this.resolvedClass = (VmInterfaceClass)type;
			} else {
				throw new ClassNotFoundException("Class " + className + " is not an interface");
			}
			resolvedClass.link();
		}
	}

	/**
	 * Convert myself into a String representation
	 * @return String
	 */
	public String toString() {
		return "_I_" + mangleClassName(getResolvedVmClass().getName());
	}
}
