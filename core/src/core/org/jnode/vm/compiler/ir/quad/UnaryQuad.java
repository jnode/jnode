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
public class UnaryQuad<T> extends AssignQuad<T> {

    private UnaryOperation operation;
    private Operand<T> refs[];

    /**
     * @param address
     * @param block
     * @param lhsIndex
     */
    public UnaryQuad(int address, IRBasicBlock<T> block, int lhsIndex,
                     UnaryOperation operation, int varIndex) {

        super(address, block, lhsIndex);
        this.operation = operation;
        refs = new Operand[]{getOperand(varIndex)};
    }

    /**
     * @see org.jnode.vm.compiler.ir.Quad#getReferencedOps()
     */
    public Operand<T>[] getReferencedOps() {
        return refs;
    }

    /**
     * @return
     */
    public Operand<T> getOperand() {
        return refs[0];
    }

    /**
     * @return
     */
    public UnaryOperation getOperation() {
        return operation;
    }

    public String toString() {
        return getAddress() + ": " + getLHS().toString() + " = " +
            operation.name() + " " + refs[0].toString();
    }

    /**
     * @see org.jnode.vm.compiler.ir.AssignQuad#propagate(org.jnode.vm.compiler.ir.Variable)
     */
    // TODO should fold constants, see BinaryQuad::propagate(...)
    public Operand<T> propagate(Variable<T> operand) {
        Quad<T> quad = foldConstants();
        if (quad instanceof ConstantRefAssignQuad) {
            //setDeadCode(true);
            ConstantRefAssignQuad<T> cop = (ConstantRefAssignQuad<T>) quad;
            return cop.getRHS();
        }
        return operand;
    }

    private Quad<T> foldConstants() {
        if (refs[0] instanceof Constant) {
            Constant<T> c = (Constant<T>) refs[0];

            switch (operation) {
                case I2L:
                    return new ConstantRefAssignQuad<T>(this.getAddress(), this.getBasicBlock(),
                        this.getLHS().getIndex(), c.i2l());

                case I2F:
                    return new ConstantRefAssignQuad<T>(this.getAddress(), this.getBasicBlock(),
                        this.getLHS().getIndex(), c.i2f());

                case I2D:
                    return new ConstantRefAssignQuad<T>(this.getAddress(), this.getBasicBlock(),
                        this.getLHS().getIndex(), c.i2d());

                case L2I:
                    return new ConstantRefAssignQuad<T>(this.getAddress(), this.getBasicBlock(),
                        this.getLHS().getIndex(), c.l2i());

                case L2F:
                    return new ConstantRefAssignQuad<T>(this.getAddress(), this.getBasicBlock(),
                        this.getLHS().getIndex(), c.l2f());

                case L2D:
                    return new ConstantRefAssignQuad<T>(this.getAddress(), this.getBasicBlock(),
                        this.getLHS().getIndex(), c.l2d());

                case F2I:
                    return new ConstantRefAssignQuad<T>(this.getAddress(), this.getBasicBlock(),
                        this.getLHS().getIndex(), c.f2i());

                case F2L:
                    return new ConstantRefAssignQuad<T>(this.getAddress(), this.getBasicBlock(),
                        this.getLHS().getIndex(), c.f2l());

                case F2D:
                    return new ConstantRefAssignQuad<T>(this.getAddress(), this.getBasicBlock(),
                        this.getLHS().getIndex(), c.f2d());

                case D2I:
                    return new ConstantRefAssignQuad<T>(this.getAddress(), this.getBasicBlock(),
                        this.getLHS().getIndex(), c.d2i());

                case D2L:
                    return new ConstantRefAssignQuad<T>(this.getAddress(), this.getBasicBlock(),
                        this.getLHS().getIndex(), c.d2l());

                case D2F:
                    return new ConstantRefAssignQuad<T>(this.getAddress(), this.getBasicBlock(),
                        this.getLHS().getIndex(), c.d2f());

                case I2B:
                    return new ConstantRefAssignQuad<T>(this.getAddress(), this.getBasicBlock(),
                        this.getLHS().getIndex(), c.i2b());

                case I2C:
                    return new ConstantRefAssignQuad<T>(this.getAddress(), this.getBasicBlock(),
                        this.getLHS().getIndex(), c.i2c());

                case I2S:
                    return new ConstantRefAssignQuad<T>(this.getAddress(), this.getBasicBlock(),
                        this.getLHS().getIndex(), c.i2s());

                case INEG:
                    return new ConstantRefAssignQuad<T>(this.getAddress(), this.getBasicBlock(),
                        this.getLHS().getIndex(), c.iNeg());

                case LNEG:
                    return new ConstantRefAssignQuad<T>(this.getAddress(), this.getBasicBlock(),
                        this.getLHS().getIndex(), c.lNeg());

                case FNEG:
                    return new ConstantRefAssignQuad<T>(this.getAddress(), this.getBasicBlock(),
                        this.getLHS().getIndex(), c.fNeg());

                case DNEG:
                    return new ConstantRefAssignQuad<T>(this.getAddress(), this.getBasicBlock(),
                        this.getLHS().getIndex(), c.dNeg());

                default:
                    throw new IllegalArgumentException("Don't know how to fold those yet...");
            }
        }
        return this;
    }


