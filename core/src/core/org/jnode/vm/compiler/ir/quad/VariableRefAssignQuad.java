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
