/*
 * $Id$
 *
 * mailto:madhu@madhu.com
 */
package org.jnode.vm.compiler.ir.quad;

import org.jnode.util.BootableHashMap;
import org.jnode.vm.compiler.ir.CodeGenerator;
import org.jnode.vm.compiler.ir.IRBasicBlock;
import org.jnode.vm.compiler.ir.Operand;

/**
 * @author Madhu Siddalingaiah
 *
 */
public class VoidReturnQuad extends Quad {
	/**
	 * @param address
	 */
	public VoidReturnQuad(int address, IRBasicBlock block) {
		super(address, block);
	}

	/**
	 * @see org.jnode.vm.compiler.ir.quad.Quad#getDefinedOp()
	 */
	public Operand getDefinedOp() {
		return null;
	}

	/**
	 * @see org.jnode.vm.compiler.ir.quad.Quad#getReferencedOps()
	 */
	public Operand[] getReferencedOps() {
		return null;
	}

	public String toString() {
		return getAddress() + ": return";
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.Quad#doPass2(org.jnode.util.BootableHashMap)
	 */
	public void doPass2(BootableHashMap liveVariables) {
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.Quad#generateCode(org.jnode.vm.compiler.ir.CodeGenerator)
	 */
	public void generateCode(CodeGenerator cg) {
		cg.generateCodeFor(this);
	}
}
