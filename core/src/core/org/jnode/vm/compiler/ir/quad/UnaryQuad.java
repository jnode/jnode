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

    public Operand<T>[] getReferencedOps() {
        return refs;
    }

    /**
     * @return the operand
     */
    public Operand<T> getOperand() {
        return refs[0];
    }

    /**
     * @return the operation
     */
    public UnaryOperation getOperation() {
        return operation;
    }

    public String toString() {
        return getAddress() + ": " + getLHS().toString() + " = " +
            operation.name() + ' ' + refs[0].toString();
    }

    public Operand<T> propagate(Variable<T> operand) {
        Quad<T> quad = foldConstants();
        if (quad instanceof ConstantRefAssignQuad) {
            setDeadCode(true);
            ConstantRefAssignQuad<T> cop = (ConstantRefAssignQuad<T>) quad;
            return cop.getRHS();
        }
        return operand;
    }

    private Quad<T> foldConstants() {
        if (refs[0] instanceof Constant) {
            Constant<T> c = (Constant<T>) refs[0];
            Constant<T> c2 = compute(c);
            int address = this.getAddress();
            IRBasicBlock<T> basicBlock = this.getBasicBlock();
            int index = this.getLHS().getIndex();
            return new ConstantRefAssignQuad<T>(address, basicBlock, index, c2);
        }
        return this;
    }

    private ConstantRefAssignQuad<T> foldConstants2() {
        if (refs[0] instanceof Constant) {
            Constant<T> c = (Constant<T>) refs[0];
            Constant<T> c2 = compute(c);
            int address = this.getAddress();
            int byteCodeAddress = this.getByteCodeAddress();
            IRBasicBlock<T> basicBlock = this.getBasicBlock();
            Variable<T> lhs = this.getLHS();
            return new ConstantRefAssignQuad<T>(address, byteCodeAddress, basicBlock, lhs, c2);
        } else {
            throw new IllegalArgumentException("Unary quad has a non-constant: " + this);
        }
    }

    private Constant<T> compute(Constant<T> c) {
        Constant<T> c2;
        switch (operation) {
            case I2L:
                c2 = c.i2l();
                break;

            case I2F:
                c2 = c.i2f();
                break;

            case I2D:
                c2 = c.i2d();
                break;

            case L2I:
                c2 = c.l2i();
                break;

            case L2F:
                c2 = c.l2f();
                break;

            case L2D:
                c2 = c.l2d();
                break;

            case F2I:
                c2 = c.f2i();
                break;

            case F2L:
                c2 = c.f2l();
                break;

            case F2D:
                c2 = c.f2d();
                break;

            case D2I:
                c2 = c.d2i();
                break;

            case D2L:
                c2 = c.d2l();
                break;

            case D2F:
                c2 = c.d2f();
                break;

            case I2B:
                c2 = c.i2b();
                break;

            case I2C:
                c2 = c.i2c();
                break;

            case I2S:
                c2 = c.i2s();
                break;

            case INEG:
                c2 = c.iNeg();
                break;

            case LNEG:
                c2 = c.lNeg();
                break;

            case FNEG:
                c2 = c.fNeg();
                break;

            case DNEG:
                c2 = c.dNeg();
                break;

            default:
                throw new IllegalArgumentException("Don't know how to fold those yet...");
        }
        return c2;
    }


    public void doPass2() {
        refs[0] = refs[0].simplify();
        getLHS().setAssignQuad(this);
    }

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
                cg.generateCodeFor(foldConstants2());
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
                // this probably won't happen, is should be folded earlier
                cg.generateCodeFor(foldConstants2());
            } else {
                throw new IllegalArgumentException("Unknown operand: " + refs[0]);
            }
        } else {
            throw new IllegalArgumentException("Unknown location: " + lhsLoc);
        }
    }

    public int getLHSLiveAddress() {
        return this.getAddress() + 1;
    }
}
