/*
 * $Id$
 */
package org.jnode.vm.compiler.ir;

/**
 * @author Madhu Siddalingaiah
 * 
 */
public abstract class RegisterPool {
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
}
