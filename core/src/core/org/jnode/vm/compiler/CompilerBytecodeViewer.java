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
 
package org.jnode.vm.compiler;

import org.jnode.vm.bytecode.BasicBlock;
import org.jnode.vm.bytecode.BytecodeParser;
import org.jnode.vm.bytecode.BytecodeViewer;
import org.jnode.vm.classmgr.VmConstClass;
import org.jnode.vm.classmgr.VmConstFieldRef;
import org.jnode.vm.classmgr.VmConstIMethodRef;
import org.jnode.vm.classmgr.VmConstMethodRef;
import org.jnode.vm.classmgr.VmConstString;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class CompilerBytecodeViewer extends InlineBytecodeVisitor {

    private final BytecodeViewer viewer;

    public CompilerBytecodeViewer() {
        viewer = new BytecodeViewer();
    }

    /**
     * @see org.jnode.vm.compiler.InlineBytecodeVisitor#endInlinedMethod(org.jnode.vm.classmgr.VmMethod)
     */
    public void endInlinedMethod(VmMethod previousMethod) {
        viewer.out("-- end of inlined method");
        viewer.unindent();
    }

    /**
     * @see org.jnode.vm.compiler.InlineBytecodeVisitor#startInlinedMethodHeader(VmMethod, int)
     */
    public void startInlinedMethodHeader(VmMethod inlinedMethod, int newMaxLocals) {
        viewer.indent();
        viewer.out("-- start of inlined method header " + inlinedMethod.getName() + ", #locals " +
            inlinedMethod.getBytecode().getNoLocals() + ", #newlocals " + newMaxLocals);
    }

    /**
     * @see org.jnode.vm.compiler.InlineBytecodeVisitor#startInlinedMethodCode(VmMethod, int)
     */
    public void startInlinedMethodCode(VmMethod inlinedMethod, int newMaxLocals) {
        viewer.indent();
        viewer.out("-- start of inlined method code " + inlinedMethod.getName() + ", #locals " +
            inlinedMethod.getBytecode().getNoLocals() + ", #newlocals " + newMaxLocals);
    }

    /**
     * @see org.jnode.vm.compiler.CompilerBytecodeVisitor#endBasicBlock()
     */
    public void endBasicBlock() {
        viewer.out("-- end of basic block");
    }

    /**
     * @see org.jnode.vm.compiler.CompilerBytecodeVisitor#startBasicBlock(org.jnode.vm.bytecode.BasicBlock)
     */
    public void startBasicBlock(BasicBlock bb) {
        viewer.out("-- start of basic block");
    }

    /**
     * A try block is about to start
     */
    public void startTryBlock() {
        viewer.out("-- start of try block");
    }

    /**
     * A try block has finished
     */
    public void endTryBlock() {
        viewer.out("-- end of try block");
    }

    /**
     * @see org.jnode.vm.compiler.CompilerBytecodeVisitor#yieldPoint()
     */
    public void yieldPoint() {
        viewer.out("-- yieldpoint");
    }

    /**
     *
     */
    public void endInstruction() {
        viewer.endInstruction();
    }

    /**
     *
     */
    public void endMethod() {
        viewer.endMethod();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        return viewer.equals(obj);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return viewer.hashCode();
    }

    /**
     * @param parser
     */
    public void setParser(BytecodeParser parser) {
        viewer.setParser(parser);
    }

    /**
     * @param address
     */
    public void startInstruction(int address) {
        viewer.startInstruction(address);
    }

    /**
     * @param method
     */
    public void startMethod(VmMethod method) {
        viewer.startMethod(method);
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return viewer.toString();
    }

    /**
     *
     */
    public void visit_aaload() {
        viewer.visit_aaload();
    }

    /**
     *
     */
    public void visit_aastore() {
        viewer.visit_aastore();
    }

    /**
     *
     */
    public void visit_aconst_null() {
        viewer.visit_aconst_null();
    }

    /**
     * @param index
     */
    public void visit_aload(int index) {
        viewer.visit_aload(index);
    }

    /**
     * @param clazz
     */
    public void visit_anewarray(VmConstClass clazz) {
        viewer.visit_anewarray(clazz);
    }

    /**
     *
     */
    public void visit_areturn() {
        viewer.visit_areturn();
    }

    /**
     *
     */
    public void visit_arraylength() {
        viewer.visit_arraylength();
    }

    /**
     * @param index
     */
    public void visit_astore(int index) {
        viewer.visit_astore(index);
    }

    /**
     *
     */
    public void visit_athrow() {
        viewer.visit_athrow();
    }

    /**
     *
     */
    public void visit_baload() {
        viewer.visit_baload();
    }

    /**
     *
     */
    public void visit_bastore() {
        viewer.visit_bastore();
    }

    /**
     *
     */
    public void visit_caload() {
        viewer.visit_caload();
    }

    /**
     *
     */
    public void visit_castore() {
        viewer.visit_castore();
    }

    /**
     * @param clazz
     */
    public void visit_checkcast(VmConstClass clazz) {
        viewer.visit_checkcast(clazz);
    }

    /**
     *
     */
    public void visit_d2f() {
        viewer.visit_d2f();
    }

    /**
     *
     */
    public void visit_d2i() {
        viewer.visit_d2i();
    }

    /**
     *
     */
    public void visit_d2l() {
        viewer.visit_d2l();
    }

    /**
     *
     */
    public void visit_dadd() {
        viewer.visit_dadd();
    }

    /**
     *
     */
    public void visit_daload() {
        viewer.visit_daload();
    }

    /**
     *
     */
    public void visit_dastore() {
        viewer.visit_dastore();
    }

    /**
     *
     */
    public void visit_dcmpg() {
        viewer.visit_dcmpg();
    }

    /**
     *
     */
    public void visit_dcmpl() {
        viewer.visit_dcmpl();
    }

    /**
     * @param value
     */
    public void visit_dconst(double value) {
        viewer.visit_dconst(value);
    }

    /**
     *
     */
    public void visit_ddiv() {
        viewer.visit_ddiv();
    }

    /**
     * @param index
     */
    public void visit_dload(int index) {
        viewer.visit_dload(index);
    }

    /**
     *
     */
    public void visit_dmul() {
        viewer.visit_dmul();
    }

    /**
     *
     */
    public void visit_dneg() {
        viewer.visit_dneg();
    }

    /**
     *
     */
    public void visit_drem() {
        viewer.visit_drem();
    }

    /**
     *
     */
    public void visit_dreturn() {
        viewer.visit_dreturn();
    }

    /**
     * @param index
     */
    public void visit_dstore(int index) {
        viewer.visit_dstore(index);
    }

    /**
     *
     */
    public void visit_dsub() {
        viewer.visit_dsub();
    }

    /**
     *
     */
    public void visit_dup() {
        viewer.visit_dup();
    }

    /**
     *
     */
    public void visit_dup_x1() {
        viewer.visit_dup_x1();
    }

    /**
     *
     */
    public void visit_dup_x2() {
        viewer.visit_dup_x2();
    }

    /**
     *
     */
    public void visit_dup2() {
        viewer.visit_dup2();
    }

    /**
     *
     */
    public void visit_dup2_x1() {
        viewer.visit_dup2_x1();
    }

    /**
     *
     */
    public void visit_dup2_x2() {
        viewer.visit_dup2_x2();
    }

    /**
     *
     */
    public void visit_f2d() {
        viewer.visit_f2d();
    }

    /**
     *
     */
    public void visit_f2i() {
        viewer.visit_f2i();
    }

    /**
     *
     */
    public void visit_f2l() {
        viewer.visit_f2l();
    }

    /**
     *
     */
    public void visit_fadd() {
        viewer.visit_fadd();
    }

    /**
     *
     */
    public void visit_faload() {
        viewer.visit_faload();
    }

    /**
     *
     */
    public void visit_fastore() {
        viewer.visit_fastore();
    }

    /**
     *
     */
    public void visit_fcmpg() {
        viewer.visit_fcmpg();
    }

    /**
     *
     */
    public void visit_fcmpl() {
        viewer.visit_fcmpl();
    }

    /**
     * @param value
     */
    public void visit_fconst(float value) {
        viewer.visit_fconst(value);
    }

    /**
     *
     */
    public void visit_fdiv() {
        viewer.visit_fdiv();
    }

    /**
     * @param index
     */
    public void visit_fload(int index) {
        viewer.visit_fload(index);
    }

    /**
     *
     */
    public void visit_fmul() {
        viewer.visit_fmul();
    }

    /**
     *
     */
    public void visit_fneg() {
        viewer.visit_fneg();
    }

    /**
     *
     */
    public void visit_frem() {
        viewer.visit_frem();
    }

    /**
     *
     */
    public void visit_freturn() {
        viewer.visit_freturn();
    }

    /**
     * @param index
     */
    public void visit_fstore(int index) {
        viewer.visit_fstore(index);
    }

    /**
     *
     */
    public void visit_fsub() {
        viewer.visit_fsub();
    }

    /**
     * @param fieldRef
     */
    public void visit_getfield(VmConstFieldRef fieldRef) {
        viewer.visit_getfield(fieldRef);
    }

    /**
     * @param fieldRef
     */
    public void visit_getstatic(VmConstFieldRef fieldRef) {
        viewer.visit_getstatic(fieldRef);
    }

    /**
     * @param address
     */
    public void visit_goto(int address) {
        viewer.visit_goto(address);
    }

    /**
     *
     */
    public void visit_i2b() {
        viewer.visit_i2b();
    }

    /**
     *
     */
    public void visit_i2c() {
        viewer.visit_i2c();
    }

    /**
     *
     */
    public void visit_i2d() {
        viewer.visit_i2d();
    }

    /**
     *
     */
    public void visit_i2f() {
        viewer.visit_i2f();
    }

    /**
     *
     */
    public void visit_i2l() {
        viewer.visit_i2l();
    }

    /**
     *
     */
    public void visit_i2s() {
        viewer.visit_i2s();
    }

    /**
     *
     */
    public void visit_iadd() {
        viewer.visit_iadd();
    }

    /**
     *
     */
    public void visit_iaload() {
        viewer.visit_iaload();
    }

    /**
     *
     */
    public void visit_iand() {
        viewer.visit_iand();
    }

    /**
     *
     */
    public void visit_iastore() {
        viewer.visit_iastore();
    }

    /**
     * @param value
     */
    public void visit_iconst(int value) {
        viewer.visit_iconst(value);
    }

    /**
     *
     */
    public void visit_idiv() {
        viewer.visit_idiv();
    }

    /**
     * @param address
     */
    public void visit_if_acmpeq(int address) {
        viewer.visit_if_acmpeq(address);
    }

    /**
     * @param address
     */
    public void visit_if_acmpne(int address) {
        viewer.visit_if_acmpne(address);
    }

    /**
     * @param address
     */
    public void visit_if_icmpeq(int address) {
        viewer.visit_if_icmpeq(address);
    }

    /**
     * @param address
     */
    public void visit_if_icmpge(int address) {
        viewer.visit_if_icmpge(address);
    }

    /**
     * @param address
     */
    public void visit_if_icmpgt(int address) {
        viewer.visit_if_icmpgt(address);
    }

    /**
     * @param address
     */
    public void visit_if_icmple(int address) {
        viewer.visit_if_icmple(address);
    }

    /**
     * @param address
     */
    public void visit_if_icmplt(int address) {
        viewer.visit_if_icmplt(address);
    }

    /**
     * @param address
     */
    public void visit_if_icmpne(int address) {
        viewer.visit_if_icmpne(address);
    }

    /**
     * @param address
     */
    public void visit_ifeq(int address) {
        viewer.visit_ifeq(address);
    }

    /**
     * @param address
     */
    public void visit_ifge(int address) {
        viewer.visit_ifge(address);
    }

    /**
     * @param address
     */
    public void visit_ifgt(int address) {
        viewer.visit_ifgt(address);
    }

    /**
     * @param address
     */
    public void visit_ifle(int address) {
        viewer.visit_ifle(address);
    }

    /**
     * @param address
     */
    public void visit_iflt(int address) {
        viewer.visit_iflt(address);
    }

    /**
     * @param address
     */
    public void visit_ifne(int address) {
        viewer.visit_ifne(address);
    }

    /**
     * @param address
     */
    public void visit_ifnonnull(int address) {
        viewer.visit_ifnonnull(address);
    }

    /**
     * @param address
     */
    public void visit_ifnull(int address) {
        viewer.visit_ifnull(address);
    }

    /**
     * @param index
     * @param incValue
     */
    public void visit_iinc(int index, int incValue) {
        viewer.visit_iinc(index, incValue);
    }

    /**
     * @param index
     */
    public void visit_iload(int index) {
        viewer.visit_iload(index);
    }

    /**
     *
     */
    public void visit_imul() {
        viewer.visit_imul();
    }

    /**
     *
     */
    public void visit_ineg() {
        viewer.visit_ineg();
    }

    /**
     * @param clazz
     */
    public void visit_instanceof(VmConstClass clazz) {
        viewer.visit_instanceof(clazz);
    }

    /**
     * @param methodRef
     * @param count
     */
    public void visit_invokeinterface(VmConstIMethodRef methodRef, int count) {
        viewer.visit_invokeinterface(methodRef, count);
    }

    /**
     * @param methodRef
     */
    public void visit_invokespecial(VmConstMethodRef methodRef) {
        viewer.visit_invokespecial(methodRef);
    }

    /**
     * @param methodRef
     */
    public void visit_invokestatic(VmConstMethodRef methodRef) {
        viewer.visit_invokestatic(methodRef);
    }

    /**
     * @param methodRef
     */
    public void visit_invokevirtual(VmConstMethodRef methodRef) {
        viewer.visit_invokevirtual(methodRef);
    }

    /**
     *
     */
    public void visit_ior() {
        viewer.visit_ior();
    }

    /**
     *
     */
    public void visit_irem() {
        viewer.visit_irem();
    }

    /**
     *
     */
    public void visit_ireturn() {
        viewer.visit_ireturn();
    }

    /**
     *
     */
    public void visit_ishl() {
        viewer.visit_ishl();
    }

    /**
     *
     */
    public void visit_ishr() {
        viewer.visit_ishr();
    }

    /**
     * @param index
     */
    public void visit_istore(int index) {
        viewer.visit_istore(index);
    }

    /**
     *
     */
    public void visit_isub() {
        viewer.visit_isub();
    }

    /**
     *
     */
    public void visit_iushr() {
        viewer.visit_iushr();
    }

    /**
     *
     */
    public void visit_ixor() {
        viewer.visit_ixor();
    }

    /**
     * @param address
     */
    public void visit_jsr(int address) {
        viewer.visit_jsr(address);
    }

    /**
     *
     */
    public void visit_l2d() {
        viewer.visit_l2d();
    }

    /**
     *
     */
    public void visit_l2f() {
        viewer.visit_l2f();
    }

    /**
     *
     */
    public void visit_l2i() {
        viewer.visit_l2i();
    }

    /**
     *
     */
    public void visit_ladd() {
        viewer.visit_ladd();
    }

    /**
     *
     */
    public void visit_laload() {
        viewer.visit_laload();
    }

    /**
     *
     */
    public void visit_land() {
        viewer.visit_land();
    }

    /**
     *
     */
    public void visit_lastore() {
        viewer.visit_lastore();
    }

    /**
     *
     */
    public void visit_lcmp() {
        viewer.visit_lcmp();
    }

    /**
     * @param value
     */
    public void visit_lconst(long value) {
        viewer.visit_lconst(value);
    }

    /**
     * @param value
     */
    public void visit_ldc(VmConstString value) {
        viewer.visit_ldc(value);
    }

    /**
     * @param value
     */
    public void visit_ldc(VmConstClass value) {
        viewer.visit_ldc(value);
    }

    /**
     * Push the given VmType on the stack.
     */
    public void visit_ldc(VmType<?> value) {
        viewer.visit_ldc(value);
    }

    /**
     *
     */
    public void visit_ldiv() {
        viewer.visit_ldiv();
    }

    /**
     * @param index
     */
    public void visit_lload(int index) {
        viewer.visit_lload(index);
    }

    /**
     *
     */
    public void visit_lmul() {
        viewer.visit_lmul();
    }

    /**
     *
     */
    public void visit_lneg() {
        viewer.visit_lneg();
    }

    /**
     * @param defValue
     * @param matchValues
     * @param addresses
     */
    public void visit_lookupswitch(int defValue, int[] matchValues,
                                   int[] addresses) {
        viewer.visit_lookupswitch(defValue, matchValues, addresses);
    }

    /**
     *
     */
    public void visit_lor() {
        viewer.visit_lor();
    }

    /**
     *
     */
    public void visit_lrem() {
        viewer.visit_lrem();
    }

    /**
     *
     */
    public void visit_lreturn() {
        viewer.visit_lreturn();
    }

    /**
     *
     */
    public void visit_lshl() {
        viewer.visit_lshl();
    }

    /**
     *
     */
    public void visit_lshr() {
        viewer.visit_lshr();
    }

    /**
     * @param index
     */
    public void visit_lstore(int index) {
        viewer.visit_lstore(index);
    }

    /**
     *
     */
    public void visit_lsub() {
        viewer.visit_lsub();
    }

    /**
     *
     */
    public void visit_lushr() {
        viewer.visit_lushr();
    }

    /**
     *
     */
    public void visit_lxor() {
        viewer.visit_lxor();
    }

    /**
     *
     */
    public void visit_monitorenter() {
        viewer.visit_monitorenter();
    }

    /**
     *
     */
    public void visit_monitorexit() {
        viewer.visit_monitorexit();
    }

    /**
     * @param clazz
     * @param dimensions
     */
    public void visit_multianewarray(VmConstClass clazz, int dimensions) {
        viewer.visit_multianewarray(clazz, dimensions);
    }

    /**
     * @param clazz
     */
    public void visit_new(VmConstClass clazz) {
        viewer.visit_new(clazz);
    }

    /**
     * @param type
     */
    public void visit_newarray(int type) {
        viewer.visit_newarray(type);
    }

    /**
     *
     */
    public void visit_nop() {
        viewer.visit_nop();
    }

    /**
     *
     */
    public void visit_pop() {
        viewer.visit_pop();
    }

    /**
     *
     */
    public void visit_pop2() {
        viewer.visit_pop2();
    }

    /**
     * @param fieldRef
     */
    public void visit_putfield(VmConstFieldRef fieldRef) {
        viewer.visit_putfield(fieldRef);
    }

    /**
     * @param fieldRef
     */
    public void visit_putstatic(VmConstFieldRef fieldRef) {
        viewer.visit_putstatic(fieldRef);
    }

    /**
     * @param index
     */
    public void visit_ret(int index) {
        viewer.visit_ret(index);
    }

    /**
     *
     */
    public void visit_return() {
        viewer.visit_return();
    }

    /**
     *
     */
    public void visit_saload() {
        viewer.visit_saload();
    }

    /**
     *
     */
    public void visit_sastore() {
        viewer.visit_sastore();
    }

    /**
     *
     */
    public void visit_swap() {
        viewer.visit_swap();
    }

    /**
     * @param defValue
     * @param lowValue
     * @param highValue
     * @param addresses
     */
    public void visit_tableswitch(int defValue, int lowValue, int highValue,
                                  int[] addresses) {
        viewer.visit_tableswitch(defValue, lowValue, highValue, addresses);
    }

    /**
     * @see org.jnode.vm.compiler.InlineBytecodeVisitor#visit_inlinedReturn()
     */
    public void visit_inlinedReturn(int jvmType) {
        viewer.out("inlinedReturn [type " + jvmType + "]");
    }
}
