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

import static org.jnode.vm.compiler.ir.quad.BinaryOperation.DADD;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.DCMPG;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.DCMPL;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.DDIV;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.DMUL;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.DREM;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.DSUB;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.FADD;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.FCMPG;
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.FCMPL;
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
import static org.jnode.vm.compiler.ir.quad.BinaryOperation.LCMP;
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
import static org.jnode.vm.compiler.ir.quad.UnaryOperation.D2F;
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

import java.util.List;
import org.jnode.vm.JvmType;
import org.jnode.vm.bytecode.BytecodeParser;
import org.jnode.vm.bytecode.BytecodeVisitor;
import org.jnode.vm.classmgr.Signature;
import org.jnode.vm.classmgr.VmByteCode;
import org.jnode.vm.classmgr.VmClassLoader;
import org.jnode.vm.classmgr.VmConstClass;
import org.jnode.vm.classmgr.VmConstFieldRef;
import org.jnode.vm.classmgr.VmConstIMethodRef;
import org.jnode.vm.classmgr.VmConstMethodRef;
import org.jnode.vm.classmgr.VmConstString;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.compiler.ir.quad.ArrayAssignQuad;
import org.jnode.vm.compiler.ir.quad.ArrayLengthAssignQuad;
import org.jnode.vm.compiler.ir.quad.ArrayStoreQuad;
import org.jnode.vm.compiler.ir.quad.AssignQuad;
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
import org.jnode.vm.compiler.ir.quad.MonitorenterQuad;
import org.jnode.vm.compiler.ir.quad.MonitorexitQuad;
import org.jnode.vm.compiler.ir.quad.NewMultiArrayAssignQuad;
import org.jnode.vm.compiler.ir.quad.NewObjectArrayAssignQuad;
import org.jnode.vm.compiler.ir.quad.NewPrimitiveArrayAssignQuad;
import org.jnode.vm.compiler.ir.quad.NewAssignQuad;
import org.jnode.vm.compiler.ir.quad.Quad;
import org.jnode.vm.compiler.ir.quad.RefAssignQuad;
import org.jnode.vm.compiler.ir.quad.RefStoreQuad;
import org.jnode.vm.compiler.ir.quad.SpecialCallAssignQuad;
import org.jnode.vm.compiler.ir.quad.SpecialCallQuad;
import org.jnode.vm.compiler.ir.quad.StaticCallAssignQuad;
import org.jnode.vm.compiler.ir.quad.StaticCallQuad;
import org.jnode.vm.compiler.ir.quad.StaticRefAssignQuad;
import org.jnode.vm.compiler.ir.quad.StaticRefStoreQuad;
import org.jnode.vm.compiler.ir.quad.LookupswitchQuad;
import org.jnode.vm.compiler.ir.quad.TableswitchQuad;
import org.jnode.vm.compiler.ir.quad.ThrowQuad;
import org.jnode.vm.compiler.ir.quad.UnaryQuad;
import org.jnode.vm.compiler.ir.quad.UnconditionalBranchQuad;
import org.jnode.vm.compiler.ir.quad.VarReturnQuad;
import org.jnode.vm.compiler.ir.quad.VariableRefAssignQuad;
import org.jnode.vm.compiler.ir.quad.VirtualCallAssignQuad;
import org.jnode.vm.compiler.ir.quad.VirtualCallQuad;
import org.jnode.vm.compiler.ir.quad.VoidReturnQuad;
import org.jnode.vm.facade.TypeSizeInfo;

/**
 * Intermediate Representation Generator.
 * Visits bytecodes of a given method and translates them into a
 * list of Quads.
 */
public class IRGenerator<T> extends BytecodeVisitor {
    private final Constant<T> NULL_CONSTANT = Constant.getInstance(0); //Constant.getInstance(null);
    private int nArgs;
    private int nLocals;
    private int maxStack;
    private int stackOffset;
    private Variable<T>[] variables;
    private int address;
    private Iterator<IRBasicBlock<T>> basicBlockIterator;
    private IRBasicBlock<T> currentBlock;
    private TypeSizeInfo typeSizeInfo;
    private VmClassLoader vmClassLoader;

