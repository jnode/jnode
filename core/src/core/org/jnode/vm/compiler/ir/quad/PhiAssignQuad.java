
/*
 * Created on Nov 25, 2004 4:00:07 PM
 *
 * Copyright (c) 2004 Madhu Siddalingaiah
 * All rights reserved
 * mailto:madhu@madhu.com
 */
package org.jnode.vm.compiler.ir.quad;

import org.jnode.util.BootableHashMap;
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
		// TODO Auto-generated method stub
		return null;
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
	public void doPass2(BootableHashMap liveVariables) {
		// TODO Auto-generated method stub

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
		return getAddress() + ": " + getLHS().toString() + " = " + phi.toString();
	}
}
