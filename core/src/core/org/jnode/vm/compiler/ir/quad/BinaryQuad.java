/*
 * $Id$
 *
 * mailto:madhu@madhu.com
 */
package org.jnode.vm.compiler.ir.quad;

import org.jnode.util.BootableHashMap;
import org.jnode.vm.compiler.ir.CodeGenerator;
import org.jnode.vm.compiler.ir.Constant;
import org.jnode.vm.compiler.ir.IRBasicBlock;
import org.jnode.vm.compiler.ir.Operand;
import org.jnode.vm.compiler.ir.Variable;

/**
 * @author Madhu Siddalingaiah
 *
 */
public class BinaryQuad extends AssignQuad {
	private static final String[] OP_MAP = {
		"+", "+", "+", "+",
		"-", "-", "-", "-",
		"*", "*", "*", "*",
		"/", "/", "/", "/",
		"%", "%", "%", "%",
		"<<", "<<",
		">>", ">>",
		">>>", ">>>",
		"&", "&",
		"|", "|",
		"^", "^",
	};
	public static final int IADD = 1;
	public static final int LADD = 2;
	public static final int FADD = 3;
	public static final int DADD = 4;
	public static final int ISUB = 5;
	public static final int LSUB = 6;
	public static final int FSUB = 7;
	public static final int DSUB = 8;
	public static final int IMUL = 9;
	public static final int LMUL = 10;
	public static final int FMUL = 11;
	public static final int DMUL = 12;
	public static final int IDIV = 13;
	public static final int LDIV = 14;
	public static final int FDIV = 15;
	public static final int DDIV = 16;
	public static final int IREM = 17;
	public static final int LREM = 18;
	public static final int FREM = 19;
	public static final int DREM = 20;
	public static final int ISHL = 21;
	public static final int LSHL = 22;
	public static final int ISHR = 23;
	public static final int LSHR = 24;
	public static final int IUSHR = 25;
	public static final int LUSHR = 26;
	public static final int IAND = 27;
	public static final int LAND = 28;
	public static final int IOR = 29;
	public static final int LOR = 30;
	public static final int IXOR = 31;
	public static final int LXOR = 32;

	private Operand operand1, operand2;
	private int operation;
	private Operand refs[];
	private boolean commutative;

	/**
	 * @param address
	 * @param block
	 * @param lhsIndex
	 */
	public BinaryQuad(int address, IRBasicBlock block, int lhsIndex,
		int varIndex1, int operation, int varIndex2) {

		super(address, block, lhsIndex);
		this.operand1 = getOperand(varIndex1);
		this.operation = operation;
		this.operand2 = getOperand(varIndex2);
		refs = new Operand[] { operand1, operand2 };
		doSSA();
		this.commutative =
			operation == IADD || operation == IMUL ||
			operation == LADD || operation == LMUL ||
			operation == FADD || operation == FMUL ||
			operation == DADD || operation == DMUL;
	}

	public BinaryQuad(int address, IRBasicBlock block, int lhsIndex,
		int varIndex1, int operation, Operand op2) {

		super(address, block, lhsIndex);
		this.operand1 = getOperand(varIndex1);
		this.operation = operation;
		this.operand2 = op2;
		refs = new Operand[] { operand1, operand2 };
		doSSA();
		this.commutative =
			operation == IADD || operation == IMUL ||
			operation == LADD || operation == LMUL ||
			operation == FADD || operation == FMUL ||
			operation == DADD || operation == DMUL;
	}

	/**
	 * @see org.jnode.vm.compiler.ir.quad.Quad#getReferencedOps()
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
	public int getOperation() {
		return operation;
	}

	public String toString() {
		return getAddress() + ": " + getLHS().toString() + " = " +
			operand1.toString() + " " + OP_MAP[operation - IADD] +
			" " + operand2.toString();
	}

	/**
	 * @return
	 */
	public Quad foldConstants() {
		if (operand1 instanceof Constant && operand2 instanceof Constant) {
			Constant c1 = (Constant) operand1;
			Constant c2 = (Constant) operand2;
			switch (operation) {
				case IADD:
					return new ConstantRefAssignQuad(this.getAddress(), this.getBasicBlock(),
						this.getLHS().getIndex(), c1.iAdd(c2));

				case ISUB:
					return new ConstantRefAssignQuad(this.getAddress(), this.getBasicBlock(),
						this.getLHS().getIndex(), c1.iSub(c2));

				case IMUL:
					return new ConstantRefAssignQuad(this.getAddress(), this.getBasicBlock(),
						this.getLHS().getIndex(), c1.iMul(c2));

				case IDIV:
					return new ConstantRefAssignQuad(this.getAddress(), this.getBasicBlock(),
						this.getLHS().getIndex(), c1.iDiv(c2));

				default:
					throw new IllegalArgumentException("Don't know how to fold those...");
			}
		}
		return this;
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.AssignQuad#propagate(org.jnode.vm.compiler.ir.Variable)
	 */
	public Operand propagate(Variable operand) {
		Quad quad = foldConstants();
		if (quad instanceof ConstantRefAssignQuad) {
			setDeadCode(true);
			ConstantRefAssignQuad cop = (ConstantRefAssignQuad) quad;
			return cop.getRHS();
		}
		return operand;
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.Quad#doPass2(org.jnode.util.BootableHashMap)
	 */
	public void doPass2(BootableHashMap liveVariables) {
		operand1 = operand1.simplify();
		operand2 = operand2.simplify();
		if (operand1 instanceof Variable) {
			Variable v = (Variable) operand1;
			v.setLastUseAddress(this.getAddress());
			liveVariables.put(v, v);
		}
		if (operand2 instanceof Variable) {
			Variable v = (Variable) operand2;
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

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.AssignQuad#getLHSLiveAddress()
	 */
	public int getLHSLiveAddress() {
		CodeGenerator cg = CodeGenerator.getInstance();
		int addr = this.getAddress();
		if (cg.supports3AddrOps() || commutative) {
			return addr + 1;
		}
		return addr;
	}
}
