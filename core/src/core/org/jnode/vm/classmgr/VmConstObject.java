/**
 * $Id$
 */
package org.jnode.vm.classmgr;

import org.jnode.vm.Uninterruptible;
import org.jnode.vm.VmSystemObject;

/**
 * <description>
 * 
 * @author epr
 */
public abstract class VmConstObject extends VmSystemObject implements Uninterruptible {

	protected final VmCP cp;
	private final int cpIdx;
	private boolean resolved = false;
	
	public VmConstObject(VmCP cp, int cpIdx) {
		this.cp = cp;
		this.cpIdx = cpIdx;
	}
	
	/**
	 * Resolve the references of this constant to loaded VmXxx objects.
	 * @param clc
	 */
	public void resolve(VmClassLoader clc) {
		if (!resolved) {
			doResolve(clc);
			resolved = true;
		}
	}
	
	/**
	 * Returns the resolved.
	 * @return boolean
	 */
	public boolean isResolved() {
		return resolved;
	}

	/**
	 * Resolve the references of this constant to loaded VmXxx objects.
	 * @param clc
	 */
	protected abstract void doResolve(VmClassLoader clc);
	
	/**
	 * Gets the index of this object in the constantpool it is in.
	 * @return Returns the cpIdx.
	 */
	public final int getCpIdx() {
		return this.cpIdx;
	}

}
