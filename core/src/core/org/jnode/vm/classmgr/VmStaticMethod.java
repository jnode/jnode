/*
 * $Id$
 */
package org.jnode.vm.classmgr;

/**
 * @author epr
 */
public class VmStaticMethod extends VmMethod {

	/**
	 * @param name
	 * @param signature
	 * @param modifiers
	 * @param declaringClass
	 * @param noArgs
	 */
	public VmStaticMethod(
		String name,
		String signature,
		int modifiers,
		VmType declaringClass,
		int noArgs, int staticsIdx) {
		super(name, signature, modifiers, declaringClass, noArgs, 0, staticsIdx);
	}

}
