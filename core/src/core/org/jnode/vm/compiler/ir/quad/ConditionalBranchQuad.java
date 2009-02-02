/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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

import static org.jnode.vm.compiler.ir.AddressingMode.CONSTANT;
import static org.jnode.vm.compiler.ir.AddressingMode.REGISTER;
import static org.jnode.vm.compiler.ir.AddressingMode.STACK;
import static org.jnode.vm.compiler.ir.quad.BranchCondition.IF_ACMPEQ;
import static org.jnode.vm.compiler.ir.quad.BranchCondition.IF_ACMPNE;
import static org.jnode.vm.compiler.ir.quad.BranchCondition.IF_ICMPEQ;
import static org.jnode.vm.compiler.ir.quad.BranchCondition.IF_ICMPNE;

import org.jnode.vm.compiler.ir.AddressingMode;
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
 * @author Levente S\u00e1ntha
 */
public class ConditionalBranchQuad<T> extends BranchQuad<T> {

    private BranchCondition condition;

    private boolean commutative;

    private Operand<T>[] refs;

    private enum Mode {
        MODE_CC(CONSTANT, CONSTANT), MODE_CR(CONSTANT, REGISTER), MODE_CS(
        CONSTANT, STACK), MODE_RC(REGISTER, CONSTANT), MODE_RR(
        REGISTER, REGISTER), MODE_RS(REGISTER, STACK), MODE_SC(STACK,
        CONSTANT), MODE_SR(STACK, REGISTER), MODE_SS(STACK, STACK);

        private final AddressingMode m1;

        private final AddressingMode m2;

        private Mode(AddressingMode m1, AddressingMode m2) {
            this.m1 = m1;
            this.m2 = m2;
        }

        public static Mode valueOf(AddressingMode m1, AddressingMode m2) {
            for (Mode m : values()) {
                if ((m.m1 == m1) && (m.m2 == m2)) {
                    return m;
                }
            }
            throw new IllegalArgumentException();
        }

    }

    /**
     * @param address
     * @param targetAddress
     */
    public ConditionalBranchQuad(int address, IRBasicBlock<T> block,
                                 int varIndex1, BranchCondition condition, int varIndex2, int targetAddress) {

        super(address, block, targetAddress);
        if (!condition.isBinary()) {
            throw new IllegalArgumentException("can't use that condition here");
        }
        this.condition = condition;
        this.commutative = condition == IF_ICMPEQ || condition == IF_ICMPNE
            || condition == IF_ACMPEQ || condition == IF_ACMPNE;
        refs = new Operand[]{getOperand(varIndex1), getOperand(varIndex2)};
    }

    public ConditionalBranchQuad(int address, IRBasicBlock<T> block, int varIndex,
                                 BranchCondition condition, int targetAddress) {

        super(address, block, targetAddress);
        if (!condition.isUnary()) {
            throw new IllegalArgumentException("can't use that condition here");
        }
        this.condition = condition;
        this.commutative = condition == IF_ICMPEQ || condition == IF_ICMPNE
            || condition == IF_ACMPEQ || condition == IF_ACMPNE;
        refs = new Operand[]{getOperand(varIndex)};
    }

    /**
     * @see org.jnode.vm.compiler.ir.quad.Quad#getDefinedOp()
     */
    public Operand<T> getDefinedOp() {
        return null;
    }

    /**
     * @see org.jnode.vm.compiler.ir.quad.Quad#getReferencedOps()
     */
    public Operand<T>[] getReferencedOps() {
        return refs;
    }

    /**
     * @return
     */
    public Operand<T> getOperand1() {
        return refs[0];
    }

    /**
     * @return
     */
    public Operand<T> getOperand2() {
        return refs[1];
    }

    /**
     * @return
     */
    public BranchCondition getCondition() {
        return condition;
    }

    public String toString() {
        if (condition.isBinary()) {
            return getAddress() + ": if " + refs[0].toString() + " "
                + condition.getCondition() + " " + refs[1].toString()
                + " goto " + getTargetBlock();
        } else {
            return getAddress() + ": if " + refs[0].toString() + " "
                + condition.getCondition() + " goto " + getTargetBlock();
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.Quad#doPass2(org.jnode.util.BootableHashMap)
     */
    public void doPass2() {
        refs[0] = refs[0].simplify();
        if (refs.length > 1 && refs[1] != null) {
            refs[1] = refs[1].simplify();
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.Quad#generateCode(org.jnode.vm.compiler.ir.CodeGenerator)
     */
    public void generateCode(CodeGenerator<T> cg) {
        // cg.generateCodeFor(this);
        if (condition.isBinary()) {
            generateCodeForBinary(cg);
        } else {
            generateCodeForUnary(cg);
        }
    }

    public void generateCodeForUnary(CodeGenerator<T> cg) {
        if (refs[0] instanceof Variable) {
            Location<T> varLoc = ((Variable<T>) refs[0]).getLocation();
            if (varLoc instanceof RegisterLocation) {
                RegisterLocation<T> vregLoc = (RegisterLocation<T>) varLoc;
                cg.generateCodeFor(this, condition, vregLoc.getRegister());
            } else if (varLoc instanceof StackLocation) {
                StackLocation<T> stackLoc = (StackLocation<T>) varLoc;
                cg.generateCodeFor(this, condition, stackLoc.getDisplacement());
            } else {
                throw new IllegalArgumentException("Unknown location: "
                    + varLoc);
            }
        } else if (refs[0] instanceof Constant) {
            // this probably won't happen, is should be folded earlier
            Constant<T> con = (Constant<T>) refs[0];
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
    public void generateCodeForBinary(CodeGenerator<T> cg) {
        cg.checkLabel(getAddress());
        AddressingMode op1Mode = refs[0].getAddressingMode();
        AddressingMode op2Mode = refs[1].getAddressingMode();

        Object reg2 = null;
        if (op1Mode == REGISTER) {
            Variable<T> var = (Variable<T>) refs[0];
            RegisterLocation<T> regLoc = (RegisterLocation<T>) var.getLocation();
            reg2 = regLoc.getRegister();
        }

        Object reg3 = null;
        if (op2Mode == REGISTER) {
            Variable<T> var = (Variable<T>) refs[1];
            RegisterLocation<T> regLoc = (RegisterLocation<T>) var.getLocation();
            reg3 = regLoc.getRegister();
        }

        int disp2 = 0;
        if (op1Mode == STACK) {
            Variable<T> var = (Variable<T>) refs[0];
            StackLocation<T> stackLoc = (StackLocation<T>) var.getLocation();
            disp2 = stackLoc.getDisplacement();
        }

        int disp3 = 0;
        if (op2Mode == STACK) {
            Variable<T> var = (Variable<T>) refs[1];
            StackLocation<T> stackLoc = (StackLocation<T>) var.getLocation();
            disp3 = stackLoc.getDisplacement();
        }

        Constant<T> c2 = null;
        if (op1Mode == CONSTANT) {
            c2 = (Constant<T>) refs[0];
        }

        Constant<T> c3 = null;
        if (op2Mode == CONSTANT) {
            c3 = (Constant<T>) refs[1];
        }

        final Mode aMode = Mode.valueOf(op1Mode, op2Mode);
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
                throw new IllegalArgumentException("Undefined addressing mode: "
                    + aMode);
        }
    }
}
