/*
 * $Id$
 */
package org.jnode.vm.classmgr;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class VmSpecialMethod extends VmStaticMethod {

	/**
	 * @param name
	 * @param signature
	 * @param modifiers
	 * @param staticsIdx
	 * @param declaringClass
	 * @param noArgs
	 */
	VmSpecialMethod(String name, String signature, int modifiers, VmType declaringClass, int noArgs, int staticsIdx) {
		super(name, signature, modifiers | Modifier.ACC_SPECIAL, declaringClass, noArgs, staticsIdx);
	}

}
