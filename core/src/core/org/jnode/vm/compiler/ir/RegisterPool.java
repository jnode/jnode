/*
 * $Id$
 */
package org.jnode.vm.compiler.ir;

/**
 * @author Madhu Siddalingaiah
 * 
 */
public abstract class RegisterPool {
	static RegisterPool instance;

	/**
	 * @param type - one of the types found in Operand
	 * @return an available register or null if none available
	 * 
	 * @see org.jnode.vm.compiler.ir.Operand
	 */
	public abstract Object request(int type);

	/**
	 * @param register - register to put back into pool
	 */
	public abstract void release(Object register);
	
	/**
	 * Returns true of this CPU supports 3 address operands
	 * This probably belongs in a machine specifics class...
	 * 
	 * @return
	 */
	public abstract boolean supports3AddrOps();

	/**
	 * @return
	 */
	public static RegisterPool getInstance() {
		if (instance == null) {
			throw new IllegalArgumentException("no register pool defined");
		}
		return instance;
	}
}
