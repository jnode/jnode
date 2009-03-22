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
 
package org.jnode.vm.compiler.ir;

import static org.jnode.vm.compiler.ir.quad.BinaryOperation.DADD;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.DDIV;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.DMUL;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.DREM;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.DSUB;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.FADD;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.FDIV;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.FMUL;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.FREM;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.FSUB;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.IADD;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.IAND;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.IDIV;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.IMUL;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.IOR;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.IREM;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.ISHL;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.ISHR;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.ISUB;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.IUSHR;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.IXOR;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.LADD;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.LAND;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.LDIV;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.LMUL;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.LOR;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.LREM;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.LSHL;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.LSHR;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.LSUB;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.LUSHR;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.LXOR;
import static org.jnode.vm.compiler.ir.quad.BranchCondition.IFEQ;
import static org.jnode.vm.compiler.ir.quad.BranchCondition.IFGE;
import static org.jnode.vm.compiler.ir.quad.BranchCondition.IFGT;
import static org.jnode.vm.compiler.ir.quad.BranchCondition.IFLE;
import static org.jnode.vm.compiler.ir.quad.BranchCondition.IFLT;
import static org.jnode.vm.compiler.ir.quad.BranchCondition.IFNE;
import static org.jnode.vm.compiler.ir.quad.BranchCondition.IFNONNULL;
import static org.jnode.vm.compiler.ir.quad.BranchCondition.IFNULL;
import static org.jnode.vm.compiler.ir.quad.BranchCondition.IF_ACMPEQ;
import static org.jnode.vm.compiler.ir.quad.BranchCondition.IF_ACMPNE;
import static org.jnode.vm.compiler.ir.quad.BranchCondition.IF_ICMPEQ;
import static org.jnode.vm.compiler.ir.quad.BranchCondition.IF_ICMPGE;
import static org.jnode.vm.compiler.ir.quad.BranchCondition.IF_ICMPGT;
import static org.jnode.vm.compiler.ir.quad.BranchCondition.IF_ICMPLE;
import static org.jnode.vm.compiler.ir.quad.BranchCondition.IF_ICMPLT;
import static org.jnode.vm.compiler.ir.quad.BranchCondition.IF_ICMPNE;
import static org.jnode.vm.compiler.ir.quad.UnaryOperation.D2I;
import static org.jnode.vm.compiler.ir.quad.UnaryOperation.D2L;
import static org.jnode.vm.compiler.ir.quad.UnaryOperation.DNEG;
import static org.jnode.vm.compiler.ir.quad.UnaryOperation.F2D;
import static org.jnode.vm.compiler.ir.quad.UnaryOperation.F2I;
import static org.jnode.vm.compiler.ir.quad.UnaryOperation.F2L;
import static org.jnode.vm.compiler.ir.quad.UnaryOperation.FNEG;
import static org.jnode.vm.compiler.ir.quad.UnaryOperation.I2B;
import static org.jnode.vm.compiler.ir.quad.UnaryOperation.I2C;
import static org.jnode.vm.compiler.ir.quad.UnaryOperation.I2D;
import static org.jnode.vm.compiler.ir.quad.UnaryOperation.I2F;
import static org.jnode.vm.compiler.ir.quad.UnaryOperation.I2L;
import static org.jnode.vm.compiler.ir.quad.UnaryOperation.I2S;
import static org.jnode.vm.compiler.ir.quad.UnaryOperation.INEG;
import static org.jnode.vm.compiler.ir.quad.UnaryOperation.L2D;
import static org.jnode.vm.compiler.ir.quad.UnaryOperation.L2F;
import static org.jnode.vm.compiler.ir.quad.UnaryOperation.L2I;
import static org.jnode.vm.compiler.ir.quad.UnaryOperation.LNEG;

import java.util.Iterator;

