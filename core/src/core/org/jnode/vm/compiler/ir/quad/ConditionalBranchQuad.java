/*
 * $Id$
 */
package org.jnode.vm.compiler.ir.quad;

import org.jnode.util.BootableHashMap;
import org.jnode.vm.compiler.ir.CodeGenerator;
import org.jnode.vm.compiler.ir.IRBasicBlock;
import org.jnode.vm.compiler.ir.Operand;
import org.jnode.vm.compiler.ir.Variable;
import org.jnode.vm.compiler.ir.Location;
import org.jnode.vm.compiler.ir.RegisterLocation;
import org.jnode.vm.compiler.ir.StackLocation;
import org.jnode.vm.compiler.ir.Constant;

/**
 * @author Madhu Siddalingaiah
 * @author Levente Sántha
 */
public class ConditionalBranchQuad extends BranchQuad {
    private final static String[] CONDITION_MAP = {
        "== 0", "!= 0", "< 0", ">= 0", "> 0", "<= 0",
        "!= null", "== null",
        "==", "!=", "<", ">=", ">", "<=",
        "=", "!="
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
    private boolean commutative;
    private Operand[] refs;
    private static final int MODE_CC = (Operand.MODE_CONSTANT << 8) | Operand.MODE_CONSTANT;
    private static final int MODE_CR = (Operand.MODE_CONSTANT << 8) | Operand.MODE_REGISTER;
    private static final int MODE_CS = (Operand.MODE_CONSTANT << 8) | Operand.MODE_STACK;
    private static final int MODE_RC = (Operand.MODE_REGISTER << 8) | Operand.MODE_CONSTANT;
    private static final int MODE_RR = (Operand.MODE_REGISTER << 8) | Operand.MODE_REGISTER;
    private static final int MODE_RS = (Operand.MODE_REGISTER << 8) | Operand.MODE_STACK;
    private static final int MODE_SC = (Operand.MODE_STACK << 8) | Operand.MODE_CONSTANT;
    private static final int MODE_SR = (Operand.MODE_STACK << 8) | Operand.MODE_REGISTER;
    private static final int MODE_SS = (Operand.MODE_STACK << 8) | Operand.MODE_STACK;

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
        this.condition = condition;
        this.commutative = condition == IF_ICMPEQ ||
                           condition == IF_ICMPNE ||
                           condition == IF_ACMPEQ ||
                           condition == IF_ACMPNE;
        refs = new Operand[]{ getOperand(varIndex1), getOperand(varIndex2) };
    }