    /**
     * @see org.jnode.vm.compiler.ir.Quad#doPass2(org.jnode.util.BootableHashMap)
     */
    public void doPass2() {
        refs[0] = refs[0].simplify();
    }

    /**
     * @see org.jnode.vm.compiler.ir.Quad#generateCode(org.jnode.vm.compiler.ir.CodeGenerator)
     */
    public void generateCode(CodeGenerator<T> cg) {
        Variable<T> lhs = getLHS();
        Location<T> lhsLoc = lhs.getLocation();
        if (lhsLoc instanceof RegisterLocation) {
            RegisterLocation<T> regLoc = (RegisterLocation<T>) lhsLoc;
            T lhsReg = regLoc.getRegister();
            if (refs[0] instanceof Variable) {
                Variable<T> var = (Variable<T>) refs[0];
                Location<T> varLoc = var.getLocation();
                if (varLoc instanceof RegisterLocation) {
                    RegisterLocation<T> vregLoc = (RegisterLocation<T>) varLoc;
                    cg.generateCodeFor(this, lhsReg, operation, vregLoc.getRegister());
                } else if (varLoc instanceof StackLocation) {
                    StackLocation<T> stackLoc = (StackLocation<T>) varLoc;
                    cg.generateCodeFor(this, lhsReg, operation, stackLoc.getDisplacement());
                } else {
                    throw new IllegalArgumentException("Unknown location: " + varLoc);
                }
            } else if (refs[0] instanceof Constant) {
                // this probably won't happen, is should be folded earlier
                Constant<T> con = (Constant<T>) refs[0];
                cg.generateCodeFor(this, lhsReg, operation, con);
            } else {
                throw new IllegalArgumentException("Unknown operand: " + refs[0]);
            }
        } else if (lhsLoc instanceof StackLocation) {
            StackLocation<T> lhsStackLoc = (StackLocation<T>) lhsLoc;
            int lhsDisp = lhsStackLoc.getDisplacement();
            if (refs[0] instanceof Variable) {
                Variable<T> var = (Variable<T>) refs[0];
                Location<T> varLoc = var.getLocation();
                if (varLoc instanceof RegisterLocation) {
                    RegisterLocation<T> vregLoc = (RegisterLocation<T>) varLoc;
                    cg.generateCodeFor(this, lhsDisp, operation, vregLoc.getRegister());
                } else if (varLoc instanceof StackLocation) {
                    StackLocation<T> stackLoc = (StackLocation<T>) varLoc;
                    cg.generateCodeFor(this, lhsDisp, operation, stackLoc.getDisplacement());
                } else {
                    throw new IllegalArgumentException("Unknown location: " + varLoc);
                }
            } else if (refs[0] instanceof Constant) {
                Constant<T> con = (Constant<T>) refs[0];
                cg.generateCodeFor(this, lhsDisp, operation, con);
            } else {
                throw new IllegalArgumentException("Unknown operand: " + refs[0]);
            }
        } else {
            throw new IllegalArgumentException("Unknown location: " + lhsLoc);
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.AssignQuad#getLHSLiveAddress()
     */
    public int getLHSLiveAddress() {
        return this.getAddress() + 1;
    }
}
