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
	private boolean resolved = false;
	
	public VmConstObject(VmCP cp) {
		this.cp = cp;
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
}