    public IRGenerator(IRControlFlowGraph<T> cfg, TypeSizeInfo typeSizeInfo, VmClassLoader loader) {
        basicBlockIterator = cfg.iterator();
        currentBlock = basicBlockIterator.next();
        this.typeSizeInfo = typeSizeInfo;
        this.vmClassLoader = loader;
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
        int argCount = method.getNoArguments();
        if (!method.isStatic()) {
            variables[index] = new MethodArgument<T>(Operand.REFERENCE, index);
            index += 1;
        }
        for (int i = 0; i < argCount; i++) {
            VmType argType = method.getArgumentType(i);
            int jvmType = argType.isPrimitive() ? argType.getJvmType() : JvmType.REFERENCE;
            variables[index] = new MethodArgument<T>(Operand.UNKNOWN, index);
            variables[index].setTypeFromJvmType(jvmType);
            index += 1;
            if (isCategory2(jvmType)) {
                variables[index] = new MethodArgument<T>(Operand.UNKNOWN, index);
                variables[index].setTypeFromJvmType(jvmType);
                index += 1;
            }
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
            IRBasicBlock lastBlock = currentBlock;
            currentBlock = basicBlockIterator.next();
//            Iterator<IRBasicBlock<T>> pi = currentBlock.getPredecessors().iterator();
//            if (!pi.hasNext()) {
            if (currentBlock.isStartOfExceptionHandler()) {
                stackOffset = nLocals;
                currentBlock.setStackOffset(stackOffset);
                currentBlock.setVariables(variables.clone());
//                    currentBlock.getVariables()[stackOffset] = new ExceptionArgument(Operand.REFERENCE, stackOffset);
                stackOffset++;
                // TODO need to set variables also...
            } else {
//                return;
//            }
                if (lastBlock != currentBlock.getIDominator()) {
                    stackOffset = currentBlock.getStackOffset();
                }
            }
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
        variables[stackOffset].setType(Operand.REFERENCE);
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
        variables[stackOffset].setType(Operand.LONG);
        variables[stackOffset + 1].setType(Operand.LONG);
        currentBlock.add(new ConstantRefAssignQuad<T>(address, currentBlock, stackOffset, c));
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
        currentBlock.add(new ConstantRefAssignQuad<T>(address, currentBlock, stackOffset, c));
        stackOffset += 2;
    }

    public void visit_ldc(VmConstString value) {
        currentBlock.add(new ConstantStringAssignQuad<T>(address, currentBlock, stackOffset, value));
        stackOffset++;
    }

    public final void visit_ldc(VmConstClass value) {
        currentBlock.add(new ConstantClassAssignQuad<T>(address, currentBlock, stackOffset, value));
        stackOffset++;
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
        variables[stackOffset + 1].setType(Operand.LONG);
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
        visitArrayLoad(Operand.INT);
    }

    public void visit_laload() {
        visitArrayLoad(Operand.LONG);
    }

    public void visit_faload() {
        visitArrayLoad(Operand.FLOAT);
    }

    public void visit_daload() {
        visitArrayLoad(Operand.DOUBLE);
    }

    public void visit_aaload() {
        visitArrayLoad(Operand.REFERENCE);
    }

    public void visit_baload() {
        visitArrayLoad(Operand.BYTE);
    }

    public void visit_caload() {
        visitArrayLoad(Operand.CHAR);
    }

    public void visit_saload() {
        visitArrayLoad(Operand.SHORT);
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
        visitArrayStore(Operand.INT);
    }

    public void visit_lastore() {
        visitArrayStore(Operand.LONG);
    }

    public void visit_fastore() {
        visitArrayStore(Operand.FLOAT);
    }

    public void visit_dastore() {
        visitArrayStore(Operand.DOUBLE);
    }

    public void visit_aastore() {
        visitArrayStore(Operand.REFERENCE);
    }

    public void visit_bastore() {
        visitArrayStore(Operand.BYTE);
    }

    public void visit_castore() {
        visitArrayStore(Operand.CHAR);
    }

    public void visit_sastore() {
        visitArrayStore(Operand.SHORT);
    }

    public void visit_pop() {
        stackOffset -= 1;
    }

    public void visit_pop2() {
        stackOffset -= 2;
    }

    public void visit_dup() {
        int index = stackOffset;
        stackOffset -= 1;
        currentBlock.add(new VariableRefAssignQuad<T>(address, currentBlock, index, stackOffset));
        fixType(); //todo fix type for other dups like here
        stackOffset += 2;
    }

    private void fixType() {
        List<Quad<T>> quadList = currentBlock.getQuads();
        VariableRefAssignQuad<T> currentQuad = (VariableRefAssignQuad<T>) quadList.get(quadList.size() - 1);
        if (currentQuad.getLHS().getType() == JvmType.UNKNOWN) {
            Operand<T> rhs = currentQuad.getRHS();
            List<Quad<T>> quads = quadList;
            for (int i = quads.size(); i -- > 0;){
                Quad q = quads.get(i);
                if (q instanceof AssignQuad) {
                    AssignQuad a = (AssignQuad) q;
                    Variable lhs = a.getLHS();
                    if (lhs.equals(rhs)) {
                        rhs.setType(lhs.getType());
                        currentQuad.getLHS().setType(lhs.getType());
                        break;
                    }
                }
            }
            if (currentQuad.getLHS().getType() == JvmType.UNKNOWN) {
                //todo throw exception here when types should be ok
            }
        }
    }

    public void visit_dup_x1() {
//        int index = stackOffset;
//        stackOffset -= 1;
//        currentBlock.add(new VariableRefAssignQuad<T>(address, currentBlock, index, stackOffset - 1));
//        currentBlock.add(new VariableRefAssignQuad<T>(address, currentBlock, index - 2, stackOffset));
//        currentBlock.add(new VariableRefAssignQuad<T>(address, currentBlock, index - 1, stackOffset + 1));
//        currentBlock.add(new VariableRefAssignQuad<T>(address, currentBlock, index, stackOffset - 1));
//        stackOffset += 2;
        int index = stackOffset;
        stackOffset -= 1;
        currentBlock.add(new VariableRefAssignQuad<T>(address, currentBlock, index, stackOffset));
        fixType();
        currentBlock.add(new VariableRefAssignQuad<T>(address, currentBlock, index - 1, stackOffset - 1));
        fixType();
        currentBlock.add(new VariableRefAssignQuad<T>(address, currentBlock, index - 2, stackOffset + 1));
        fixType();
        stackOffset += 2;
    }

    public void visit_dup_x2() {
        //form 1
        if (!isCategory2(getVariables()[stackOffset - 1].getType()) &&
            !isCategory2(getVariables()[stackOffset - 2].getType())) {
            int index = stackOffset;
            stackOffset -= 1;
            currentBlock.add(new VariableRefAssignQuad<T>(address, currentBlock, index, stackOffset));
            fixType();
            currentBlock.add(new VariableRefAssignQuad<T>(address, currentBlock, index - 1, stackOffset - 1));
            fixType();
            currentBlock.add(new VariableRefAssignQuad<T>(address, currentBlock, index - 2, stackOffset - 2));
            fixType();
            currentBlock.add(new VariableRefAssignQuad<T>(address, currentBlock, index - 3, stackOffset + 1));
            fixType();
            stackOffset += 2;
        } else {
            throw new IllegalArgumentException("byte code not yet supported");
        }
    }

    public void visit_dup2() {
        int index = stackOffset;

        Variable var = currentBlock.getVariables()[index - 2];
        if (var.getType() == Operand.LONG || var.getType() == Operand.DOUBLE) {
            stackOffset -= 2;
            currentBlock.add(new VariableRefAssignQuad<T>(address, currentBlock, index, stackOffset));
            fixType();
            stackOffset += 4;
        } else {
            stackOffset -= 1;
            currentBlock.add(new VariableRefAssignQuad<T>(address, currentBlock, index + 1, stackOffset));
            fixType();
            stackOffset -= 1;
            currentBlock.add(new VariableRefAssignQuad<T>(address, currentBlock, index, stackOffset));
            fixType();
            stackOffset += 4;
        }
    }

    public void visit_dup2_x1() {
        //todo
        //form 2;
        if (isCategory2(getVariables()[stackOffset - 1].getType()) &&
            !isCategory2(getVariables()[stackOffset - 3].getType())) {
            int index = stackOffset;
            stackOffset -= 2;
            currentBlock.add(new VariableRefAssignQuad<T>(address, currentBlock, index, stackOffset));
            fixType();
            //currentBlock.add(new VariableRefAssignQuad<T>(address, currentBlock, index - 1, stackOffset - 1));
            currentBlock.add(new VariableRefAssignQuad<T>(address, currentBlock, index - 1, stackOffset - 1));
            fixType();
            currentBlock.add(new VariableRefAssignQuad<T>(address, currentBlock, index - 3, stackOffset + 2));
            fixType();
            //currentBlock.add(new VariableRefAssignQuad<T>(address, currentBlock, index - 4, stackOffset + 1));
            stackOffset += 4;
        } else {
            throw new IllegalArgumentException("byte code not yet supported");
        }
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
        variables[s1].setType(Operand.LONG);
        variables[s1 + 1].setType(Operand.LONG);
        variables[stackOffset + 1].setType(Operand.INT);
        currentBlock.add(new BinaryQuad<T>(address, currentBlock, s1, s1, LUSHR, stackOffset + 1));
        stackOffset += 1;
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
        stackOffset -= 2;
        variables[stackOffset].setType(Operand.FLOAT);
        currentBlock.add(new UnaryQuad<T>(address, currentBlock, stackOffset, D2F, stackOffset));
        stackOffset += 1;
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
        currentBlock.add(doBinaryQuad(LCMP, Operand.LONG));
    }

    public void visit_fcmpl() {
        currentBlock.add(doBinaryQuad(FCMPL, Operand.FLOAT));
    }

    public void visit_fcmpg() {
        currentBlock.add(doBinaryQuad(FCMPG, Operand.FLOAT));
    }

    public void visit_dcmpl() {
        currentBlock.add(doBinaryQuad(DCMPL, Operand.DOUBLE));
    }

    public void visit_dcmpg() {
        currentBlock.add(doBinaryQuad(DCMPG, Operand.DOUBLE));
    }

    public void visit_ifeq(int address) {
        visitBranchCondition(address, IFEQ, Operand.INT);
    }

    public void visit_ifne(int address) {
        visitBranchCondition(address, IFNE, Operand.INT);
    }

    public void visit_iflt(int address) {
        visitBranchCondition(address, IFLT, Operand.INT);
    }

    public void visit_ifge(int address) {
        visitBranchCondition(address, IFGE, Operand.INT);
    }

    public void visit_ifgt(int address) {
        visitBranchCondition(address, IFGT, Operand.INT);
    }

    public void visit_ifle(int address) {
        visitBranchCondition(address, IFLE, Operand.INT);
    }

    public void visit_if_icmpeq(int address) {
        visitBranchCondition(address, IF_ICMPEQ, Operand.INT);
    }

    public void visit_if_icmpne(int address) {
        visitBranchCondition(address, IF_ICMPNE, Operand.INT);
    }

    public void visit_if_icmplt(int address) {
        visitBranchCondition(address, IF_ICMPLT, Operand.INT);
    }

    public void visit_if_icmpge(int address) {
        visitBranchCondition(address, IF_ICMPGE, Operand.INT);
    }

    public void visit_if_icmpgt(int address) {
        visitBranchCondition(address, IF_ICMPGT, Operand.INT);
    }

    public void visit_if_icmple(int address) {
        visitBranchCondition(address, IF_ICMPLE, Operand.INT);
    }

    public void visit_if_acmpeq(int address) {
        visitBranchCondition(address, IF_ACMPEQ, Operand.REFERENCE);
    }

    public void visit_if_acmpne(int address) {
        visitBranchCondition(address, IF_ACMPNE, Operand.REFERENCE);
    }

    public void visit_goto(int address) {
        currentBlock.add(new UnconditionalBranchQuad<T>(this.address, currentBlock, address));
        setSuccessorStackOffset();
    }

    public void visit_tableswitch(int defValue, int lowValue, int highValue, int[] addresses) {
        stackOffset -= 1;
        currentBlock.add(new TableswitchQuad<T>(address, currentBlock, defValue, lowValue, highValue, addresses,
            stackOffset));
        setSuccessorStackOffset();
    }

    public void visit_lookupswitch(int defAddress, int[] matchValues, int[] addresses) {
        stackOffset -= 1;
        currentBlock.add(new LookupswitchQuad<T>(address, currentBlock, defAddress, matchValues, addresses,
            stackOffset));
        setSuccessorStackOffset();
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
        fieldRef.resolve(vmClassLoader);
        int jvmType = fieldRef.getResolvedVmField().getType().getJvmType();
        variables[stackOffset].setTypeFromJvmType(jvmType);
        currentBlock.add(new StaticRefAssignQuad<T>(address, currentBlock, stackOffset, fieldRef));
        stackOffset += getCategory(jvmType);
    }

    public void visit_putstatic(VmConstFieldRef fieldRef) {
        fieldRef.resolve(vmClassLoader);
        int jvmType = fieldRef.getResolvedVmField().getType().getJvmType();
        stackOffset -= getCategory(jvmType);
        currentBlock.add(new StaticRefStoreQuad<T>(address, currentBlock, stackOffset, fieldRef));
    }

    public void visit_getfield(VmConstFieldRef fieldRef) {
        fieldRef.resolve(vmClassLoader);
        stackOffset -= 1;
        int jvmType = fieldRef.getResolvedVmField().getType().getJvmType();
        variables[stackOffset].setTypeFromJvmType(jvmType);
        currentBlock.add(new RefAssignQuad<T>(address, currentBlock, stackOffset, fieldRef, stackOffset));
        stackOffset += getCategory(jvmType);
    }

    public void visit_putfield(VmConstFieldRef fieldRef) {
        fieldRef.resolve(vmClassLoader);
        int jvmType = fieldRef.getResolvedVmField().getType().getJvmType();
        stackOffset -= getCategory(jvmType);
        int ref = stackOffset - 1;
        currentBlock.add(new RefStoreQuad<T>(address, currentBlock, stackOffset, fieldRef, ref));
        stackOffset -= 1;
    }

    public void visit_invokevirtual(VmConstMethodRef methodRef) {
        methodRef.resolve(vmClassLoader);
        VmMethod vmMethod = methodRef.getResolvedVmMethod();
        int nrArguments = vmMethod.getNoArguments();
        int[] varOffs = new int[nrArguments + 1];
        for (int i = nrArguments; i-- > 0;) {
            VmType argType = vmMethod.getArgumentType(i);
            int stackChange;
            int jvmType;
            if (argType.isPrimitive()) {
                jvmType = argType.getJvmType();
                stackChange = getCategory(jvmType);
            } else {
                stackChange = 1;
                jvmType = JvmType.REFERENCE;
            }
            stackOffset -= stackChange;
            variables[stackOffset].setTypeFromJvmType(jvmType);
            varOffs[i + 1] = stackOffset;
        }
        stackOffset--;
        variables[stackOffset].setType(Operand.REFERENCE);
        varOffs[0] = stackOffset;

        int returnType = JvmType.getReturnType(methodRef.getSignature());
        if (JvmType.VOID == returnType) {
            currentBlock.add(new VirtualCallQuad(address, currentBlock, methodRef, varOffs));
        } else {
            currentBlock.add(new VirtualCallAssignQuad(address, currentBlock, stackOffset, methodRef, varOffs));
            stackOffset += typeSizeInfo.getStackSlots(returnType);
        }
//        int argSlotCount = Signature.getArgSlotCount(typeSizeInfo, methodRef.getSignature());
//        int returnType = JvmType.getReturnType(methodRef.getSignature());
//        if (JvmType.VOID == returnType) {
//            int[] varOffs = new int[argSlotCount + 1];
//            for (int i = 0; i < argSlotCount; i++) {
//                stackOffset--;
//                variables[stackOffset].setType(Operand.INT);
//                varOffs[i] = stackOffset;
//            }
//            stackOffset--;
//            variables[argSlotCount].setType(Operand.REFERENCE);
//            varOffs[argSlotCount] = stackOffset;
//            currentBlock.add(new VirtualCallQuad(address, currentBlock, methodRef, varOffs));
//        } else {
//            int[] varOffs = new int[argSlotCount + 1];
//            for (int i = 0; i < argSlotCount; i++) {
//                stackOffset--;
//                variables[stackOffset].setType(Operand.INT);
//                varOffs[i] = stackOffset;
//            }
//            stackOffset--;
//            variables[argSlotCount].setType(Operand.REFERENCE);
//            varOffs[argSlotCount] = stackOffset;
//            currentBlock.add(new VirtualCallAssignQuad(address, currentBlock, stackOffset, methodRef, varOffs));
//            stackOffset += typeSizeInfo.getStackSlots(returnType);
//        }
    }

    public void visit_invokespecial(VmConstMethodRef methodRef) {
        methodRef.resolve(vmClassLoader);
        VmMethod vmMethod = methodRef.getResolvedVmMethod();
        int nrArguments = vmMethod.getNoArguments();
        int[] varOffs = new int[nrArguments + 1];
        for (int i = nrArguments; i-- > 0;) {
            VmType argType = vmMethod.getArgumentType(i);
            int stackChange;
            int jvmType;
            if (argType.isPrimitive()) {
                jvmType = argType.getJvmType();
                stackChange = getCategory(jvmType);
            } else {
                stackChange = 1;
                jvmType = JvmType.REFERENCE;
            }
            stackOffset -= stackChange;
            variables[stackOffset].setTypeFromJvmType(jvmType);
            varOffs[i + 1] = stackOffset;
        }
        stackOffset--;
        variables[stackOffset].setType(Operand.REFERENCE);
        varOffs[0] = stackOffset;

        int returnType = JvmType.getReturnType(methodRef.getSignature());
        if (JvmType.VOID == returnType) {
            currentBlock.add(new SpecialCallQuad(address, currentBlock, methodRef, varOffs));
        } else {
            currentBlock.add(new SpecialCallAssignQuad(address, currentBlock, stackOffset, methodRef, varOffs));
            stackOffset += typeSizeInfo.getStackSlots(returnType);
        }
//        int argSlotCount = Signature.getArgSlotCount(typeSizeInfo, methodRef.getSignature());
//        int returnType = JvmType.getReturnType(methodRef.getSignature());
//        if (JvmType.VOID == returnType) {
//            int[] varOffs = new int[argSlotCount + 1];
//            for (int i = 0; i < argSlotCount; i++) {
//                stackOffset--;
//                variables[stackOffset].setType(Operand.INT);
//                varOffs[i] = stackOffset;
//            }
//            stackOffset--;
//            variables[argSlotCount].setType(Operand.REFERENCE);
//            varOffs[argSlotCount] = stackOffset;
//            currentBlock.add(new SpecialCallQuad(address, currentBlock, methodRef, varOffs));
//        } else {
//            int[] varOffs = new int[argSlotCount + 1];
//            for (int i = 0; i < argSlotCount; i++) {
//                stackOffset--;
//                variables[stackOffset].setType(Operand.INT);
//                varOffs[i] = stackOffset;
//            }
//            stackOffset--;
//            variables[argSlotCount].setType(Operand.REFERENCE);
//            varOffs[argSlotCount] = stackOffset;
//            currentBlock.add(new SpecialCallAssignQuad(address, currentBlock, stackOffset, methodRef, varOffs));
//            stackOffset++;
//        }
    }

    public void visit_invokestatic(VmConstMethodRef methodRef) {
        methodRef.resolve(vmClassLoader);
        VmMethod vmMethod = methodRef.getResolvedVmMethod();
        int nrArguments = vmMethod.getNoArguments();
        int[] varOffs = new int[nrArguments];
        for (int i = nrArguments; i-- > 0;) {
            VmType argType = vmMethod.getArgumentType(i);
            int stackChange;
            int jvmType;
            if (argType.isPrimitive()) {
                jvmType = argType.getJvmType();
                stackChange = getCategory(jvmType);
            } else {
                stackChange = 1;
                jvmType = JvmType.REFERENCE;
            }
            stackOffset -= stackChange;
            variables[stackOffset].setTypeFromJvmType(jvmType);
            varOffs[i] = stackOffset;
        }
        int returnType = JvmType.getReturnType(methodRef.getSignature());
        if (JvmType.VOID == returnType) {
            currentBlock.add(new StaticCallQuad(address, currentBlock, methodRef, varOffs));
        } else {
            currentBlock.add(new StaticCallAssignQuad(address, currentBlock, stackOffset, methodRef, varOffs));
            stackOffset += typeSizeInfo.getStackSlots(returnType);
        }
//        int argSlotCount = Signature.getArgSlotCount(typeSizeInfo, methodRef.getSignature());
//        int returnType = JvmType.getReturnType(methodRef.getSignature());
//        if (JvmType.VOID == returnType) {
//            int[] varOffs = new int[argSlotCount];
//            for (int i = 0; i < argSlotCount; i++) {
//                stackOffset--;
//                variables[stackOffset].setType(Operand.INT);
//                varOffs[i] = stackOffset;
//            }
//            currentBlock.add(new StaticCallQuad<T>(address, currentBlock, methodRef, varOffs));
//        } else {
//            int[] varOffs = new int[argSlotCount];
//            for (int i = 0; i < argSlotCount; i++) {
//                stackOffset--;
//                variables[stackOffset].setType(Operand.INT);
//                varOffs[i] = stackOffset;
//            }
//            currentBlock.add(new StaticCallAssignQuad<T>(address, currentBlock, stackOffset, methodRef, varOffs));
//            stackOffset++;
//        }
    }

    public void visit_invokeinterface(VmConstIMethodRef methodRef, int count) {
        methodRef.resolve(vmClassLoader);
        VmMethod vmMethod = methodRef.getResolvedVmMethod();
        int nrArguments = vmMethod.getNoArguments();
        int[] varOffs = new int[nrArguments + 1];
        for (int i = nrArguments; i-- > 0;) {
            VmType argType = vmMethod.getArgumentType(i);
            int stackChange;
            int jvmType;
            if (argType.isPrimitive()) {
                jvmType = argType.getJvmType();
                stackChange = getCategory(jvmType);
            } else {
                stackChange = 1;
                jvmType = JvmType.REFERENCE;
            }
            stackOffset -= stackChange;
            variables[stackOffset].setTypeFromJvmType(jvmType);
            varOffs[i + 1] = stackOffset;
        }
        stackOffset--;
        variables[stackOffset].setType(Operand.REFERENCE);
        varOffs[0] = stackOffset;

        int returnType = JvmType.getReturnType(methodRef.getSignature());
        if (JvmType.VOID == returnType) {
            currentBlock.add(new InterfaceCallQuad(address, currentBlock, methodRef, varOffs));
        } else {
            currentBlock.add(new InterfaceCallAssignQuad(address, currentBlock, stackOffset, methodRef, varOffs));
            stackOffset += typeSizeInfo.getStackSlots(returnType);
        }


//        int argSlotCount = Signature.getArgSlotCount(typeSizeInfo, methodRef.getSignature());
//        int returnType = JvmType.getReturnType(methodRef.getSignature());
//        if (JvmType.VOID == returnType) {
//            int[] varOffs = new int[argSlotCount + 1];
//            for (int i = 0; i < argSlotCount; i++) {
//                stackOffset--;
//                variables[stackOffset].setType(Operand.INT);
//                varOffs[i] = stackOffset;
//            }
//            stackOffset--;
//            variables[argSlotCount].setType(Operand.REFERENCE);
//            varOffs[argSlotCount] = stackOffset;
//            currentBlock.add(new InterfaceCallQuad(address, currentBlock, methodRef, varOffs));
//        } else {
//            int[] varOffs = new int[argSlotCount + 1];
//            for (int i = 0; i < argSlotCount; i++) {
//                stackOffset--;
//                variables[stackOffset].setType(Operand.INT);
//                varOffs[i] = stackOffset;
//            }
//            stackOffset--;
//            variables[argSlotCount].setType(Operand.REFERENCE);
//            varOffs[argSlotCount] = stackOffset;
//            currentBlock.add(new InterfaceCallAssignQuad(address, currentBlock, stackOffset, methodRef, varOffs));
//            stackOffset += typeSizeInfo.getStackSlots(returnType);
//        }
    }

    public void visit_new(VmConstClass clazz) {
        currentBlock.add(new NewAssignQuad<T>(address, currentBlock, stackOffset, clazz));
        stackOffset++;
    }

    public void visit_newarray(int type) {
        stackOffset -= 1;
        currentBlock.add(new NewPrimitiveArrayAssignQuad<T>(address, currentBlock, stackOffset, type, stackOffset));
        stackOffset += 1;
    }

    public void visit_anewarray(VmConstClass clazz) {
        stackOffset -= 1;
        currentBlock.add(new NewObjectArrayAssignQuad<T>(address, currentBlock, stackOffset, clazz, stackOffset));
        stackOffset += 1;
    }

    public void visit_arraylength() {
        stackOffset -= 1;
        currentBlock.add(new ArrayLengthAssignQuad(address, currentBlock, stackOffset, stackOffset));
        stackOffset += 1;
    }

    public void visit_athrow() {
        stackOffset -= 1;
        currentBlock.add(new ThrowQuad<T>(address, currentBlock, stackOffset));
        stackOffset = nLocals + 1;
    }

    public void visit_checkcast(VmConstClass clazz) {
        stackOffset -= 1;
        currentBlock.add(new CheckcastQuad<T>(address, currentBlock, clazz, stackOffset));
        stackOffset += 1;
    }

    public void visit_instanceof(VmConstClass clazz) {
        stackOffset -= 1;
        currentBlock.add(new InstanceofAssignQuad<T>(address, currentBlock, stackOffset, clazz, stackOffset));
        stackOffset += 1;
    }

    public void visit_monitorenter() {
        stackOffset -= 1;
        currentBlock.add(new MonitorenterQuad<T>(address, currentBlock, stackOffset));
    }

    public void visit_monitorexit() {
        stackOffset -= 1;
        currentBlock.add(new MonitorexitQuad<T>(address, currentBlock, stackOffset));
    }

    public void visit_multianewarray(VmConstClass clazz, int dimensions) {
        stackOffset -= 1;
        int[] sizes = new int[dimensions];
        for (int i = 0; i < dimensions; i++) {
            sizes[i] = stackOffset;
            stackOffset -= 1;
        }
        stackOffset += 1;
        currentBlock.add(new NewMultiArrayAssignQuad<T>(address, currentBlock, stackOffset, clazz, sizes));
        stackOffset += 1;
    }

    public void visit_ifnull(int address) {
        visitBranchCondition(address, IFNULL, Operand.REFERENCE);
    }

    public void visit_ifnonnull(int address) {
        visitBranchCondition(address, IFNONNULL, Operand.REFERENCE);
    }

    public void visit_jsr(int address) {
        throw new UnsupportedOperationException("Byte code not supported: jsr");
    }

    public void visit_ret(int index) {
        throw new UnsupportedOperationException("Byte code not supported: ret");
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
        if (op == BinaryOperation.LCMP || op == BinaryOperation.DCMPL || op == BinaryOperation.DCMPG) {
            stackOffset -= 1;
        }
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

    //*********************** HELPER METHODS *****************************//
    private void visitArrayLoad(int arrayType) {
        stackOffset -= 1;
        int ind = stackOffset;
        int ref = stackOffset - 1;
        variables[ind].setType(Operand.INT);
        variables[ref].setType(Operand.REFERENCE);
        currentBlock.add(new ArrayAssignQuad(address, currentBlock, ref, ind, ref, arrayType));
        if (arrayType ==  Operand.LONG || arrayType == Operand.DOUBLE) {
            stackOffset += 1;
        }
    }

    private void visitArrayStore(int arrayType) {
        stackOffset -= 1;
        int disp = arrayType == Operand.LONG || arrayType == Operand.DOUBLE ? 1 : 0;
        int val = stackOffset - disp;
        int ind = stackOffset - disp - 1;
        int ref = stackOffset - disp - 2;
        variables[ind].setType(Operand.INT);
        variables[val].setType(arrayType);
        variables[ref].setType(Operand.REFERENCE);
        currentBlock.add(new ArrayStoreQuad(address, currentBlock, val, ind, ref, arrayType));
        stackOffset -= disp + 2;
    }

    private void visitBranchCondition(int address, BranchCondition condition, int type) {
        if (condition.isUnary()) {
            int s1 = stackOffset - 1;
            Variable[] variables1 = currentBlock.getVariables();
            variables1[s1].setType(type);
            currentBlock.add(new ConditionalBranchQuad<T>(this.address, currentBlock, s1, condition, address));
            stackOffset -= 1;
            setSuccessorStackOffset();
        } else {
            int s1 = stackOffset - 2;
            int s2 = stackOffset - 1;
            Variable[] variables1 = currentBlock.getVariables();
            variables1[s1].setType(type);
            variables1[s2].setType(type);
            currentBlock.add(new ConditionalBranchQuad<T>(this.address, currentBlock, s1, condition, s2, address));
            stackOffset -= 2;
            setSuccessorStackOffset();
        }
    }

    //todo review useage; move this method to VmType ?
    private int getCategory(int jvmType) {
        return isCategory2(jvmType) ? 2 : 1;
    }

    private boolean isCategory2(int jvmType) {
        return jvmType == JvmType.LONG || jvmType == JvmType.DOUBLE;
    }
}
