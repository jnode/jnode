/**
 * $Id$
 */
package org.jnode.vm.classmgr;

/**
 * Entry of a constantpool describing a class reference.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class VmConstClass extends VmConstObject {

	/** The cp index of the name of the class */
	private int nameIndex;
	/** The resolved class */
	private VmType vmClass;
	private String name;

	public VmConstClass(VmCP cp, int cpIdx, int nameIndex) {
		super(cp, cpIdx);
		this.nameIndex = nameIndex;
	}

	/**
	 * Gets the name of the class this constant is a reference to
	 * @return String
	 */
	public String getClassName() {
		if (name == null) {
			name = cp.getUTF8(nameIndex).replace('/', '.');
		}
		return name;
	}

	/**
	 * Resolve the references of this constant to loaded VmXxx objects.
	 * @param clc
	 */
	protected void doResolve(VmClassLoader clc) {
		if (vmClass == null) {
			final String name = getClassName();
			try {
				vmClass = clc.loadClass(name, true);
			} catch (ClassNotFoundException ex) {
				throw (NoClassDefFoundError)new NoClassDefFoundError(name).initCause(ex);
			}
		}
	}

	/**
	 * Convert myself into a String representation 
	 * @see java.lang.Object#toString()
	 * @return String
	 */
	public String toString() {
		return "ConstClass: " + getClassName();
	}
	
	/**
	 * Returns the vmClass.
	 * @return VmClass
	 */
	public VmType getResolvedVmClass() {
		if (vmClass == null) {
			throw new RuntimeException("vmClass is not yet resolved");
		} else {
			return vmClass;
		}
	}
	
	/**
	 * Sets the resolved vmClass. If the resolved vmClass was already set, any
	 * call to this method is silently ignored.
	 * @param vmClass The vmClass to set
	 */
	public void setResolvedVmClass(VmType vmClass) {
		if (this.vmClass == null) {
			this.vmClass = vmClass;
		}
	}
}
