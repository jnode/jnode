/*
 * $Id$
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
public class ConditionalBranchQuad extends BranchQuad {
	private final static String[] CONDITION_MAP = {
		"== 0", "!= 0", "< 0", ">= 0", "> 0", "<= 0",
		"!= null", "== null",
		"==",   "!=",   "<",   ">=",   ">",   "<=",
		"=",   "!="
	};

	public final static int IFEQ = 0;
	public final static int IFNE = 1;
	public final static int IFLT = 2;
	public final static int IFGE = 3;
	public final static int IFGT = 4;
	public final static int IFLE = 5;

	public final static int IFNONNULL = 6;
	public final static int IFNULL = 7;

	public final static int IF_ICMPEQ = 8;
	public final static int IF_ICMPNE = 9;
	public final static int IF_ICMPLT = 10;
	public final static int IF_ICMPGE = 11;
	public final static int IF_ICMPGT = 12;
	public final static int IF_ICMPLE = 13;

	public final static int IF_ACMPEQ = 14;
	public final static int IF_ACMPNE = 15;

	private int condition;
	private Operand[] refs;
	private Operand operand2;
	private Operand operand1;

	/**
	 * @param address
	 * @param targetAddress
	 */
	public ConditionalBranchQuad(int address, IRBasicBlock block,
		int varIndex1, int condition, int varIndex2, int targetAddress) {

		super(address, block, targetAddress);
		if (condition < IF_ICMPEQ || condition > IF_ACMPNE) {
			throw new IllegalArgumentException("can't use that condition here");
		}
		this.operand1 = getOperand(varIndex1);
		this.condition = condition;
		this.operand2 = getOperand(varIndex2);
		refs = new Operand[] { operand1, operand2 };
	}

	public ConditionalBranchQuad(int address, IRBasicBlock block,
		int varIndex, int condition, int targetAddress) {

		super(address, block, targetAddress);
		if (condition < IFEQ || condition > IFNULL) {
			throw new IllegalArgumentException("can't use that condition here");
		}
		this.operand1 = getOperand(varIndex);
		this.condition = condition;
		refs = new Operand[] { operand1 };
	}

	/**
	 * @see org.jnode.vm.compiler.ir.Quad#getDefinedOp()
	 */
	public Operand getDefinedOp() {
		return null;
	}

	/**
	 * @see org.jnode.vm.compiler.ir.Quad#getReferencedOps()
	 */
	public Operand[] getReferencedOps() {
		return refs;
	}

	/**
	 * @return
	 */
	public Operand getOperand1() {
		return operand1;
	}

	/**
	 * @return
	 */
	public Operand getOperand2() {
		return operand2;
	}

	/**
	 * @return
	 */
	public int getCondition() {
		return condition;
	}

	public String toString() {
		if (condition >= IF_ICMPEQ) {
			return getAddress() + ": if " + operand1.toString() + " " +
				CONDITION_MAP[condition] + " " + operand2.toString() +
				" goto " + getTargetAddress();
		} else {
			return getAddress() + ": if " + operand1.toString() + " " +
				CONDITION_MAP[condition] + " goto " + getTargetAddress();
		}
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.Quad#doPass2(org.jnode.util.BootableHashMap)
	 */
	public void doPass2(BootableHashMap liveVariables) {
		operand1 = operand1.simplify();
		if (operand1 instanceof Variable) {
			Variable v = (Variable) operand1;
			v.setLastUseAddress(this.getAddress());
			liveVariables.put(v, v);
		}
		if (operand2 != null) {
			operand2 = operand2.simplify();
			if (operand2 instanceof Variable) {
				Variable v = (Variable) operand2;
				v.setLastUseAddress(this.getAddress());
				liveVariables.put(v, v);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.Quad#generateCode(org.jnode.vm.compiler.ir.CodeGenerator)
	 */
	public void generateCode(CodeGenerator cg) {
		cg.generateCodeFor(this);
	}
}
