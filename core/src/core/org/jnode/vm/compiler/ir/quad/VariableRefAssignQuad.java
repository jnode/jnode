/*
 * $Id$
 *
 * mailto:madhu@madhu.com
 */
package org.jnode.vm.compiler.ir.quad;

import org.jnode.vm.compiler.ir.CodeGenerator;
import org.jnode.vm.compiler.ir.IRBasicBlock;
import org.jnode.vm.compiler.ir.Operand;
import org.jnode.vm.compiler.ir.Variable;

/**
 * @author Madhu Siddalingaiah
 *
 */
public class VariableRefAssignQuad extends AssignQuad {
	/**
	 * Right hand side of assignment
	 */
	private Operand refs[];

	/**
	 * @param address
	 */
	public VariableRefAssignQuad(int address, IRBasicBlock block, int lhsIndex, int rhsIndex) {
		super(address, block, lhsIndex);
		refs = new Operand[] { getOperand(rhsIndex) };
	}

	/**
	 * @param lhs
	 * @param rhs
	 */
	public VariableRefAssignQuad(int address, IRBasicBlock block, Variable lhs, Variable rhs) {
		super(address, block, lhs);
		refs = new Operand[] { rhs };
	}

	/**
	 * @see org.jnode.vm.compiler.ir.quad.Quad#getReferencedOps()
	 */
	public Operand[] getReferencedOps() {
		return refs;
	}

	public String toString() {
		return getAddress() + ": " + getLHS().toString() + " = " + refs[0];
	}

	/**
	 * @return
	 */
	public Operand getRHS() {
		return refs[0];
	}

	/**
	 * @param operand
	 * @return
	 */
	public Operand propagate(Variable operand) {
		setDeadCode(true);
		return refs[0];
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.Quad#doPass2(org.jnode.util.BootableHashMap)
	 */
	// This operation will almost always become dead code, but I wanted to play it
	// safe and compute liveness assuming it might survive.
	public void doPass2() {
		refs[0] = refs[0].simplify();
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
