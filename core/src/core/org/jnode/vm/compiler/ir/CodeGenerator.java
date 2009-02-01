/*
 * $Id$
 *
 * JNode.org
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
 
package org.jnode.vm.compiler.ir;

import org.jnode.vm.compiler.ir.quad.BinaryOperation;
import org.jnode.vm.compiler.ir.quad.BranchCondition;
import org.jnode.vm.compiler.ir.quad.ConditionalBranchQuad;
import org.jnode.vm.compiler.ir.quad.ConstantRefAssignQuad;
import org.jnode.vm.compiler.ir.quad.UnaryOperation;
import org.jnode.vm.compiler.ir.quad.UnaryQuad;
import org.jnode.vm.compiler.ir.quad.UnconditionalBranchQuad;
import org.jnode.vm.compiler.ir.quad.VarReturnQuad;
import org.jnode.vm.compiler.ir.quad.VariableRefAssignQuad;
import org.jnode.vm.compiler.ir.quad.VoidReturnQuad;

/**
 * @author Madhu Siddalingaiah
 * @author Levente S\u00e1ntha
 */
public abstract class CodeGenerator<T> {
    private static CodeGenerator cgInstance;

    public static void setCodeGenerator(CodeGenerator cg) {
        cgInstance = cg;
    }

    public static <T> CodeGenerator<T> getInstance() {
        return cgInstance;
    }

    public abstract void checkLabel(int address);

    /**
     * @return
     */
    public abstract RegisterPool<T> getRegisterPool();

    /**
     * Returns true of this CPU supports 3 address operands
     *
     * @return
     */
    public abstract boolean supports3AddrOps();

    /**
     * @param variables
     */
    public abstract void setSpilledVariables(Variable<T>[] variables);

    /**
     *
     */
    public abstract void emitHeader();

    /**
     * @param quad
     */
    public abstract void generateCodeFor(ConditionalBranchQuad<T> quad);

    /**
     * @param quad
     */
    public abstract void generateCodeFor(ConstantRefAssignQuad<T> quad);

    /**
     * @param quad
     */
    public abstract void generateCodeFor(UnconditionalBranchQuad<T> quad);

    /**
     * @param quad
     */
    public abstract void generateCodeFor(VariableRefAssignQuad<T> quad);

    /**
     * @param quad
     */
    public abstract void generateCodeFor(VarReturnQuad<T> quad);

    /**
     * @param quad
     */
    public abstract void generateCodeFor(VoidReturnQuad<T> quad);

    /**
     * @param quad
     * @param lhsReg
     * @param operation
     * @param con
     */
    public abstract void generateCodeFor(UnaryQuad<T> quad, Object lhsReg,
                                         UnaryOperation operation, Constant<T> con);

    /**
     * @param quad
     * @param lhsReg
     * @param operation
     * @param rhsReg
     */
    public abstract void generateCodeFor(UnaryQuad<T> quad, Object lhsReg,
                                         UnaryOperation operation, Object rhsReg);

    /**
     * @param quad
     * @param lhsReg
     * @param operation
     * @param rhsDisp
     */
    public abstract void generateCodeFor(UnaryQuad<T> quad, Object lhsReg,
                                         UnaryOperation operation, int rhsDisp);

    /**
     * @param quad
     * @param lhsDisp
     * @param operation
     * @param rhsReg
     */
    public abstract void generateCodeFor(UnaryQuad<T> quad, int lhsDisp,
                                         UnaryOperation operation, Object rhsReg);

    /**
     * @param quad
     * @param lhsDisp
     * @param operation
     * @param rhsDisp
     */
    public abstract void generateCodeFor(UnaryQuad<T> quad, int lhsDisp,
                                         UnaryOperation operation, int rhsDisp);

    /**
     * @param quad
     * @param lhsDisp
     * @param operation
     * @param con
     */
    public abstract void generateCodeFor(UnaryQuad<T> quad, int lhsDisp,
                                         UnaryOperation operation, Constant<T> con);

    /**
     * @param reg1
     * @param c2
     * @param operation
     * @param c3
     */
    public abstract void generateBinaryOP(T reg1, Constant<T> c2,
                                          BinaryOperation operation, Constant<T> c3);

    /**
     * @param reg1
     * @param c2
     * @param operation
     * @param reg3
     */
    public abstract void generateBinaryOP(T reg1, Constant<T> c2,
                                          BinaryOperation operation, T reg3);

    /**
     * @param reg1
     * @param c2
     * @param operation
     * @param disp3
     */
    public abstract void generateBinaryOP(T reg1, Constant<T> c2,
                                          BinaryOperation operation, int disp3);

    /**
     * @param reg1
     * @param reg2
     * @param operation
     * @param c3
     */
    public abstract void generateBinaryOP(T reg1, T reg2,
                                          BinaryOperation operation, Constant<T> c3);

    /**
     * @param reg1
     * @param reg2
     * @param operation
     * @param reg3
     */
    public abstract void generateBinaryOP(T reg1, T reg2,
                                          BinaryOperation operation, T reg3);

    /**
     * @param reg1
     * @param reg2
     * @param operation
     * @param disp3
     */
    public abstract void generateBinaryOP(T reg1, T reg2,
                                          BinaryOperation operation, int disp3);

