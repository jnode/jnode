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
import org.jnode.vm.compiler.ir.Location;
import org.jnode.vm.compiler.ir.Operand;
import org.jnode.vm.compiler.ir.RegisterLocation;
import org.jnode.vm.compiler.ir.StackLocation;
import org.jnode.vm.compiler.ir.Variable;

/**
 * @author Madhu Siddalingaiah
 * @author Levente Sántha
 *
 */
public class UnaryQuad extends AssignQuad {
	private static final String[] OP_MAP = {
		"I2L", "I2F", "I2D", "L2I",
		"L2F", "L2D", "F2I", "F2L",
		"F2D", "D2I", "D2L", "D2F",
		"I2B", "I2C", "I2S", "INEG",
		"LNEG", "FNEG", "DNEG"
	};
	public static final int I2L = 1;
	public static final int I2F = 2;
	public static final int I2D = 3;
	public static final int L2I = 4;
	public static final int L2F = 5;
	public static final int L2D = 6;
	public static final int F2I = 7;
	public static final int F2L = 8;
	public static final int F2D = 9;
	public static final int D2I = 10;
	public static final int D2L = 11;
	public static final int D2F = 12;
	public static final int I2B = 13;
	public static final int I2C = 14;
	public static final int I2S = 15;
	public static final int INEG = 16;
	public static final int LNEG = 17;
	public static final int FNEG = 18;
	public static final int DNEG = 19;

	private Operand operand;
	private int operation;
	private Operand refs[];

