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
public class PhiAssignQuad<T> extends AssignQuad<T> {
	private PhiOperand<T> phi;

	/**
	 * @param address
	 * @param block
	 * @param lhsIndex
	 */
	public PhiAssignQuad(int address, IRBasicBlock<T> block, int lhsIndex) {
		super(address, block, lhsIndex);
		phi = new PhiOperand<T>();
	}

	/**
	 * @param dfb
	 * @param def
	 */
	public PhiAssignQuad(IRBasicBlock<T> dfb, int lhsIndex) {
		this(dfb.getStartPC(), dfb, lhsIndex);
	}

	public PhiOperand<T> getPhiOperand() {
		return phi;
	}

	/**
	 * @see org.jnode.vm.compiler.ir.quad.AssignQuad#propagate(org.jnode.vm.compiler.ir.Variable)
	 */
	public Operand<T> propagate(Variable<T> operand) {
		return operand;
	}

	/**
	 * @see org.jnode.vm.compiler.ir.quad.AssignQuad#getLHSLiveAddress()
	 */
	public int getLHSLiveAddress() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see org.jnode.vm.compiler.ir.quad.Quad#getReferencedOps()
	 */
	public Operand<T>[] getReferencedOps() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.jnode.vm.compiler.ir.quad.Quad#doPass2(org.jnode.util.BootableHashMap)
	 */
	public void doPass2() {
		phi.simplify();
	}

	/**
	 * @see org.jnode.vm.compiler.ir.quad.Quad#generateCode(org.jnode.vm.compiler.ir.CodeGenerator)
	 */
	public void generateCode(CodeGenerator cg) {
		// TODO Auto-generated method stub

	}

	/**
     * @see java.lang.Object#equals(java.lang.Object)
     */
	public boolean equals(Object obj) {
		if (obj instanceof PhiAssignQuad) {
			PhiAssignQuad<T> paq = (PhiAssignQuad<T>) obj;
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
