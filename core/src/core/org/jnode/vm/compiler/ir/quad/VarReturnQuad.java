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
import org.jnode.vm.compiler.ir.Variable;

/**
 * @author Madhu Siddalingaiah
 *
 */
public class VarReturnQuad extends Quad {
	private Operand refs[];

	/**
	 * @param address
	 */
	public VarReturnQuad(int address, IRBasicBlock block, int varIndex) {
		super(address, block);
		refs = new Operand[] { getOperand(varIndex) };
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
		return refs;
	}
	
	public Operand getOperand() {
		return refs[0];
	}

	public String toString() {
		return getAddress() + ": return " + refs[0];
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.Quad#doPass2(org.jnode.util.BootableHashMap)
	 */
	public void doPass2(BootableHashMap liveVariables) {
		refs[0] = refs[0].simplify();
		if (refs[0] instanceof Variable) {
			Variable v = (Variable) refs[0];
			v.setLastUseAddress(this.getAddress());
			liveVariables.put(v, v);
		}
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.Quad#generateCode(org.jnode.vm.compiler.ir.CodeGenerator)
	 */
	public void generateCode(CodeGenerator cg) {
		cg.generateCodeFor(this);
	}
}
