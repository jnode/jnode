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
import org.jnode.vm.compiler.ir.RegisterLocation;
import org.jnode.vm.compiler.ir.StackLocation;
import org.jnode.vm.compiler.ir.Variable;

/**
 * @author Madhu Siddalingaiah
 *
 * This class represents binary operations of the form:
 * 
 *   lhs = operand1 operation operand2, where operation is +, -, <<, |, && etc.
 * 
 * The left hand side (lhs) is a Variable inherited from AssignQuad.
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

	/*
	 * These are used to simplify addressing mode testing
	 */
	private static final int MODE_RCC = (Operand.MODE_REGISTER << 16) | (Operand.MODE_CONSTANT << 8) | Operand.MODE_CONSTANT;
	private static final int MODE_RCR = (Operand.MODE_REGISTER << 16) | (Operand.MODE_CONSTANT << 8) | Operand.MODE_REGISTER;
	private static final int MODE_RCS = (Operand.MODE_REGISTER << 16) | (Operand.MODE_CONSTANT << 8) | Operand.MODE_STACK;
	private static final int MODE_RRC = (Operand.MODE_REGISTER << 16) | (Operand.MODE_REGISTER << 8) | Operand.MODE_CONSTANT;
	private static final int MODE_RRR = (Operand.MODE_REGISTER << 16) | (Operand.MODE_REGISTER << 8) | Operand.MODE_REGISTER;
	private static final int MODE_RRS = (Operand.MODE_REGISTER << 16) | (Operand.MODE_REGISTER << 8) | Operand.MODE_STACK;
	private static final int MODE_RSC = (Operand.MODE_REGISTER << 16) | (Operand.MODE_STACK << 8) | Operand.MODE_CONSTANT;
	private static final int MODE_RSR = (Operand.MODE_REGISTER << 16) | (Operand.MODE_STACK << 8) | Operand.MODE_REGISTER;
	private static final int MODE_RSS = (Operand.MODE_REGISTER << 16) | (Operand.MODE_STACK << 8) | Operand.MODE_STACK;
	private static final int MODE_SCC = (Operand.MODE_STACK << 16) | (Operand.MODE_CONSTANT << 8) | Operand.MODE_CONSTANT;
	private static final int MODE_SCR = (Operand.MODE_STACK << 16) | (Operand.MODE_CONSTANT << 8) | Operand.MODE_REGISTER;
	private static final int MODE_SCS = (Operand.MODE_STACK << 16) | (Operand.MODE_CONSTANT << 8) | Operand.MODE_STACK;
	private static final int MODE_SRC = (Operand.MODE_STACK << 16) | (Operand.MODE_REGISTER << 8) | Operand.MODE_CONSTANT;
	private static final int MODE_SRR = (Operand.MODE_STACK << 16) | (Operand.MODE_REGISTER << 8) | Operand.MODE_REGISTER;
	private static final int MODE_SRS = (Operand.MODE_STACK << 16) | (Operand.MODE_REGISTER << 8) | Operand.MODE_STACK;
	private static final int MODE_SSC = (Operand.MODE_STACK << 16) | (Operand.MODE_STACK << 8) | Operand.MODE_CONSTANT;
	private static final int MODE_SSR = (Operand.MODE_STACK << 16) | (Operand.MODE_STACK << 8) | Operand.MODE_REGISTER;
	private static final int MODE_SSS = (Operand.MODE_STACK << 16) | (Operand.MODE_STACK << 8) | Operand.MODE_STACK;

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
			operation == DADD || operation == DMUL ||
			operation == IAND || operation == LAND ||
			operation == IOR  || operation == LOR  ||
			operation == IXOR || operation == LXOR;
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
			operation == DADD || operation == DMUL ||
			operation == IAND || operation == LAND ||
			operation == IOR  || operation == LOR  ||
			operation == IXOR || operation == LXOR;
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
	 * If operand1 and operand2 are both Constants, then fold them.
	 * TODO: this only supports int +, -, *, / right now.
	 * 
	 * @return resulting Quad after folding
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
					throw new IllegalArgumentException("Don't know how to fold those yet...");
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

	/**
	 * Simplifies operands by calling operand.simplify().
	 * simplify will combine phi references and propagate copies
	 * This method will also update liveness of operands by setting last use addr
	 * 
	 * @see org.jnode.vm.compiler.ir.quad.Quad#doPass2(org.jnode.util.BootableHashMap)
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

	/**
	 * Code generation is complicated by the permutations of addressing modes.
	 * This is not as nice as it could be, but it could be worse!
	 * 
	 * @see org.jnode.vm.compiler.ir.quad.Quad#generateCode(org.jnode.vm.compiler.ir.CodeGenerator)
	 */
	public void generateCode(CodeGenerator cg) {
		Variable lhs = getLHS();
		int lhsMode = lhs.getAddressingMode();
		int op1Mode = operand1.getAddressingMode();
		int op2Mode = operand2.getAddressingMode();
		
		Object reg1 = null;
		if (lhsMode == Operand.MODE_REGISTER) {
			RegisterLocation regLoc = (RegisterLocation) lhs.getLocation();
			reg1 = regLoc.getRegister();
		}
		Object reg2 = null;
		if (op1Mode == Operand.MODE_REGISTER) {
			Variable var = (Variable) operand1;
			RegisterLocation regLoc = (RegisterLocation) var.getLocation();
			reg2 = regLoc.getRegister();
		}
		Object reg3 = null;
		if (op2Mode == Operand.MODE_REGISTER) {
			Variable var = (Variable) operand2;
			RegisterLocation regLoc = (RegisterLocation) var.getLocation();
			reg3 = regLoc.getRegister();
		}

		int disp1 = 0;
		if (lhsMode == Operand.MODE_STACK) {
			StackLocation stackLoc = (StackLocation) lhs.getLocation();
			disp1 = stackLoc.getDisplacement();
		}
		int disp2 = 0;
		if (op1Mode == Operand.MODE_STACK) {
			Variable var = (Variable) operand1;
			StackLocation stackLoc = (StackLocation) var.getLocation();
			disp2 = stackLoc.getDisplacement();
		}
		int disp3 = 0;
		if (op2Mode == Operand.MODE_STACK) {
			Variable var = (Variable) operand2;
			StackLocation stackLoc = (StackLocation) var.getLocation();
			disp3 = stackLoc.getDisplacement();
		}

		Constant c2 = null;
		if (op1Mode == Operand.MODE_CONSTANT) {
			c2 = (Constant) operand1;
		}
		Constant c3 = null;
		if (op2Mode == Operand.MODE_CONSTANT) {
			c3 = (Constant) operand2;
		}

		int aMode = (lhsMode << 16) | (op1Mode << 8) | op2Mode;
		switch (aMode) {
			case MODE_RCC:
				cg.generateBinaryOP(reg1, c2, operation, c3);
				break;
			case MODE_RCR:
				if (reg1 == reg3 && commutative && !cg.supports3AddrOps()) {
					cg.generateBinaryOP(reg1, reg3, operation, c2);
				} else {
					cg.generateBinaryOP(reg1, c2, operation, reg3);
				}
				break;
			case MODE_RCS:
				cg.generateBinaryOP(reg1, c2, operation, disp3);
				break;
			case MODE_RRC:
				cg.generateBinaryOP(reg1, reg2, operation, c3);
				break;
			case MODE_RRR:
				if (reg1 == reg3 && commutative && !cg.supports3AddrOps()) {
					cg.generateBinaryOP(reg1, reg3, operation, reg2);
				} else {
					cg.generateBinaryOP(reg1, reg2, operation, reg3);
				}
				break;
			case MODE_RRS:
				cg.generateBinaryOP(reg1, reg2, operation, disp3);
				break;
			case MODE_RSC:
				cg.generateBinaryOP(reg1, disp2, operation, c3);
				break;
			case MODE_RSR:
				if (reg1 == reg3 && commutative && !cg.supports3AddrOps()) {
					cg.generateBinaryOP(reg1, reg3, operation, disp2);
				} else {
					cg.generateBinaryOP(reg1, disp2, operation, reg3);
				}
				break;
			case MODE_RSS:
				cg.generateBinaryOP(reg1, disp2, operation, disp3);
				break;
			case MODE_SCC:
				cg.generateBinaryOP(disp1, c2, operation, c3);
				break;
			case MODE_SCR:
				cg.generateBinaryOP(disp1, c2, operation, reg3);
				break;
			case MODE_SCS:
				if (disp1 == disp3 && commutative && !cg.supports3AddrOps()) {
					cg.generateBinaryOP(disp1, disp3, operation, c2);
				} else {
					cg.generateBinaryOP(disp1, c2, operation, disp3);
				}
				break;
			case MODE_SRC:
				cg.generateBinaryOP(disp1, reg2, operation, c3);
				break;
			case MODE_SRR:
				cg.generateBinaryOP(disp1, reg2, operation, reg3);
				break;
			case MODE_SRS:
				if (disp1 == disp3 && commutative && !cg.supports3AddrOps()) {
					cg.generateBinaryOP(disp1, disp3, operation, reg2);
				} else {
					cg.generateBinaryOP(disp1, reg2, operation, disp3);
				}
				break;
			case MODE_SSC:
				cg.generateBinaryOP(disp1, disp2, operation, c3);
				break;
			case MODE_SSR:
				cg.generateBinaryOP(disp1, disp2, operation, reg3);
				break;
			case MODE_SSS:
				if (disp1 == disp3 && commutative && !cg.supports3AddrOps()) {
					cg.generateBinaryOP(disp1, disp3, operation, disp2);
				} else {
					cg.generateBinaryOP(disp1, disp2, operation, disp3);
				}
				break;
			default:
				throw new IllegalArgumentException("Undefined addressing mode: " + aMode);
		}
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
