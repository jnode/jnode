/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jnode.vm.compiler.ir.quad;

import org.jnode.vm.compiler.ir.AddressingMode;
import org.jnode.vm.compiler.ir.CodeGenerator;
import org.jnode.vm.compiler.ir.Constant;
import org.jnode.vm.compiler.ir.IRBasicBlock;
import org.jnode.vm.compiler.ir.Operand;
import org.jnode.vm.compiler.ir.RegisterLocation;
import org.jnode.vm.compiler.ir.StackLocation;
import org.jnode.vm.compiler.ir.Variable;

import static org.jnode.vm.compiler.ir.AddressingMode.CONSTANT;
import static org.jnode.vm.compiler.ir.AddressingMode.REGISTER;
import static org.jnode.vm.compiler.ir.AddressingMode.STACK;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.DADD;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.DMUL;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.FADD;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.FMUL;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.IADD;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.IAND;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.IMUL;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.IOR;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.IXOR;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.LADD;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.LAND;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.LMUL;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.LOR;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.LXOR;

/**
 * This class represents binary operations of the form:
 * <p/>
 * lhs = operand1 operation operand2, where operation is +, -, <<, |, && etc.
 * <p/>
 * The left hand side (lhs) is a Variable inherited from AssignQuad.
 *
 * @author Madhu Siddalingaiah
 * @author Levente S\u00e1ntha
 */
public class BinaryQuad<T> extends AssignQuad<T> {

    private enum Mode {
        /*
       * These are used to simplify addressing mode testing
       */
        MODE_RCC(REGISTER, CONSTANT, CONSTANT),
        MODE_RCR(REGISTER, CONSTANT, REGISTER),
        MODE_RCS(REGISTER, CONSTANT, STACK),
        MODE_RRC(REGISTER, REGISTER, CONSTANT),
        MODE_RRR(REGISTER, REGISTER, REGISTER),
        MODE_RRS(REGISTER, REGISTER, STACK),
        MODE_RSC(REGISTER, STACK, CONSTANT),
        MODE_RSR(REGISTER, STACK, REGISTER),
        MODE_RSS(REGISTER, STACK, STACK),
        MODE_SCC(STACK, CONSTANT, CONSTANT),
        MODE_SCR(STACK, CONSTANT, REGISTER),
        MODE_SCS(STACK, CONSTANT, STACK),
        MODE_SRC(STACK, REGISTER, CONSTANT),
        MODE_SRR(STACK, REGISTER, REGISTER),
        MODE_SRS(STACK, REGISTER, STACK),
        MODE_SSC(STACK, STACK, CONSTANT),
        MODE_SSR(STACK, STACK, REGISTER),
        MODE_SSS(STACK, STACK, STACK);

        private final AddressingMode m1;
        private final AddressingMode m2;
        private final AddressingMode m3;

        private Mode(AddressingMode m1, AddressingMode m2, AddressingMode m3) {
            this.m1 = m1;
            this.m2 = m2;
            this.m3 = m3;
        }

        public static Mode valueOf(AddressingMode m1, AddressingMode m2, AddressingMode m3) {
            for (Mode m : values()) {
                if ((m.m1 == m1) && (m.m2 == m2) && (m.m3 == m3)) {
                    return m;
                }
            }
            throw new IllegalArgumentException();
        }

    }

    private BinaryOperation operation;
    private Operand<T> refs[];
    private boolean commutative;

    /**
     * @param address
     * @param block
     * @param lhsIndex
     */
    public BinaryQuad(int address, IRBasicBlock<T> block, int lhsIndex,
                      int varIndex1, BinaryOperation operation, int varIndex2) {

        super(address, block, lhsIndex);
        this.operation = operation;
        refs = new Operand[]{getOperand(varIndex1), getOperand(varIndex2)};
        this.commutative =
            operation == IADD || operation == IMUL ||
                operation == LADD || operation == LMUL ||
                operation == FADD || operation == FMUL ||
                operation == DADD || operation == DMUL ||
                operation == IAND || operation == LAND ||
                operation == IOR || operation == LOR ||
                operation == IXOR || operation == LXOR;
    }

    public BinaryQuad(int address, IRBasicBlock<T> block, int lhsIndex,
                      int varIndex1, BinaryOperation operation, Operand<T> op2) {

        super(address, block, lhsIndex);
        this.operation = operation;
        refs = new Operand[]{getOperand(varIndex1), op2};
        this.commutative =
            operation == IADD || operation == IMUL ||
                operation == LADD || operation == LMUL ||
                operation == FADD || operation == FMUL ||
                operation == DADD || operation == DMUL ||
                operation == IAND || operation == LAND ||
                operation == IOR || operation == LOR ||
                operation == IXOR || operation == LXOR;
    }