import org.jnode.vm.bytecode.BytecodeParser;
import org.jnode.vm.bytecode.BytecodeVisitor;
import org.jnode.vm.classmgr.VmByteCode;
import org.jnode.vm.classmgr.VmConstClass;
import org.jnode.vm.classmgr.VmConstFieldRef;
import org.jnode.vm.classmgr.VmConstIMethodRef;
import org.jnode.vm.classmgr.VmConstMethodRef;
import org.jnode.vm.classmgr.VmConstString;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.compiler.ir.quad.BinaryOperation;
import org.jnode.vm.compiler.ir.quad.BinaryQuad;
import org.jnode.vm.compiler.ir.quad.ConditionalBranchQuad;
import org.jnode.vm.compiler.ir.quad.ConstantRefAssignQuad;
import org.jnode.vm.compiler.ir.quad.Quad;
import org.jnode.vm.compiler.ir.quad.UnaryQuad;
import org.jnode.vm.compiler.ir.quad.UnconditionalBranchQuad;
import org.jnode.vm.compiler.ir.quad.VarReturnQuad;
import org.jnode.vm.compiler.ir.quad.VariableRefAssignQuad;
import org.jnode.vm.compiler.ir.quad.VoidReturnQuad;

/**
 * Intermediate Representation Generator.
 * Visits bytecodes of a given method and translates them into a
 * list of Quads.
 */
public class IRGenerator<T> extends BytecodeVisitor {
    private final Constant<T> NULL_CONSTANT = Constant.getInstance(null);
    private int nArgs;
    private int nLocals;
    private int maxStack;
    private int stackOffset;
    private Variable<T>[] variables;
    private int address;
    private Iterator<IRBasicBlock<T>> basicBlockIterator;
    private IRBasicBlock<T> currentBlock;

    public IRGenerator(IRControlFlowGraph<T> cfg) {
        basicBlockIterator = cfg.iterator();
        currentBlock = basicBlockIterator.next();
    }

    public void setParser(BytecodeParser parser) {
    }

    public void startMethod(VmMethod method) {
        VmByteCode code = method.getBytecode();
        nArgs = method.getArgSlotCount();
        nLocals = code.getNoLocals();
        maxStack = code.getMaxStack();
        stackOffset = nLocals;
        variables = new Variable[nLocals + maxStack];
        int index = 0;
        for (int i = 0; i < nArgs; i += 1) {
            variables[index] = new MethodArgument<T>(Operand.UNKNOWN, index);
            index += 1;
        }
        for (int i = nArgs; i < nLocals; i += 1) {
            variables[index] = new LocalVariable<T>(Operand.UNKNOWN, index);
            index += 1;
        }
        for (int i = 0; i < maxStack; i += 1) {
            variables[index] = new StackVariable<T>(Operand.UNKNOWN, index);
            index += 1;
        }
        currentBlock.setVariables(variables);
    }

    public void endMethod() {
    }

    public void startInstruction(int address) {
        this.address = address;
        if (address >= currentBlock.getEndPC()) {
            currentBlock = basicBlockIterator.next();
            Iterator<IRBasicBlock<T>> pi = currentBlock.getPredecessors().iterator();
            if (!pi.hasNext()) {
                if (currentBlock.isStartOfExceptionHandler()) {
                    stackOffset = nLocals + 1;
                    currentBlock.setStackOffset(stackOffset);
                    // TODO need to set variables also...
                }
                return;
            }
            stackOffset = currentBlock.getStackOffset();
//          while (pi.hasNext()) {
//              IRBasicBlock irb = (IRBasicBlock) pi.next();
//              Variable[] prevVars = irb.getVariables();
//              if (prevVars != null) {
//                  int n = prevVars.length;
//                  variables = new Variable[n];
//                  for (int i=0; i<n; i+=1) {
//                      variables[i] = prevVars[i];
//                  }
//                  currentBlock.setVariables(variables);
//                  return;
//              }
//          }
//          currentBlock.setVariables(currentBlock.getIDominator().getVariables());
        }
        if (address < currentBlock.getStartPC() || address >= currentBlock.getEndPC()) {
            throw new AssertionError("instruction not in basic block!");
        }
    }

    public void endInstruction() {
    }

    public void visit_nop() {
    }

    public void visit_aconst_null() {
        currentBlock.add(new ConstantRefAssignQuad<T>(address, currentBlock, stackOffset,
            NULL_CONSTANT));
        stackOffset += 1;
    }

