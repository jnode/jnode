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
import org.jnode.vm.compiler.ir.PhiOperand;
import org.jnode.vm.compiler.ir.Variable;

/**
 * @author Madhu Siddalingaiah
 * 
 */
public class PhiAssignQuad extends AssignQuad {
	private PhiOperand phi;

	/**
	 * @param address
	 * @param block
	 * @param lhsIndex
	 */
	public PhiAssignQuad(int address, IRBasicBlock block, int lhsIndex) {
		super(address, block, lhsIndex);
		phi = new PhiOperand();
	}

	/**
	 * @param dfb
	 * @param def
	 */
	public PhiAssignQuad(IRBasicBlock dfb, int lhsIndex) {
		this(dfb.getStartPC(), dfb, lhsIndex);
	}

	public PhiOperand getPhiOperand() {
		return (PhiOperand) phi;
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.quad.AssignQuad#propagate(org.jnode.vm.compiler.ir.Variable)
	 */
	public Operand propagate(Variable operand) {
		return operand;
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.quad.AssignQuad#getLHSLiveAddress()
	 */
	public int getLHSLiveAddress() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.quad.Quad#getReferencedOps()
	 */
	public Operand[] getReferencedOps() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.quad.Quad#doPass2(org.jnode.util.BootableHashMap)
	 */
	public void doPass2() {
		phi.simplify();
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.quad.Quad#generateCode(org.jnode.vm.compiler.ir.CodeGenerator)
	 */
	public void generateCode(CodeGenerator cg) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof PhiAssignQuad) {
			PhiAssignQuad paq = (PhiAssignQuad) obj;
			return getLHS().equals(paq.getLHS());
		}
		return false;
	}

	public String toString() {
		if (isDeadCode()) {
			return getAddress() + ": " + "            nop (pruned phi)";
		} else {
			return getAddress() + ": " + getLHS() + " = " + phi;
		}
	}
}