    /**
     * @see org.jnode.vm.compiler.ir.quad.Quad#getReferencedOps()
     */
    public Operand<T>[] getReferencedOps() {
        return refs;
    }

    /**
     * @return the first operand
     */
    public Operand getOperand1() {
        return refs[0];
    }

    /**
     * @return the second operand
     */
    public Operand getOperand2() {
        return refs[1];
    }

    /**
     * @return the operation
     */
    public BinaryOperation getOperation() {
        return operation;
    }

    public String toString() {
        return getAddress() + ": " + getLHS().toString() + " = " +
            refs[0].toString() + ' ' + operation.getOperation() +
            ' ' + refs[1].toString();
    }

    /**
     * If refs[0] and refs[1] are both Constants, then fold them.
     *
     * @return resulting Quad after folding
     */
    public Quad<T> foldConstants() {
        if (refs[0] instanceof Constant && refs[1] instanceof Constant) {
            Constant<T> c1 = (Constant<T>) refs[0];
            Constant<T> c2 = (Constant<T>) refs[1];
            Constant<T> c3 = compute(c1, c2);
            int address = this.getAddress();
            IRBasicBlock<T> basicBlock = this.getBasicBlock();
            int index = this.getLHS().getIndex();
            return new ConstantRefAssignQuad<T>(address, basicBlock, index, c3);
        }
        return this;
    }
    /**
     * If refs[0] and refs[1] are both Constants, then fold them.
     *
     * @return resulting Quad after folding
     */
    public ConstantRefAssignQuad<T> foldConstants2() {
        if (refs[0] instanceof Constant && refs[1] instanceof Constant) {
            Constant<T> c1 = (Constant<T>) refs[0];
            Constant<T> c2 = (Constant<T>) refs[1];
            Constant<T> c3 = compute(c1, c2);
            int address = this.getAddress();
            int byteCodeAddress = this.getByteCodeAddress();
            IRBasicBlock<T> basicBlock = this.getBasicBlock();
            Variable<T> lhs = this.getLHS();
            return new ConstantRefAssignQuad<T>(address, byteCodeAddress, basicBlock, lhs, c3);
        } else {
            throw new IllegalArgumentException("Binary quad has a non-constant operand: " + this);
        }
    }


    private Constant<T> compute(Constant<T> c1, Constant<T> c2) {
        Constant<T> c3;
        switch (operation) {
            case IADD:
                c3 = c1.iAdd(c2);
                break;

            case ISUB:
                c3 = c1.iSub(c2);
                break;

            case IMUL:
                c3 = c1.iMul(c2);
                break;

            case IDIV:
                c3 = c1.iDiv(c2);
                break;

            case IREM:
                c3 = c1.iRem(c2);
                break;

            case IAND:
                c3 = c1.iAnd(c2);
                break;

            case IOR:
                c3 = c1.iOr(c2);
                break;

            case IXOR:
                c3 = c1.iXor(c2);
                break;

            case ISHL:
                c3 = c1.iShl(c2);
                break;

            case ISHR:
                c3 = c1.iShr(c2);
                break;

            case IUSHR:
                c3 = c1.iUshr(c2);
                break;

            case LADD:
                c3 = c1.lAdd(c2);
                break;

            case LSUB:
                c3 = c1.lSub(c2);
                break;

            case LMUL:
                c3 = c1.lMul(c2);
                break;

            case LDIV:
                c3 = c1.lDiv(c2);
                break;

            case LREM:
                c3 = c1.lRem(c2);
                break;

            case LAND:
                c3 = c1.lAnd(c2);
                break;

            case LOR:
                c3 = c1.lOr(c2);
                break;

            case LXOR:
                c3 = c1.lXor(c2);
                break;

            case LSHL:
                c3 = c1.lShl(c2);
                break;

            case LSHR:
                c3 = c1.lShr(c2);
                break;

            case LUSHR:
                c3 = c1.lUshr(c2);
                break;

            case FADD:
                c3 = c1.fAdd(c2);
                break;

            case FSUB:
                c3 = c1.fSub(c2);
                break;

            case FMUL:
                c3 = c1.fMul(c2);
                break;

            case FDIV:
                c3 = c1.fDiv(c2);
                break;

            case FREM:
                c3 = c1.fRem(c2);
                break;

            case LCMP:
                c3 = c1.lCmp(c2);
                break;

            case DADD:
                c3 = c1.dAdd(c2);
                break;

            case DSUB:
                c3 = c1.dSub(c2);
                break;

            case DMUL:
                c3 = c1.dMul(c2);
                break;

            case DDIV:
                c3 = c1.dDiv(c2);
                break;

            case DREM:
                c3 = c1.dRem(c2);
                break;

            default:
                throw new IllegalArgumentException("Don't know how to fold those yet...");
        }
        return c3;
    }

