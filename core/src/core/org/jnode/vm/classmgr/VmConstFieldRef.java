/**
 * $Id$
 */
package org.jnode.vm.classmgr;


/**
 * Entry of a constantpool describing a field reference.
 * 
 * @author Ewout Prangsma (ewout@users.sourceforge.net)
 */
public class VmConstFieldRef extends VmConstMemberRef  {

	/** The reference to the resolved field */
	private VmField vmResolvedField;

	/**
	 * Constructor for VmFieldRef.
	 * @param cp
	 * @param classIndex
	 * @param nameTypeIndex
	 */
	public VmConstFieldRef(VmCP cp, int cpIdx, int classIndex, int nameTypeIndex) {
		super(cp, cpIdx, classIndex, nameTypeIndex);
	}

	/**
	 * Resolve the references of this constant to loaded VmXxx objects.
	 * @param clc
	 */
	protected void doResolveMember(AbstractVmClassLoader clc) {
		VmType vmClass = getConstClass().getResolvedVmClass();
		vmResolvedField = vmClass.getField(getName());	
		if (vmResolvedField == null) {
			throw new NoSuchFieldError(toString() + " in class " + getClassName());
		}
	}
	
	/**
	 * Returns the resolved field.
	 * @return VmField
	 */
	public VmField getResolvedVmField() {
		if (vmResolvedField == null) {
			throw new RuntimeException("vmField is not yet resolved");
		} else {
			return vmResolvedField;
		}
	}
	
	/**
	 * Sets the resolved vmField. If the resolved vmField was already set, any
	 * call to this method is silently ignored.
	 * @param vmField The vmField to set
	 */
	public void setResolvedVmField(VmField vmField) {
		if (this.vmResolvedField == null) {
			this.vmResolvedField = vmField;
		}
	}

	/**
	 * Is this a field of double width (double, long)
	 * @return boolean
	 */
	public boolean isWide() {
		return Modifier.isWide(getSignature());
	}
}
