/**
 * $Id$
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
	public VmInterfaceClass getResolvedVmClass() {
		return resolvedClass;
	}

	/**
	 * Resolve the members of this object.
	 * @param clc
	 * @throws ClassNotFoundException
	 */
	protected void resolve(AbstractVmClassLoader clc)
		throws ClassNotFoundException {
		if (resolvedClass == null) {
			final VmType type = clc.loadClass(className, true);
			if (type instanceof VmInterfaceClass) {
				this.resolvedClass = (VmInterfaceClass)type;
			} else {
				throw new ClassNotFoundException("Class " + className + " is not an interface");
			}
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
