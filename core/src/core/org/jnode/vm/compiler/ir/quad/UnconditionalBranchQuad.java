/*
 * $Id$
 */
package org.jnode.vm.compiler.ir.quad;

import org.jnode.vm.compiler.ir.CodeGenerator;
import org.jnode.vm.compiler.ir.IRBasicBlock;
import org.jnode.vm.compiler.ir.Operand;

/**
 * @author Madhu Siddalingaiah
 * 
 */
public class UnconditionalBranchQuad extends BranchQuad {
	/**
	 * @param address
	 * @param targetAddress
	 */
	public UnconditionalBranchQuad(int address, IRBasicBlock block, int targetAddress) {
		super(address, block, targetAddress);
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.Quad#getDefinedOp()
	 */
	public Operand getDefinedOp() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.Quad#getReferencedOps()
	 */
	public Operand[] getReferencedOps() {
		return null;
	}

	public String toString() {
		return getAddress() + ": goto " + getTargetBlock(); 
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.Quad#doPass2(org.jnode.util.BootableHashMap)
	 */
	public void doPass2() {
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.Quad#generateCode(org.jnode.vm.compiler.ir.CodeGenerator)
	 */
	public void generateCode(CodeGenerator cg) {
		cg.generateCodeFor(this);
	}
}
