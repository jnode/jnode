/*
 * $Id$
 *
 * mailto:madhu@madhu.com
 */
package org.jnode.vm.compiler.ir.quad;

import org.jnode.vm.compiler.ir.IRBasicBlock;
import org.jnode.vm.compiler.ir.Operand;
import org.jnode.vm.compiler.ir.Variable;

/**
 * @author Madhu Siddalingaiah
 *
 */
public abstract class AssignQuad extends Quad {
	/**
	 * Left hand side of assignment
	 */
	private Variable lhs;
	
	// TODO these two need to go!
	private int lhsIndex;
	private Variable[] variables;

	public AssignQuad(int address, IRBasicBlock block, int lhsIndex) {
		super(address, block);
		this.variables = block.getVariables();
		this.lhsIndex = lhsIndex;
		this.lhs = (Variable) variables[lhsIndex].clone();
		lhs.setAssignQuad(this);
	}

	/**
	 * @see org.jnode.vm.compiler.ir.quad.Quad#getDefinedOp()
	 */
	public Operand getDefinedOp() {
		return lhs;
	}
	
	public Variable getLHS() {
		return lhs;
	}

	/**
	 * @return
	 */
	public Variable[] getVariables() {
		return variables;
	}

	/**
	 * Simplifies this operation by propagating the right hand side (RHS)
	 * For example, constant assignments can be simplified to return the
	 * RHS constant. Binary operations can be constant folded if the RHS
	 * contains only constants.
	 * 
	 * If simplification is not possible, this method should return the
	 * argument operand.
	 * 
	 * @param operand
	 * @return simplifed result of this operation, or operand
	 */
	public abstract Operand propagate(Variable operand);

	/**
	 * Returns the address where the left hand side (LHS) of this quad
	 * is live. In general, this address is simply this.getAddress() + 1.
	 * In the case of CPUs that support only two address operations,
	 * e.g. x86, there are conditions where the LHS will interfere
	 * with the RHS. The obvious example is any non-commutative binary
	 * operation:
	 * 
	 * a = b / c
	 * 
	 * Variables a and c cannot occupy the same location for two address
	 * machines.
	 * 
	 * For these cases, this method must return this.getAddress() so that
	 * register allocators can accommodate the interference.
	 * 
	 * @return the address where the left hand side variable is live
	 */
	public abstract int getLHSLiveAddress();

	/**
	 * @param lhs
	 */
	public void setLHS(Variable lhs) {
		this.lhs = lhs;
	}
}