    /**
     * @param reg1
     * @param disp2
     * @param operation
     * @param c3
     */
    public abstract void generateBinaryOP(T reg1, int disp2,
                                          BinaryOperation operation, Constant<T> c3);

    /**
     * @param reg1
     * @param disp2
     * @param operation
     * @param reg3
     */
    public abstract void generateBinaryOP(T reg1, int disp2,
                                          BinaryOperation operation, T reg3);

    /**
     * @param reg1
     * @param disp2
     * @param operation
     * @param disp3
     */
    public abstract void generateBinaryOP(T reg1, int disp2,
                                          BinaryOperation operation, int disp3);

    /**
     * @param disp1
     * @param c2
     * @param operation
     * @param c3
     */
    public abstract void generateBinaryOP(int disp1, Constant<T> c2,
                                          BinaryOperation operation, Constant<T> c3);

    /**
     * @param disp1
     * @param c2
     * @param operation
     * @param reg3
     */
    public abstract void generateBinaryOP(int disp1, Constant<T> c2,
                                          BinaryOperation operation, T reg3);

    /**
     * @param disp1
     * @param c2
     * @param operation
     * @param disp3
     */
    public abstract void generateBinaryOP(int disp1, Constant<T> c2,
                                          BinaryOperation operation, int disp3);

    /**
     * @param disp1
     * @param reg2
     * @param operation
     * @param c3
     */
    public abstract void generateBinaryOP(int disp1, T reg2,
                                          BinaryOperation operation, Constant<T> c3);

    /**
     * @param disp1
     * @param reg2
     * @param operation
     * @param reg3
     */
    public abstract void generateBinaryOP(int disp1, T reg2,
                                          BinaryOperation operation, T reg3);

    /**
     * @param disp1
     * @param reg2
     * @param operation
     * @param disp3
     */
    public abstract void generateBinaryOP(int disp1, T reg2,
                                          BinaryOperation operation, int disp3);

    /**
     * @param disp1
     * @param disp2
     * @param operation
     * @param c3
     */
    public abstract void generateBinaryOP(int disp1, int disp2,
                                          BinaryOperation operation, Constant<T> c3);

    /**
     * @param disp1
     * @param disp2
     * @param operation
     * @param reg3
     */
    public abstract void generateBinaryOP(int disp1, int disp2,
                                          BinaryOperation operation, T reg3);

    /**
     * @param disp1
     * @param disp2
     * @param operation
     * @param disp3
     */
    public abstract void generateBinaryOP(int disp1, int disp2,
                                          BinaryOperation operation, int disp3);

    /**
     * @param quad
     * @param condition
     * @param reg
     */
    public abstract void generateCodeFor(ConditionalBranchQuad<T> quad,
                                         BranchCondition condition, Object reg);

    /**
     * @param quad
     * @param condition
     * @param disp
     */
    public abstract void generateCodeFor(ConditionalBranchQuad<T> quad,
                                         BranchCondition condition, int disp);

    /**
     * @param quad
     * @param condition
     * @param reg
     */
    public abstract void generateCodeFor(ConditionalBranchQuad<T> quad,
                                         BranchCondition condition, Constant<T> reg);

    /**
     * @param quad
     * @param c1
     * @param condition
     * @param c2
     */
    public abstract void generateCodeFor(ConditionalBranchQuad<T> quad,
                                         Constant<T> c1, BranchCondition condition, Constant<T> c2);

    /**
     * @param quad
     * @param reg1
     * @param condition
     * @param c2
     */
    public abstract void generateCodeFor(ConditionalBranchQuad<T> quad,
                                         Object reg1, BranchCondition condition, Constant<T> c2);

    /**
     * @param quad
     * @param c1
     * @param condition
     * @param reg2
     */
    public abstract void generateCodeFor(ConditionalBranchQuad<T> quad,
                                         Constant<T> c1, BranchCondition condition, Object reg2);

    /**
     * @param quad
     * @param c1
     * @param condition
     * @param disp2
     */
    public abstract void generateCodeFor(ConditionalBranchQuad<T> quad,
                                         Constant<T> c1, BranchCondition condition, int disp2);

    /**
     * @param quad
     * @param reg1
     * @param condition
     * @param reg2
     */
    public abstract void generateCodeFor(ConditionalBranchQuad<T> quad,
                                         Object reg1, BranchCondition condition, Object reg2);

    /**
     * @param quad
     * @param reg1
     * @param condition
     * @param disp2
     */
    public abstract void generateCodeFor(ConditionalBranchQuad<T> quad,
                                         Object reg1, BranchCondition condition, int disp2);

    /**
     * @param quad
     * @param disp1
     * @param condition
     * @param c2
     */
    public abstract void generateCodeFor(ConditionalBranchQuad<T> quad, int disp1,
                                         BranchCondition condition, Constant<T> c2);

    /**
     * @param quad
     * @param disp1
     * @param condition
     * @param reg2
     */
    public abstract void generateCodeFor(ConditionalBranchQuad<T> quad, int disp1,
                                         BranchCondition condition, Object reg2);

    /**
     * @param quad
     * @param disp1
     * @param condition
     * @param disp2
     */
    public abstract void generateCodeFor(ConditionalBranchQuad<T> quad, int disp1,
                                         BranchCondition condition, int disp2);
}
