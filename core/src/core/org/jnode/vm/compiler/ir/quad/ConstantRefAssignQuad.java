/*
 * $Id$
 *
 * mailto:madhu@madhu.com
 */
package org.jnode.vm.compiler.ir.quad;

import org.jnode.vm.compiler.ir.CodeGenerator;
import org.jnode.vm.compiler.ir.Constant;
import org.jnode.vm.compiler.ir.IRBasicBlock;
import org.jnode.vm.compiler.ir.Operand;
import org.jnode.vm.compiler.ir.Variable;

/**
 * @author Madhu Siddalingaiah
 *
 */
public class ConstantRefAssignQuad extends AssignQuad {
	private Constant rhs;

	/**
	 * @param address
	 * @param block
	 * @param lhsIndex
	 */
	public ConstantRefAssignQuad(int address, IRBasicBlock block, int lhsIndex,
		Constant rhs) {
		super(address, block, lhsIndex);
		this.rhs = rhs;
	}

	/**
	 * @see org.jnode.vm.compiler.ir.quad.Quad#getReferencedOps()
	 */
	public Operand[] getReferencedOps() {
		return null;
	}

	public String toString() {
		return getAddress() + ": " + getLHS().toString() + " = " + rhs.toString();
	}

	/**
	 * @return
	 */
	public Constant getRHS() {
		return rhs;
	}

	/**
	 * @param operand
	 * @return
	 */
	public Operand propagate(Variable operand) {
		setDeadCode(true);
		return rhs;
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

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.AssignQuad#getLHSLiveAddress()
	 */
	public int getLHSLiveAddress() {
		return this.getAddress() + 1;
	}
}