    public void visit_iconst(int value) {
        Constant<T> c = Constant.getInstance(value);
        Quad<T> quad = new ConstantRefAssignQuad<T>(address, currentBlock, stackOffset,
            c);
        currentBlock.add(quad);
        stackOffset += 1;
    }

    public void visit_lconst(long value) {
        Constant<T> c = Constant.getInstance(value);
        currentBlock.add(new ConstantRefAssignQuad<T>(address, currentBlock, stackOffset,
            c));
        stackOffset += 2;
    }

    public void visit_fconst(float value) {
        Constant<T> c = Constant.getInstance(value);
        currentBlock.add(new ConstantRefAssignQuad<T>(address, currentBlock, stackOffset,
            c));
        stackOffset += 1;
    }

    public void visit_dconst(double value) {
        Constant<T> c = Constant.getInstance(value);
        currentBlock.add(new ConstantRefAssignQuad<T>(address, currentBlock, stackOffset,
            c));
        stackOffset += 2;
    }

    public void visit_ldc(VmConstString value) {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public final void visit_ldc(VmConstClass value) {
        throw new Error("Not implemented yet");
    }

    public void visit_iload(int index) {
        variables[index].setType(Operand.INT);
        variables[stackOffset].setType(Operand.INT);
        VariableRefAssignQuad<T> assignQuad = new VariableRefAssignQuad<T>(address, currentBlock,
            stackOffset, index);
        currentBlock.add(assignQuad);
        stackOffset += 1;
    }

    public void visit_lload(int index) {
        variables[index].setType(Operand.LONG);
        variables[stackOffset].setType(Operand.LONG);
        currentBlock.add(new VariableRefAssignQuad<T>(address, currentBlock,
            stackOffset, index));
        stackOffset += 2;
    }

    public void visit_fload(int index) {
        variables[index].setType(Operand.FLOAT);
        variables[stackOffset].setType(Operand.FLOAT);
        currentBlock.add(new VariableRefAssignQuad<T>(address, currentBlock,
            stackOffset, index));
        stackOffset += 1;
    }

    public void visit_dload(int index) {
        variables[index].setType(Operand.DOUBLE);
        variables[stackOffset].setType(Operand.DOUBLE);
        currentBlock.add(new VariableRefAssignQuad<T>(address, currentBlock,
            stackOffset, index));
        stackOffset += 2;
    }

    public void visit_aload(int index) {
        variables[index].setType(Operand.REFERENCE);
        variables[stackOffset].setType(Operand.REFERENCE);
        currentBlock.add(new VariableRefAssignQuad<T>(address, currentBlock, stackOffset, index));
        stackOffset += 1;
    }

    public void visit_iaload() {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_laload() {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_faload() {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_daload() {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_aaload() {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_baload() {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_caload() {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_saload() {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_istore(int index) {
        stackOffset -= 1;
        variables[index].setType(Operand.INT);
        variables[stackOffset].setType(Operand.INT);
        currentBlock.add(new VariableRefAssignQuad<T>(address, currentBlock, index, stackOffset));
    }

    public void visit_lstore(int index) {
        stackOffset -= 2;
        variables[index].setType(Operand.LONG);
        variables[stackOffset].setType(Operand.LONG);
        currentBlock.add(new VariableRefAssignQuad<T>(address, currentBlock, index, stackOffset));
    }

    public void visit_fstore(int index) {
        stackOffset -= 1;
        variables[index].setType(Operand.FLOAT);
        variables[stackOffset].setType(Operand.FLOAT);
        currentBlock.add(new VariableRefAssignQuad<T>(address, currentBlock, index, stackOffset));
    }

    public void visit_dstore(int index) {
        stackOffset -= 2;
        variables[index].setType(Operand.DOUBLE);
        variables[stackOffset].setType(Operand.DOUBLE);
        currentBlock.add(new VariableRefAssignQuad<T>(address, currentBlock, index, stackOffset));
    }

    public void visit_astore(int index) {
        stackOffset -= 1;
        variables[index].setType(Operand.REFERENCE);
        variables[stackOffset].setType(Operand.REFERENCE);
        currentBlock.add(new VariableRefAssignQuad<T>(address, currentBlock, index, stackOffset));
    }

    public void visit_iastore() {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_lastore() {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_fastore() {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_dastore() {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_aastore() {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_bastore() {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_castore() {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_sastore() {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_pop() {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_pop2() {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_dup() {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_dup_x1() {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_dup_x2() {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_dup2() {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_dup2_x1() {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_dup2_x2() {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_swap() {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_iadd() {
        currentBlock.add(doBinaryQuad(IADD, Operand.INT));
    }

    public void visit_ladd() {
        currentBlock.add(doBinaryQuad(LADD, Operand.LONG));
    }

    public void visit_fadd() {
        currentBlock.add(doBinaryQuad(FADD, Operand.FLOAT));
    }

    public void visit_dadd() {
        currentBlock.add(doBinaryQuad(DADD, Operand.DOUBLE));
    }

    public void visit_isub() {
        currentBlock.add(doBinaryQuad(ISUB, Operand.INT));
    }

    public void visit_lsub() {
        currentBlock.add(doBinaryQuad(LSUB, Operand.LONG));
    }

    public void visit_fsub() {
        currentBlock.add(doBinaryQuad(FSUB, Operand.FLOAT));
    }

    public void visit_dsub() {
        currentBlock.add(doBinaryQuad(DSUB, Operand.DOUBLE));
    }

    public void visit_imul() {
        currentBlock.add(doBinaryQuad(IMUL, Operand.INT));
    }

    public void visit_lmul() {
        currentBlock.add(doBinaryQuad(LMUL, Operand.LONG));
    }

    public void visit_fmul() {
        currentBlock.add(doBinaryQuad(FMUL, Operand.FLOAT));
    }

    public void visit_dmul() {
        currentBlock.add(doBinaryQuad(DMUL, Operand.DOUBLE));
    }

    public void visit_idiv() {
        currentBlock.add(doBinaryQuad(IDIV, Operand.INT));
    }

    public void visit_ldiv() {
        currentBlock.add(doBinaryQuad(LDIV, Operand.LONG));
    }

    public void visit_fdiv() {
        currentBlock.add(doBinaryQuad(FDIV, Operand.FLOAT));
    }

    public void visit_ddiv() {
        currentBlock.add(doBinaryQuad(DDIV, Operand.DOUBLE));
    }

    public void visit_irem() {
        currentBlock.add(doBinaryQuad(IREM, Operand.INT));
    }

    public void visit_lrem() {
        currentBlock.add(doBinaryQuad(LREM, Operand.LONG));
    }

    public void visit_frem() {
        currentBlock.add(doBinaryQuad(FREM, Operand.FLOAT));
    }

    public void visit_drem() {
        currentBlock.add(doBinaryQuad(DREM, Operand.DOUBLE));
    }

    public void visit_ineg() {
        int s1 = stackOffset - 1;
        variables[s1].setType(Operand.INT);
        currentBlock.add(new UnaryQuad<T>(address, currentBlock, s1, INEG, s1));
    }

    public void visit_lneg() {
        int s1 = stackOffset - 2;
        variables[s1].setType(Operand.LONG);
        currentBlock.add(new UnaryQuad<T>(address, currentBlock, s1, LNEG, s1));
    }

    public void visit_fneg() {
        int s1 = stackOffset - 1;
        variables[s1].setType(Operand.FLOAT);
        currentBlock.add(new UnaryQuad<T>(address, currentBlock, s1, FNEG, s1));
    }

    public void visit_dneg() {
        int s1 = stackOffset - 2;
        variables[s1].setType(Operand.DOUBLE);
        currentBlock.add(new UnaryQuad<T>(address, currentBlock, s1, DNEG, s1));
    }

    public void visit_ishl() {
        currentBlock.add(doBinaryQuad(ISHL, Operand.INT));
    }

    public void visit_lshl() {
        stackOffset -= 1;
        int s1 = stackOffset - 2;
        variables[s1].setType(Operand.LONG);
        variables[stackOffset].setType(Operand.INT);
        currentBlock.add(new BinaryQuad<T>(address, currentBlock, s1, s1, LSHL, stackOffset));
    }

    public void visit_ishr() {
        currentBlock.add(doBinaryQuad(ISHR, Operand.INT));
    }

    public void visit_lshr() {
        stackOffset -= 1;
        int s1 = stackOffset - 2;
        variables[s1].setType(Operand.LONG);
        variables[stackOffset].setType(Operand.INT);
        currentBlock.add(new BinaryQuad<T>(address, currentBlock, s1, s1, LSHR, stackOffset));
    }

    public void visit_iushr() {
        currentBlock.add(doBinaryQuad(IUSHR, Operand.INT));
    }

    public void visit_lushr() {
        stackOffset -= 2;
        int s1 = stackOffset - 1;
        variables[s1].setType(Operand.INT);
        variables[stackOffset].setType(Operand.LONG);
        currentBlock.add(new BinaryQuad<T>(address, currentBlock, s1, s1, LUSHR, stackOffset));
    }

    public void visit_iand() {
        currentBlock.add(doBinaryQuad(IAND, Operand.INT));
    }

    public void visit_land() {
        currentBlock.add(doBinaryQuad(LAND, Operand.LONG));
    }

    public void visit_ior() {
        currentBlock.add(doBinaryQuad(IOR, Operand.INT));
    }

    public void visit_lor() {
        currentBlock.add(doBinaryQuad(LOR, Operand.LONG));
    }

    public void visit_ixor() {
        currentBlock.add(doBinaryQuad(IXOR, Operand.INT));
    }

    public void visit_lxor() {
        currentBlock.add(doBinaryQuad(LXOR, Operand.LONG));
    }

    public void visit_iinc(int index, int incValue) {
        variables[index].setType(Operand.INT);
        BinaryQuad<T> binaryQuad =
            new BinaryQuad<T>(address, currentBlock, index, index, IADD, new IntConstant<T>(incValue));
        currentBlock.add(binaryQuad.foldConstants());
    }

    public void visit_i2l() {
        stackOffset -= 1;
        variables[stackOffset].setType(Operand.LONG);
        currentBlock.add(new UnaryQuad<T>(address, currentBlock, stackOffset, I2L, stackOffset));
        stackOffset += 2;
    }

    public void visit_i2f() {
        stackOffset -= 1;
        variables[stackOffset].setType(Operand.FLOAT);
        currentBlock.add(new UnaryQuad<T>(address, currentBlock, stackOffset, I2F, stackOffset));
        stackOffset += 1;
    }

    public void visit_i2d() {
        stackOffset -= 1;
        variables[stackOffset].setType(Operand.DOUBLE);
        currentBlock.add(new UnaryQuad<T>(address, currentBlock, stackOffset, I2D, stackOffset));
        stackOffset += 2;
    }

    public void visit_l2i() {
        stackOffset -= 2;
        variables[stackOffset].setType(Operand.INT);
        currentBlock.add(new UnaryQuad<T>(address, currentBlock, stackOffset, L2I, stackOffset));
        stackOffset += 1;
    }

    public void visit_l2f() {
        stackOffset -= 2;
        variables[stackOffset].setType(Operand.FLOAT);
        currentBlock.add(new UnaryQuad<T>(address, currentBlock, stackOffset, L2F, stackOffset));
        stackOffset += 1;
    }

    public void visit_l2d() {
        stackOffset -= 2;
        variables[stackOffset].setType(Operand.DOUBLE);
        currentBlock.add(new UnaryQuad<T>(address, currentBlock, stackOffset, L2D, stackOffset));
        stackOffset += 2;
    }

    public void visit_f2i() {
        stackOffset -= 1;
        variables[stackOffset].setType(Operand.INT);
        currentBlock.add(new UnaryQuad<T>(address, currentBlock, stackOffset, F2I, stackOffset));
        stackOffset += 1;
    }

    public void visit_f2l() {
        stackOffset -= 1;
        variables[stackOffset].setType(Operand.LONG);
        currentBlock.add(new UnaryQuad<T>(address, currentBlock, stackOffset, F2L, stackOffset));
        stackOffset += 2;
    }

    public void visit_f2d() {
        stackOffset -= 1;
        variables[stackOffset].setType(Operand.DOUBLE);
        currentBlock.add(new UnaryQuad<T>(address, currentBlock, stackOffset, F2D, stackOffset));
        stackOffset += 2;
    }

    public void visit_d2i() {
        stackOffset -= 2;
        variables[stackOffset].setType(Operand.INT);
        currentBlock.add(new UnaryQuad<T>(address, currentBlock, stackOffset, D2I, stackOffset));
        stackOffset += 1;
    }

    public void visit_d2l() {
        stackOffset -= 2;
        variables[stackOffset].setType(Operand.LONG);
        currentBlock.add(new UnaryQuad<T>(address, currentBlock, stackOffset, D2L, stackOffset));
        stackOffset += 2;
    }

    public void visit_d2f() {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_i2b() {
        stackOffset -= 1;
        variables[stackOffset].setType(Operand.BYTE);
        currentBlock.add(new UnaryQuad<T>(address, currentBlock, stackOffset, I2B, stackOffset));
        stackOffset += 1;
    }

    public void visit_i2c() {
        stackOffset -= 1;
        variables[stackOffset].setType(Operand.CHAR);
        currentBlock.add(new UnaryQuad<T>(address, currentBlock, stackOffset, I2C, stackOffset));
        stackOffset += 1;
    }

    public void visit_i2s() {
        stackOffset -= 1;
        variables[stackOffset].setType(Operand.SHORT);
        currentBlock.add(new UnaryQuad<T>(address, currentBlock, stackOffset, I2S, stackOffset));
        stackOffset += 1;
    }

    public void visit_lcmp() {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_fcmpl() {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_fcmpg() {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_dcmpl() {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_dcmpg() {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_ifeq(int address) {
        int s1 = stackOffset - 1;
        Variable[] variables = currentBlock.getVariables();
        variables[s1].setType(Operand.INT);
        currentBlock.add(new ConditionalBranchQuad<T>(this.address, currentBlock,
            s1, IFEQ, address));
        stackOffset -= 1;
        setSuccessorStackOffset();
    }

    public void visit_ifne(int address) {
        int s1 = stackOffset - 1;
        Variable[] variables = currentBlock.getVariables();
        variables[s1].setType(Operand.INT);
        currentBlock.add(new ConditionalBranchQuad<T>(this.address, currentBlock,
            s1, IFNE, address));
        stackOffset -= 1;
        setSuccessorStackOffset();
    }

    public void visit_iflt(int address) {
        int s1 = stackOffset - 1;
        Variable[] variables = currentBlock.getVariables();
        variables[s1].setType(Operand.INT);
        currentBlock.add(new ConditionalBranchQuad<T>(this.address, currentBlock,
            s1, IFLT, address));
        stackOffset -= 1;
        setSuccessorStackOffset();
    }

    public void visit_ifge(int address) {
        int s1 = stackOffset - 1;
        Variable[] variables = currentBlock.getVariables();
        variables[s1].setType(Operand.INT);
        currentBlock.add(new ConditionalBranchQuad<T>(this.address, currentBlock,
            s1, IFGE, address));
        stackOffset -= 1;
        setSuccessorStackOffset();
    }

    public void visit_ifgt(int address) {
        int s1 = stackOffset - 1;
        Variable[] variables = currentBlock.getVariables();
        variables[s1].setType(Operand.INT);
        currentBlock.add(new ConditionalBranchQuad<T>(this.address, currentBlock,
            s1, IFGT, address));
        stackOffset -= 1;
        setSuccessorStackOffset();
    }

    public void visit_ifle(int address) {
        int s1 = stackOffset - 1;
        Variable[] variables = currentBlock.getVariables();
        variables[s1].setType(Operand.INT);
        currentBlock.add(new ConditionalBranchQuad<T>(this.address, currentBlock,
            s1, IFLE, address));
        stackOffset -= 1;
        setSuccessorStackOffset();
    }

    public void visit_if_icmpeq(int address) {
        int s1 = stackOffset - 2;
        int s2 = stackOffset - 1;
        Variable[] variables = currentBlock.getVariables();
        variables[s1].setType(Operand.INT);
        variables[s2].setType(Operand.INT);
        currentBlock.add(new ConditionalBranchQuad<T>(this.address, currentBlock,
            s1, IF_ICMPEQ, s2, address));
        stackOffset -= 2;
        setSuccessorStackOffset();
    }

    public void visit_if_icmpne(int address) {
        int s1 = stackOffset - 2;
        int s2 = stackOffset - 1;
        Variable[] variables = currentBlock.getVariables();
        variables[s1].setType(Operand.INT);
        variables[s2].setType(Operand.INT);
        currentBlock.add(new ConditionalBranchQuad<T>(this.address, currentBlock,
            s1, IF_ICMPNE, s2, address));
        stackOffset -= 2;
        setSuccessorStackOffset();
    }

    public void visit_if_icmplt(int address) {
        int s1 = stackOffset - 2;
        int s2 = stackOffset - 1;
        Variable[] variables = currentBlock.getVariables();
        variables[s1].setType(Operand.INT);
        variables[s2].setType(Operand.INT);
        currentBlock.add(new ConditionalBranchQuad<T>(this.address, currentBlock,
            s1, IF_ICMPLT, s2, address));
        stackOffset -= 2;
        setSuccessorStackOffset();
    }

    public void visit_if_icmpge(int address) {
        int s1 = stackOffset - 2;
        int s2 = stackOffset - 1;
        Variable[] variables = currentBlock.getVariables();
        variables[s1].setType(Operand.INT);
        variables[s2].setType(Operand.INT);
        currentBlock.add(new ConditionalBranchQuad<T>(this.address, currentBlock,
            s1, IF_ICMPGE, s2, address));
        stackOffset -= 2;
        setSuccessorStackOffset();
    }

    public void visit_if_icmpgt(int address) {
        int s1 = stackOffset - 2;
        int s2 = stackOffset - 1;
        Variable[] variables = currentBlock.getVariables();
        variables[s1].setType(Operand.INT);
        variables[s2].setType(Operand.INT);
        currentBlock.add(new ConditionalBranchQuad<T>(this.address, currentBlock,
            s1, IF_ICMPGT, s2, address));
        stackOffset -= 2;
        setSuccessorStackOffset();
    }

    public void visit_if_icmple(int address) {
        int s1 = stackOffset - 2;
        int s2 = stackOffset - 1;
        Variable[] variables = currentBlock.getVariables();
        variables[s1].setType(Operand.INT);
        variables[s2].setType(Operand.INT);
        currentBlock.add(new ConditionalBranchQuad<T>(this.address, currentBlock,
            s1, IF_ICMPLE, s2, address));
        stackOffset -= 2;
        setSuccessorStackOffset();
    }

    public void visit_if_acmpeq(int address) {
        int s1 = stackOffset - 2;
        int s2 = stackOffset - 1;
        Variable[] variables = currentBlock.getVariables();
        variables[s1].setType(Operand.REFERENCE);
        variables[s2].setType(Operand.REFERENCE);
        currentBlock.add(new ConditionalBranchQuad<T>(this.address, currentBlock,
            s1, IF_ACMPEQ, s2, address));
        stackOffset -= 2;
        setSuccessorStackOffset();
    }

    public void visit_if_acmpne(int address) {
        int s1 = stackOffset - 2;
        int s2 = stackOffset - 1;
        Variable[] variables = currentBlock.getVariables();
        variables[s1].setType(Operand.REFERENCE);
        variables[s2].setType(Operand.REFERENCE);
        currentBlock.add(new ConditionalBranchQuad<T>(this.address, currentBlock,
            s1, IF_ACMPNE, s2, address));
        stackOffset -= 2;
        setSuccessorStackOffset();
    }

    public void visit_goto(int address) {
        currentBlock.add(new UnconditionalBranchQuad<T>(this.address, currentBlock, address));
        setSuccessorStackOffset();
    }

    public void visit_jsr(int address) {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_ret(int index) {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_tableswitch(int defValue, int lowValue, int highValue, int[] addresses) {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_lookupswitch(int defValue, int[] matchValues, int[] addresses) {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_ireturn() {
        stackOffset -= 1;
        variables[stackOffset].setType(Operand.INT);
        currentBlock.add(new VarReturnQuad<T>(address, currentBlock, stackOffset));
    }

    public void visit_lreturn() {
        stackOffset -= 2;
        variables[stackOffset].setType(Operand.LONG);
        currentBlock.add(new VarReturnQuad<T>(address, currentBlock, stackOffset));
    }

    public void visit_freturn() {
        stackOffset -= 1;
        variables[stackOffset].setType(Operand.FLOAT);
        currentBlock.add(new VarReturnQuad<T>(address, currentBlock, stackOffset));
    }

    public void visit_dreturn() {
        stackOffset -= 2;
        variables[stackOffset].setType(Operand.DOUBLE);
        currentBlock.add(new VarReturnQuad<T>(address, currentBlock, stackOffset));
    }

    public void visit_areturn() {
        stackOffset -= 1;
        variables[stackOffset].setType(Operand.REFERENCE);
        currentBlock.add(new VarReturnQuad<T>(address, currentBlock, stackOffset));
    }

    public void visit_return() {
        currentBlock.add(new VoidReturnQuad<T>(address, currentBlock));
    }

    public void visit_getstatic(VmConstFieldRef fieldRef) {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_putstatic(VmConstFieldRef fieldRef) {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_getfield(VmConstFieldRef fieldRef) {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_putfield(VmConstFieldRef fieldRef) {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_invokevirtual(VmConstMethodRef methodRef) {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_invokespecial(VmConstMethodRef methodRef) {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_invokestatic(VmConstMethodRef methodRef) {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_invokeinterface(VmConstIMethodRef methodRef, int count) {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_new(VmConstClass clazz) {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_newarray(int type) {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_anewarray(VmConstClass clazz) {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_arraylength() {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_athrow() {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_checkcast(VmConstClass clazz) {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_instanceof(VmConstClass clazz) {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_monitorenter() {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_monitorexit() {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_multianewarray(VmConstClass clazz, int dimensions) {
        throw new IllegalArgumentException("byte code not yet supported");
    }

    public void visit_ifnull(int address) {
        int s1 = stackOffset - 1;
        Variable[] variables = currentBlock.getVariables();
        variables[s1].setType(Operand.REFERENCE);
        currentBlock.add(new ConditionalBranchQuad<T>(this.address, currentBlock,
            s1, IFNULL, address));
        stackOffset -= 1;
        setSuccessorStackOffset();
    }

    public void visit_ifnonnull(int address) {
        int s1 = stackOffset - 1;
        Variable[] variables = currentBlock.getVariables();
        variables[s1].setType(Operand.REFERENCE);
        currentBlock.add(new ConditionalBranchQuad<T>(this.address, currentBlock,
            s1, IFNONNULL, address));
        stackOffset -= 1;
        setSuccessorStackOffset();
    }

    // TODO
    public Quad<T> doBinaryQuad(BinaryOperation op, int type) {
        int sCount = 1;
        if (type == Operand.DOUBLE || type == Operand.LONG) {
            sCount = 2;
        }
        stackOffset -= sCount;
        int s1 = stackOffset - sCount;
        Variable[] variables = currentBlock.getVariables();
        variables[s1].setType(type);
        variables[stackOffset].setType(type);
        BinaryQuad<T> bop = new BinaryQuad<T>(address, currentBlock, s1, s1, op, stackOffset);
        return bop.foldConstants();
    }

    /**
     * @return the variables
     */
    public Variable<T>[] getVariables() {
        return variables;
    }

    /**
     * @return the number of args
     */
    public int getNoArgs() {
        return this.nArgs;
    }

    private void setSuccessorStackOffset() {
        // this is needed for terniary operators, the stack is not the
        // same as our dominator
        for (IRBasicBlock b : currentBlock.getSuccessors()) {
            b.setStackOffset(stackOffset);
        }
    }
}