	/**
	 * @param address
	 * @param block
	 * @param lhsIndex
	 */
	public UnaryQuad(int address, IRBasicBlock block, int lhsIndex,
		int operation, int varIndex) {

		super(address, block, lhsIndex);
		this.operation = operation;
		this.operand = getOperand(varIndex);
		refs = new Operand[] { operand };
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.Quad#getReferencedOps()
	 */
	public Operand[] getReferencedOps() {
		return refs;
	}
	/**
	 * @return
	 */
	public Operand getOperand() {
		return operand;
	}

	/**
	 * @return
	 */
	public int getOperation() {
		return operation;
	}

	public String toString() {
		return getAddress() + ": " + getLHS().toString() + " = " +
			OP_MAP[operation - I2L] + " " + operand.toString();
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.AssignQuad#propagate(org.jnode.vm.compiler.ir.Variable)
	 */
	// TODO should fold constants, see BinaryQuad::propagate(...)
	public Operand propagate(Variable operand) {
        Quad quad = foldConstants();
		if (quad instanceof ConstantRefAssignQuad) {
			setDeadCode(true);
			ConstantRefAssignQuad cop = (ConstantRefAssignQuad) quad;
			return cop.getRHS();
		}
		return operand;
	}

    private Quad foldConstants() {
        if(operand instanceof Constant){
            Constant c = (Constant) operand;

            switch (operation) {
                case I2L:
					return new ConstantRefAssignQuad(this.getAddress(), this.getBasicBlock(),
						this.getLHS().getIndex(), c.i2l());

                case I2F:
					return new ConstantRefAssignQuad(this.getAddress(), this.getBasicBlock(),
						this.getLHS().getIndex(), c.i2f());

                case I2D:
					return new ConstantRefAssignQuad(this.getAddress(), this.getBasicBlock(),
						this.getLHS().getIndex(), c.i2d());

                case L2I:
					return new ConstantRefAssignQuad(this.getAddress(), this.getBasicBlock(),
						this.getLHS().getIndex(), c.l2i());

                case L2F:
					return new ConstantRefAssignQuad(this.getAddress(), this.getBasicBlock(),
						this.getLHS().getIndex(), c.l2f());

                case L2D:
					return new ConstantRefAssignQuad(this.getAddress(), this.getBasicBlock(),
						this.getLHS().getIndex(), c.l2d());

                case F2I:
					return new ConstantRefAssignQuad(this.getAddress(), this.getBasicBlock(),
						this.getLHS().getIndex(), c.f2i());

                case F2L:
					return new ConstantRefAssignQuad(this.getAddress(), this.getBasicBlock(),
						this.getLHS().getIndex(), c.f2l());

                case F2D:
					return new ConstantRefAssignQuad(this.getAddress(), this.getBasicBlock(),
						this.getLHS().getIndex(), c.f2d());

                case D2I:
					return new ConstantRefAssignQuad(this.getAddress(), this.getBasicBlock(),
						this.getLHS().getIndex(), c.d2i());

                case D2L:
					return new ConstantRefAssignQuad(this.getAddress(), this.getBasicBlock(),
						this.getLHS().getIndex(), c.d2l());

                case D2F:
					return new ConstantRefAssignQuad(this.getAddress(), this.getBasicBlock(),
						this.getLHS().getIndex(), c.d2f());

                case I2B:
					return new ConstantRefAssignQuad(this.getAddress(), this.getBasicBlock(),
						this.getLHS().getIndex(), c.i2b());

                case I2C:
					return new ConstantRefAssignQuad(this.getAddress(), this.getBasicBlock(),
						this.getLHS().getIndex(), c.i2c());

                case I2S:
					return new ConstantRefAssignQuad(this.getAddress(), this.getBasicBlock(),
						this.getLHS().getIndex(), c.i2s());

				case INEG:
					return new ConstantRefAssignQuad(this.getAddress(), this.getBasicBlock(),
						this.getLHS().getIndex(), c.iNeg());

                case LNEG:
					return new ConstantRefAssignQuad(this.getAddress(), this.getBasicBlock(),
						this.getLHS().getIndex(), c.lNeg());

                case FNEG:
					return new ConstantRefAssignQuad(this.getAddress(), this.getBasicBlock(),
						this.getLHS().getIndex(), c.fNeg());

                case DNEG:
					return new ConstantRefAssignQuad(this.getAddress(), this.getBasicBlock(),
						this.getLHS().getIndex(), c.dNeg());

                default:
					throw new IllegalArgumentException("Don't know how to fold those yet...");
            }
        }
        return this;
    }


    /* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.Quad#doPass2(org.jnode.util.BootableHashMap)
	 */
	public void doPass2(BootableHashMap liveVariables) {
		operand = operand.simplify();
		if (operand instanceof Variable) {
			Variable v = (Variable) operand;
			v.setLastUseAddress(this.getAddress());
			liveVariables.put(v, v);
		}
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.Quad#generateCode(org.jnode.vm.compiler.ir.CodeGenerator)
	 */
	public void generateCode(CodeGenerator cg) {
		Variable lhs = getLHS();
		Location lhsLoc = lhs.getLocation();
		if (lhsLoc instanceof RegisterLocation) {
			RegisterLocation regLoc = (RegisterLocation) lhsLoc;
			Object lhsReg = regLoc.getRegister();
			if (operand instanceof Variable) {
				Variable var = (Variable) operand;
				Location varLoc = var.getLocation();
				if (varLoc instanceof RegisterLocation) {
					RegisterLocation vregLoc = (RegisterLocation) varLoc;
					cg.generateCodeFor(this, lhsReg, operation, vregLoc.getRegister());
				} else if (varLoc instanceof StackLocation) {
					StackLocation stackLoc = (StackLocation) varLoc;
					cg.generateCodeFor(this, lhsReg, operation, stackLoc.getDisplacement());
				} else {
					throw new IllegalArgumentException("Unknown location: " + varLoc);
				}
			} else if (operand instanceof Constant) {
				// this probably won't happen, is should be folded earlier
				Constant con = (Constant) operand;
				cg.generateCodeFor(this, lhsReg, operation, con);
			} else {
				throw new IllegalArgumentException("Unknown operand: " + operand);
			}
		} else if (lhsLoc instanceof StackLocation) {
			StackLocation lhsStackLoc = (StackLocation) lhsLoc;
			int lhsDisp = lhsStackLoc.getDisplacement();
			if (operand instanceof Variable) {
				Variable var = (Variable) operand;
				Location varLoc = var.getLocation();
				if (varLoc instanceof RegisterLocation) {
					RegisterLocation vregLoc = (RegisterLocation) varLoc;
					cg.generateCodeFor(this, lhsDisp, operation, vregLoc.getRegister());
				} else if (varLoc instanceof StackLocation) {
					StackLocation stackLoc = (StackLocation) varLoc;
					cg.generateCodeFor(this, lhsDisp, operation, stackLoc.getDisplacement());
				} else {
					throw new IllegalArgumentException("Unknown location: " + varLoc);
				}
			} else if (operand instanceof Constant) {
				Constant con = (Constant) operand;
				cg.generateCodeFor(this, lhsDisp, operation, con);
			} else {
				throw new IllegalArgumentException("Unknown operand: " + operand);
			}
		} else {
			throw new IllegalArgumentException("Unknown location: " + lhsLoc);
		}
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.AssignQuad#getLHSLiveAddress()
	 */
	public int getLHSLiveAddress() {
		return this.getAddress() + 1;
	}
}