    /**
     * @see org.jnode.vm.compiler.ir.quad.AssignQuad#propagate(org.jnode.vm.compiler.ir.Variable)
     */
    public Operand<T> propagate(Variable<T> operand) {
        Quad<T> quad = foldConstants();
        if (quad instanceof ConstantRefAssignQuad) {
            setDeadCode(true);
            ConstantRefAssignQuad<T> cop = (ConstantRefAssignQuad<T>) quad;
            return cop.getRHS();
        }
        return operand;
    }

    /**
     * Simplifies operands by calling operand.simplify().
     * simplify will combine phi references and propagate copies
     * This method will also update liveness of operands by setting last use addr
     *
     * @see org.jnode.vm.compiler.ir.quad.Quad#doPass2()
     */
    public void doPass2() {
        refs[0] = refs[0].simplify();
        refs[1] = refs[1].simplify();
        getLHS().setAssignQuad(this);
    }

    /**
     * Code generation is complicated by the permutations of addressing modes.
     * This is not as nice as it could be, but it could be worse!
     *
     * @see org.jnode.vm.compiler.ir.quad.Quad#generateCode(org.jnode.vm.compiler.ir.CodeGenerator)
     */
    public void generateCode(CodeGenerator<T> cg) {
        cg.checkLabel(getAddress());
        Variable<T> lhs = getLHS();
        final AddressingMode lhsMode = lhs.getAddressingMode();
        final AddressingMode op1Mode = refs[0].getAddressingMode();
        final AddressingMode op2Mode = refs[1].getAddressingMode();

        T reg1 = null;
        if (lhsMode == AddressingMode.REGISTER) {
            RegisterLocation<T> regLoc = (RegisterLocation<T>) lhs.getLocation();
            reg1 = regLoc.getRegister();
        }
        T reg2 = null;
        if (op1Mode == AddressingMode.REGISTER) {
            Variable<T> var = (Variable<T>) refs[0];
            RegisterLocation<T> regLoc = (RegisterLocation<T>) var.getLocation();
            reg2 = regLoc.getRegister();
        }
        T reg3 = null;
        if (op2Mode == AddressingMode.REGISTER) {
            Variable<T> var = (Variable<T>) refs[1];
            RegisterLocation<T> regLoc = (RegisterLocation<T>) var.getLocation();
            reg3 = regLoc.getRegister();
        }

        int disp1 = 0;
        if (lhsMode == AddressingMode.STACK) {
            StackLocation<T> stackLoc = (StackLocation<T>) lhs.getLocation();
            disp1 = stackLoc.getDisplacement();
        }
        int disp2 = 0;
        if (op1Mode == AddressingMode.STACK) {
            Variable<T> var = (Variable<T>) refs[0];
            StackLocation<T> stackLoc = (StackLocation<T>) var.getLocation();
            disp2 = stackLoc.getDisplacement();
        }
        int disp3 = 0;
        if (op2Mode == AddressingMode.STACK) {
            Variable<T> var = (Variable<T>) refs[1];
            StackLocation<T> stackLoc = (StackLocation<T>) var.getLocation();
            disp3 = stackLoc.getDisplacement();
        }

        Constant<T> c2 = null;
        if (op1Mode == AddressingMode.CONSTANT) {
            c2 = (Constant<T>) refs[0];
        }
        Constant<T> c3 = null;
        if (op2Mode == AddressingMode.CONSTANT) {
            c3 = (Constant<T>) refs[1];
        }

        final Mode aMode = Mode.valueOf(lhsMode, op1Mode, op2Mode);
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
                cg.generateBinaryOP(this, reg1, disp2, operation, c3);
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
                    cg.generateBinaryOP(this, disp1, disp3, operation, c2);
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
                cg.generateBinaryOP(this, disp1, disp2, operation, c3);
                break;
            case MODE_SSR:
                cg.generateBinaryOP(disp1, disp2, operation, reg3);
                break;
            case MODE_SSS:
                if (disp1 == disp3 && commutative && !cg.supports3AddrOps()) {
                    cg.generateBinaryOP(this, disp1, disp3, operation, disp2);
                } else {
                    cg.generateBinaryOP(this, disp1, disp2, operation, disp3);
                }
                break;
            default:
                throw new IllegalArgumentException("Undefined addressing mode: " + aMode);
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.quad.AssignQuad#getLHSLiveAddress()
     */
    public int getLHSLiveAddress() {
        CodeGenerator<T> cg = CodeGenerator.getInstance();
        int addr = this.getAddress();
        if (cg.supports3AddrOps() || commutative) {
            return addr + 1;
        }
        return addr;
    }
}
