/*
 * $Id$
 */
package org.jnode.vm.compiler.ir.quad;

import org.jnode.vm.compiler.ir.IRBasicBlock;

/**
 * @author Madhu Siddalingaiah
 * 
 */
public abstract class BranchQuad extends Quad {
	private int targetAddress;

	/**
	 * @param address
	 */
	public BranchQuad(int address, IRBasicBlock block, int targetAddress) {
		super(address, block);
		this.targetAddress = targetAddress;
	}

	/**
	 * @return
	 */
	public int getTargetAddress() {
		return targetAddress;
	}
}
