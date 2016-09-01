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
 
package org.jnode.vm.compiler.ir;

import org.jnode.vm.compiler.ir.quad.ArrayAssignQuad;
import org.jnode.vm.compiler.ir.quad.ArrayLengthAssignQuad;
import org.jnode.vm.compiler.ir.quad.ArrayStoreQuad;
import org.jnode.vm.compiler.ir.quad.BinaryOperation;
import org.jnode.vm.compiler.ir.quad.BinaryQuad;
import org.jnode.vm.compiler.ir.quad.BranchCondition;
import org.jnode.vm.compiler.ir.quad.CheckcastQuad;
import org.jnode.vm.compiler.ir.quad.ConditionalBranchQuad;
import org.jnode.vm.compiler.ir.quad.ConstantClassAssignQuad;
import org.jnode.vm.compiler.ir.quad.ConstantRefAssignQuad;
import org.jnode.vm.compiler.ir.quad.ConstantStringAssignQuad;
import org.jnode.vm.compiler.ir.quad.InstanceofAssignQuad;
import org.jnode.vm.compiler.ir.quad.InterfaceCallAssignQuad;
import org.jnode.vm.compiler.ir.quad.InterfaceCallQuad;
import org.jnode.vm.compiler.ir.quad.LookupswitchQuad;
import org.jnode.vm.compiler.ir.quad.MonitorenterQuad;
import org.jnode.vm.compiler.ir.quad.MonitorexitQuad;
import org.jnode.vm.compiler.ir.quad.NewAssignQuad;
import org.jnode.vm.compiler.ir.quad.NewMultiArrayAssignQuad;
import org.jnode.vm.compiler.ir.quad.NewObjectArrayAssignQuad;
import org.jnode.vm.compiler.ir.quad.NewPrimitiveArrayAssignQuad;
import org.jnode.vm.compiler.ir.quad.RefAssignQuad;
import org.jnode.vm.compiler.ir.quad.RefStoreQuad;
import org.jnode.vm.compiler.ir.quad.SpecialCallAssignQuad;
import org.jnode.vm.compiler.ir.quad.SpecialCallQuad;
import org.jnode.vm.compiler.ir.quad.StaticCallAssignQuad;
import org.jnode.vm.compiler.ir.quad.StaticCallQuad;
import org.jnode.vm.compiler.ir.quad.StaticRefAssignQuad;
import org.jnode.vm.compiler.ir.quad.StaticRefStoreQuad;
import org.jnode.vm.compiler.ir.quad.TableswitchQuad;
import org.jnode.vm.compiler.ir.quad.ThrowQuad;
import org.jnode.vm.compiler.ir.quad.UnaryOperation;
import org.jnode.vm.compiler.ir.quad.UnaryQuad;
import org.jnode.vm.compiler.ir.quad.UnconditionalBranchQuad;
import org.jnode.vm.compiler.ir.quad.VarReturnQuad;
import org.jnode.vm.compiler.ir.quad.VariableRefAssignQuad;
import org.jnode.vm.compiler.ir.quad.VirtualCallAssignQuad;
import org.jnode.vm.compiler.ir.quad.VirtualCallQuad;
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
     * @return the register pool
     */
    public abstract RegisterPool<T> getRegisterPool();

    /**
     * Returns true of this CPU supports 3 address operands
     *
     * @return {@code true} if the 3 address operands are supported, otherwise {@code false}.
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
     * @param quad
     * @param reg1
     * @param disp2
     * @param operation
     * @param c3
     */
    public abstract void generateBinaryOP(BinaryQuad<T> quad, T reg1, int disp2,
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
    public abstract void generateBinaryOP(BinaryQuad<T> quad, int disp1, int disp2,
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
     * @param quad
     * @param disp1
     * @param disp2
     * @param operation
     * @param disp3
     */
    public abstract void generateBinaryOP(BinaryQuad<T> quad, int disp1, int disp2,
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

    /**
     *
     * @param quad
     */
    public abstract void generateCodeFor(StaticRefAssignQuad<T> quad);

    /**
     *
     * @param quad
     */
    public abstract void generateCodeFor(StaticRefStoreQuad<T> quad);

    /**
     *
     * @param quad
     */
    public abstract void generateCodeFor(StaticCallAssignQuad<T> quad);

    /**
     *
     * @param quad
     */
    public abstract void generateCodeFor(StaticCallQuad<T> quad);

    /**
     *
     * @param quad
     */
    public abstract void generateCodeFor(VirtualCallQuad quad);

    /**
     *
     * @param quad
     */
    public abstract void generateCodeFor(ArrayAssignQuad quad);


    public abstract void generateCodeFor(ArrayLengthAssignQuad quad);

    public abstract void generateCodeFor(ArrayStoreQuad quad);

    public abstract void generateCodeFor(CheckcastQuad<T> quad);

    public abstract void generateCodeFor(ConstantClassAssignQuad<T> quad);

    public abstract void generateCodeFor(ConstantStringAssignQuad<T> quad);

    public abstract void generateCodeFor(InstanceofAssignQuad<T> quad);

    public abstract void generateCodeFor(InterfaceCallAssignQuad quad);

    public abstract void generateCodeFor(InterfaceCallQuad quad);

    public abstract void generateCodeFor(LookupswitchQuad<T> quad);

    public abstract void generateCodeFor(MonitorenterQuad<T> quad);

    public abstract void generateCodeFor(MonitorexitQuad<T> quad);

    public abstract void generateCodeFor(NewAssignQuad<T> quad);

    public abstract void generateCodeFor(NewMultiArrayAssignQuad<T> quad);

    public abstract void generateCodeFor(NewObjectArrayAssignQuad<T> quad);

    public abstract void generateCodeFor(NewPrimitiveArrayAssignQuad<T> quad);

    public abstract void generateCodeFor(RefAssignQuad<T> quad);

    public abstract void generateCodeFor(RefStoreQuad<T> quad);

    public abstract void generateCodeFor(SpecialCallAssignQuad quad);

    public abstract void generateCodeFor(SpecialCallQuad quad);

    public abstract void generateCodeFor(TableswitchQuad<T> quad);

    public abstract void generateCodeFor(ThrowQuad<T> quad);

    public abstract void generateCodeFor(VirtualCallAssignQuad quad);
}
