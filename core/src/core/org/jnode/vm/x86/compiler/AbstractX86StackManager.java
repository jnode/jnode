/*
 * $Id$
 */
package org.jnode.vm.x86.compiler;

import org.jnode.assembler.x86.Register;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface AbstractX86StackManager {

	/**
	 * Write code to push the contents of the given register on the stack
	 * 
	 * @param reg
	 * @see JvmType
	 */
	public void writePUSH(int jvmType, Register reg);

	/**
	 * Write code to push a 64-bit word on the stack
	 * 
	 * @param lsbReg
	 * @param msbReg
	 * @see JvmType
	 */
	public void writePUSH64(int jvmType, Register lsbReg, Register msbReg);
}
