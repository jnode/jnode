/*
 * $Id$
 */
package org.jnode.vm.classmgr;

/**
 * VM representation of a static method.
 *
 * @author epr
 */
public class VmStaticMethod extends VmMethod {

	/**
	 * @param name
	 * @param signature
	 * @param modifiers
	 * @param declaringClass
	 */
	public VmStaticMethod(
		String name,
		String signature,
		int modifiers,
		VmType declaringClass) {
		super(name, signature, modifiers, declaringClass);
	}

}
