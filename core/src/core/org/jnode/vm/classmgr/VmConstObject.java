/**
 * $Id$
 */
package org.jnode.vm.classmgr;

import org.jnode.vm.VmSystemObject;
import org.vmmagic.pragma.Uninterruptible;

/**
 * <description>
 * 
 * @author epr
 */
public abstract class VmConstObject extends VmSystemObject implements Uninterruptible {

	private boolean resolved = false;
	
	public VmConstObject() {
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
	
	void link(VmCP cp) {
		// Override when needed
	}
}
