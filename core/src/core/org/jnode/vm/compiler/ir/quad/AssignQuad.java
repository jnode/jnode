/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
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

	public AssignQuad(int address, IRBasicBlock block, Variable lhs) {
		super(address, block);
		setLHS(lhs);
	}

	public AssignQuad(int address, IRBasicBlock block, int lhsIndex) {
		this(address, block, block.getVariables()[lhsIndex]);
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
		lhs.setAssignQuad(this);
	}
}
