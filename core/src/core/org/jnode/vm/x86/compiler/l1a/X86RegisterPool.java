/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.x86.Register;
import org.jnode.util.BootableArrayList;
import org.jnode.vm.compiler.ir.Operand;

/**
* @author Madhu Siddalingaiah
* @author Patrik Reali
 *
 * Handle the pool of registers
 * Taken from l2 compiler. Should be merged with it in the end, integrating the changes.
 */

//TODO: merge with l2's version of X86RegisterPool
//TODO: not all registers are equivalent; try to return EBX and ECX when possible, spare EAX, EDX
//TODO: keep track of register in use, to allow spilling (to stack or other registers)

final class X86RegisterPool extends org.jnode.vm.compiler.ir.RegisterPool {

	BootableArrayList registers;

	public X86RegisterPool() {
		registers = new BootableArrayList();
		registers.add(Register.EAX);
		registers.add(Register.EBX);
		registers.add(Register.ECX);
		registers.add(Register.EDX);
		// not sure what to do with ESI and EDI just yet...
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.RegisterPool#request(int)
	 */
	public Object request(int type) {
		if (type == Operand.LONG) {
			return null;
		}
		if (type == Operand.FLOAT || type == Operand.DOUBLE) {
			return null;
		}
		if (registers.size() == 0) {
			return null;
		} else {
			return registers.remove(registers.size() - 1);
		}
	}
	
	/**
	 * Check whether the given register is free
	 * 
	 * @param register
	 * @return true, when register is free
	 */
	public boolean isFree(Object register) {
		return registers.contains(register);
	}
	
	/**
	 * Require a particular registers
	 * 
	 * @param register
	 * @return false, if the register is already in use
	 */
	public boolean request(Object register) {
		boolean free = isFree(register);
		if (free)
			registers.remove(register);
		return free;
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.RegisterPool#release(java.lang.Object)
	 */
	public void release(Object register) {
		registers.add(register);
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.RegisterPool#supports3AddrOps()
	 */
	public boolean supports3AddrOps() {
		return false;
	}

}