    public ConditionalBranchQuad(int address, IRBasicBlock block,
                                 int varIndex, int condition, int targetAddress) {

        super(address, block, targetAddress);
        if (condition < IFEQ || condition > IFNULL) {
            throw new IllegalArgumentException("can't use that condition here");
        }
        this.condition = condition;
        this.commutative = condition == IF_ICMPEQ ||
                           condition == IF_ICMPNE ||
                           condition == IF_ACMPEQ ||
                           condition == IF_ACMPNE;
        refs = new Operand[]{ getOperand(varIndex) };
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

    /**
     * @return
     */
    public Operand getOperand1() {
        return refs[0];
    }

    /**
     * @return
     */
    public Operand getOperand2() {
        return refs[1];
    }

    /**
     * @return
     */
    public int getCondition() {
        return condition;
    }

    public String toString() {
        if (condition >= IF_ICMPEQ) {
            return getAddress() + ": if " + refs[0].toString() + " " +
                    CONDITION_MAP[condition] + " " + refs[1].toString() +
                    " goto " + getTargetAddress();
        } else {
            return getAddress() + ": if " + refs[0].toString() + " " +
                    CONDITION_MAP[condition] + " goto " + getTargetAddress();
        }
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
        if (refs[1] != null) {
            refs[1] = refs[1].simplify();
            if (refs[1] instanceof Variable) {
                Variable v = (Variable) refs[1];
                v.setLastUseAddress(this.getAddress());
                liveVariables.put(v, v);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.jnode.vm.compiler.ir.Quad#generateCode(org.jnode.vm.compiler.ir.CodeGenerator)
     */
    public void generateCode(CodeGenerator cg) {
        //cg.generateCodeFor(this);
        if (condition >= IF_ICMPEQ)
            generateCodeForBinary(cg);
        else
            generateCodeForUnary(cg);
    }


    public void generateCodeForUnary(CodeGenerator cg) {
        if (refs[0] instanceof Variable) {
            Location varLoc = ((Variable) refs[0]).getLocation();
            if (varLoc instanceof RegisterLocation) {
                RegisterLocation vregLoc = (RegisterLocation) varLoc;
                cg.generateCodeFor(this, condition, vregLoc.getRegister());
            } else if (varLoc instanceof StackLocation) {
                StackLocation stackLoc = (StackLocation) varLoc;
                cg.generateCodeFor(this, condition, stackLoc.getDisplacement());
            } else {
                throw new IllegalArgumentException("Unknown location: " + varLoc);
            }
        } else if (refs[0] instanceof Constant) {
            // this probably won't happen, is should be folded earlier
            Constant con = (Constant) refs[0];
            cg.generateCodeFor(this, condition, con);
        } else {
            throw new IllegalArgumentException("Unknown operand: " + refs[0]);
        }
    }

    /**
     * Code generation is complicated by the permutations of addressing modes.
     * This is not as nice as it could be, but it could be worse!
     *
     * @see org.jnode.vm.compiler.ir.quad.Quad#generateCode(org.jnode.vm.compiler.ir.CodeGenerator)
     */
    public void generateCodeForBinary(CodeGenerator cg) {
        cg.checkLabel(getAddress());
        int op1Mode = refs[0].getAddressingMode();
        int op2Mode = refs[1].getAddressingMode();

        Object reg2 = null;
        if (op1Mode == Operand.MODE_REGISTER) {
            Variable var = (Variable) refs[0];
            RegisterLocation regLoc = (RegisterLocation) var.getLocation();
            reg2 = regLoc.getRegister();
        }

        Object reg3 = null;
        if (op2Mode == Operand.MODE_REGISTER) {
            Variable var = (Variable) refs[1];
            RegisterLocation regLoc = (RegisterLocation) var.getLocation();
            reg3 = regLoc.getRegister();
        }

        int disp2 = 0;
        if (op1Mode == Operand.MODE_STACK) {
            Variable var = (Variable) refs[0];
            StackLocation stackLoc = (StackLocation) var.getLocation();
            disp2 = stackLoc.getDisplacement();
        }

        int disp3 = 0;
        if (op2Mode == Operand.MODE_STACK) {
            Variable var = (Variable) refs[1];
            StackLocation stackLoc = (StackLocation) var.getLocation();
            disp3 = stackLoc.getDisplacement();
        }

        Constant c2 = null;
        if (op1Mode == Operand.MODE_CONSTANT) {
            c2 = (Constant) refs[0];
        }

        Constant c3 = null;
        if (op2Mode == Operand.MODE_CONSTANT) {
            c3 = (Constant) refs[1];
        }

        int aMode = (op1Mode << 8) | op2Mode;
        switch (aMode) {
            case MODE_CC:
                cg.generateCodeFor(this, c2, condition, c3);
                break;
            case MODE_CR:
                if (commutative && !cg.supports3AddrOps()) {
                    cg.generateCodeFor(this, reg3, condition, c2);
                } else {
                    cg.generateCodeFor(this, c2, condition, reg3);
                }
                break;
            case MODE_CS:
                cg.generateCodeFor(this, c2, condition, disp3);
                break;
            case MODE_RC:
                cg.generateCodeFor(this, reg2, condition, c3);
                break;
            case MODE_RR:
                if (commutative && !cg.supports3AddrOps()) {
                    cg.generateCodeFor(this, reg3, condition, reg2);
                } else {
                    cg.generateCodeFor(this, reg2, condition, reg3);
                }
                break;
            case MODE_RS:
                cg.generateCodeFor(this, reg2, condition, disp3);
                break;
            case MODE_SC:
                cg.generateCodeFor(this, disp2, condition, c3);
                break;
            case MODE_SR:
                if (commutative && !cg.supports3AddrOps()) {
                    cg.generateCodeFor(this, reg3, condition, disp2);
                } else {
                    cg.generateCodeFor(this, disp2, condition, reg3);
                }
                break;
            case MODE_SS:
                cg.generateCodeFor(this, disp2, condition, disp3);
                break;
            default:
                throw new IllegalArgumentException("Undefined addressing mode: " + aMode);
        }
    }
}
