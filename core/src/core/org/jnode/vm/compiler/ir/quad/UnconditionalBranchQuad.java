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
