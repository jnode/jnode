/*
 * $Id$
 */
package org.jnode.vm.compiler.ir.quad;

import java.util.Iterator;

import org.jnode.vm.compiler.ir.IRBasicBlock;

/**
 * @author Madhu Siddalingaiah
 * 
 */
public abstract class BranchQuad extends Quad {
	private IRBasicBlock targetBlock;

	/**
	 * @param address
	 */
	public BranchQuad(int address, IRBasicBlock block, int targetAddress) {
		super(address, block);
		Iterator it = block.getSuccessors().iterator();
		while (it.hasNext()) {
			IRBasicBlock succ = (IRBasicBlock) it.next();
			if (succ.getStartPC() == targetAddress) {
				targetBlock = succ;
				break;
			}
		}
		if (targetBlock == null) {
			throw new AssertionError("unable to find target block!");
		}
	}

	/**
	 * @return
	 */
	public int getTargetAddress() {
		return targetBlock.getStartPC();
	}

	/**
	 * @return
	 */
	public IRBasicBlock getTargetBlock() {
		return targetBlock;
	}
}
