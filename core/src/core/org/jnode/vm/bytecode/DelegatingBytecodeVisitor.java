/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.vm.bytecode;

import org.jnode.vm.classmgr.VmConstClass;
import org.jnode.vm.classmgr.VmConstFieldRef;
import org.jnode.vm.classmgr.VmConstIMethodRef;
import org.jnode.vm.classmgr.VmConstMethodRef;
import org.jnode.vm.classmgr.VmConstString;
import org.jnode.vm.classmgr.VmMethod;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class DelegatingBytecodeVisitor extends BytecodeVisitor {

    private final BytecodeVisitor delegate;

    public DelegatingBytecodeVisitor(BytecodeVisitor delegate) {
        this.delegate = delegate;
    }

    public final BytecodeVisitor getDelegate() {
        return delegate;
    }

    /**
     *
     */
    public void endInstruction() {
        delegate.endInstruction();
    }

    /**
     *
     */
    public void endMethod() {
        delegate.endMethod();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return delegate.hashCode();
    }

    /**
     * @param parser
     */
    public void setParser(BytecodeParser parser) {
        delegate.setParser(parser);
    }

    /**
     * @param address
     */
    public void startInstruction(int address) {
        delegate.startInstruction(address);
    }

    /**
     * @param method
     */
    public void startMethod(VmMethod method) {
        delegate.startMethod(method);
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return delegate.toString();
    }

    /**
     *
     */
    public void visit_aaload() {
        delegate.visit_aaload();
    }

    /**
     *
     */
    public void visit_aastore() {
        delegate.visit_aastore();
    }

    /**
     *
     */
    public void visit_aconst_null() {
        delegate.visit_aconst_null();
    }

    /**
     * @param index
     */
    public void visit_aload(int index) {
        delegate.visit_aload(index);
    }

    /**
     * @param clazz
     */
    public void visit_anewarray(VmConstClass clazz) {
        delegate.visit_anewarray(clazz);
    }

    /**
     *
     */
    public void visit_areturn() {
        delegate.visit_areturn();
    }

    /**
     *
     */
    public void visit_arraylength() {
        delegate.visit_arraylength();
    }

    /**
     * @param index
     */
    public void visit_astore(int index) {
        delegate.visit_astore(index);
    }

    /**
     *
     */
    public void visit_athrow() {
        delegate.visit_athrow();
    }

    /**
     *
     */
    public void visit_baload() {
        delegate.visit_baload();
    }

    /**
     *
     */
    public void visit_bastore() {
        delegate.visit_bastore();
    }

    /**
     *
     */
    public void visit_caload() {
        delegate.visit_caload();
    }

    /**
     *
     */
    public void visit_castore() {
        delegate.visit_castore();
    }

    /**
     * @param clazz
     */
    public void visit_checkcast(VmConstClass clazz) {
        delegate.visit_checkcast(clazz);
    }

    /**
     *
     */
    public void visit_d2f() {
        delegate.visit_d2f();
    }

    /**
     *
     */
    public void visit_d2i() {
        delegate.visit_d2i();
    }

    /**
     *
     */
    public void visit_d2l() {
        delegate.visit_d2l();
    }

    /**
     *
     */
    public void visit_dadd() {
        delegate.visit_dadd();
    }

    /**
     *
     */
    public void visit_daload() {
        delegate.visit_daload();
    }

    /**
     *
     */
    public void visit_dastore() {
        delegate.visit_dastore();
    }

    /**
     *
     */
    public void visit_dcmpg() {
        delegate.visit_dcmpg();
    }

    /**
     *
     */
    public void visit_dcmpl() {
        delegate.visit_dcmpl();
    }

    /**
     * @param value
     */
    public void visit_dconst(double value) {
        delegate.visit_dconst(value);
    }

    /**
     *
     */
    public void visit_ddiv() {
        delegate.visit_ddiv();
    }

    /**
     * @param index
     */
    public void visit_dload(int index) {
        delegate.visit_dload(index);
    }

    /**
     *
     */
    public void visit_dmul() {
        delegate.visit_dmul();
    }

    /**
     *
     */
    public void visit_dneg() {
        delegate.visit_dneg();
    }

    /**
     *
     */
    public void visit_drem() {
        delegate.visit_drem();
    }

    /**
     *
     */
    public void visit_dreturn() {
        delegate.visit_dreturn();
    }

    /**
     * @param index
     */
    public void visit_dstore(int index) {
        delegate.visit_dstore(index);
    }

    /**
     *
     */
    public void visit_dsub() {
        delegate.visit_dsub();
    }

    /**
     *
     */
    public void visit_dup() {
        delegate.visit_dup();
    }

    /**
     *
     */
    public void visit_dup_x1() {
        delegate.visit_dup_x1();
    }

    /**
     *
     */
    public void visit_dup_x2() {
        delegate.visit_dup_x2();
    }

    /**
     *
     */
    public void visit_dup2() {
        delegate.visit_dup2();
    }

    /**
     *
     */
    public void visit_dup2_x1() {
        delegate.visit_dup2_x1();
    }

    /**
     *
     */
    public void visit_dup2_x2() {
        delegate.visit_dup2_x2();
    }

    /**
     *
     */
    public void visit_f2d() {
        delegate.visit_f2d();
    }

    /**
     *
     */
    public void visit_f2i() {
        delegate.visit_f2i();
    }

    /**
     *
     */
    public void visit_f2l() {
        delegate.visit_f2l();
    }

    /**
     *
     */
    public void visit_fadd() {
        delegate.visit_fadd();
    }

    /**
     *
     */
    public void visit_faload() {
        delegate.visit_faload();
    }

    /**
     *
     */
    public void visit_fastore() {
        delegate.visit_fastore();
    }

    /**
     *
     */
    public void visit_fcmpg() {
        delegate.visit_fcmpg();
    }

    /**
     *
     */
    public void visit_fcmpl() {
        delegate.visit_fcmpl();
    }

    /**
     * @param value
     */
    public void visit_fconst(float value) {
        delegate.visit_fconst(value);
    }

    /**
     *
     */
    public void visit_fdiv() {
        delegate.visit_fdiv();
    }

    /**
     * @param index
     */
    public void visit_fload(int index) {
        delegate.visit_fload(index);
    }

    /**
     *
     */
    public void visit_fmul() {
        delegate.visit_fmul();
    }

    /**
     *
     */
    public void visit_fneg() {
        delegate.visit_fneg();
    }

    /**
     *
     */
    public void visit_frem() {
        delegate.visit_frem();
    }

    /**
     *
     */
    public void visit_freturn() {
        delegate.visit_freturn();
    }

    /**
     * @param index
     */
    public void visit_fstore(int index) {
        delegate.visit_fstore(index);
    }

    /**
     *
     */
    public void visit_fsub() {
        delegate.visit_fsub();
    }

    /**
     * @param fieldRef
     */
    public void visit_getfield(VmConstFieldRef fieldRef) {
        delegate.visit_getfield(fieldRef);
    }

    /**
     * @param fieldRef
     */
    public void visit_getstatic(VmConstFieldRef fieldRef) {
        delegate.visit_getstatic(fieldRef);
    }

    /**
     * @param address
     */
    public void visit_goto(int address) {
        delegate.visit_goto(address);
    }

    /**
     *
     */
    public void visit_i2b() {
        delegate.visit_i2b();
    }

    /**
     *
     */
    public void visit_i2c() {
        delegate.visit_i2c();
    }

    /**
     *
     */
    public void visit_i2d() {
        delegate.visit_i2d();
    }

    /**
     *
     */
    public void visit_i2f() {
        delegate.visit_i2f();
    }

    /**
     *
     */
    public void visit_i2l() {
        delegate.visit_i2l();
    }

    /**
     *
     */
    public void visit_i2s() {
        delegate.visit_i2s();
    }

    /**
     *
     */
    public void visit_iadd() {
        delegate.visit_iadd();
    }

    /**
     *
     */
    public void visit_iaload() {
        delegate.visit_iaload();
    }

    /**
     *
     */
    public void visit_iand() {
        delegate.visit_iand();
    }

    /**
     *
     */
    public void visit_iastore() {
        delegate.visit_iastore();
    }

    /**
     * @param value
     */
    public void visit_iconst(int value) {
        delegate.visit_iconst(value);
    }

    /**
     *
     */
    public void visit_idiv() {
        delegate.visit_idiv();
    }

    /**
     * @param address
     */
    public void visit_if_acmpeq(int address) {
        delegate.visit_if_acmpeq(address);
    }

    /**
     * @param address
     */
    public void visit_if_acmpne(int address) {
        delegate.visit_if_acmpne(address);
    }

    /**
     * @param address
     */
    public void visit_if_icmpeq(int address) {
        delegate.visit_if_icmpeq(address);
    }

    /**
     * @param address
     */
    public void visit_if_icmpge(int address) {
        delegate.visit_if_icmpge(address);
    }

    /**
     * @param address
     */
    public void visit_if_icmpgt(int address) {
        delegate.visit_if_icmpgt(address);
    }

    /**
     * @param address
     */
    public void visit_if_icmple(int address) {
        delegate.visit_if_icmple(address);
    }

    /**
     * @param address
     */
    public void visit_if_icmplt(int address) {
        delegate.visit_if_icmplt(address);
    }

    /**
     * @param address
     */
    public void visit_if_icmpne(int address) {
        delegate.visit_if_icmpne(address);
    }

    /**
     * @param address
     */
    public void visit_ifeq(int address) {
        delegate.visit_ifeq(address);
    }

    /**
     * @param address
     */
    public void visit_ifge(int address) {
        delegate.visit_ifge(address);
    }

    /**
     * @param address
     */
    public void visit_ifgt(int address) {
        delegate.visit_ifgt(address);
    }

    /**
     * @param address
     */
    public void visit_ifle(int address) {
        delegate.visit_ifle(address);
    }

    /**
     * @param address
     */
    public void visit_iflt(int address) {
        delegate.visit_iflt(address);
    }

    /**
     * @param address
     */
    public void visit_ifne(int address) {
        delegate.visit_ifne(address);
    }

    /**
     * @param address
     */
    public void visit_ifnonnull(int address) {
        delegate.visit_ifnonnull(address);
    }

    /**
     * @param address
     */
    public void visit_ifnull(int address) {
        delegate.visit_ifnull(address);
    }

    /**
     * @param index
     * @param incValue
     */
    public void visit_iinc(int index, int incValue) {
        delegate.visit_iinc(index, incValue);
    }

    /**
     * @param index
     */
    public void visit_iload(int index) {
        delegate.visit_iload(index);
    }

    /**
     *
     */
    public void visit_imul() {
        delegate.visit_imul();
    }

    /**
     *
     */
    public void visit_ineg() {
        delegate.visit_ineg();
    }

    /**
     * @param clazz
     */
    public void visit_instanceof(VmConstClass clazz) {
        delegate.visit_instanceof(clazz);
    }

    /**
     * @param methodRef
     * @param count
     */
    public void visit_invokeinterface(VmConstIMethodRef methodRef, int count) {
        delegate.visit_invokeinterface(methodRef, count);
    }

    /**
     * @param methodRef
     */
    public void visit_invokespecial(VmConstMethodRef methodRef) {
        delegate.visit_invokespecial(methodRef);
    }

    /**
     * @param methodRef
     */
    public void visit_invokestatic(VmConstMethodRef methodRef) {
        delegate.visit_invokestatic(methodRef);
    }

    /**
     * @param methodRef
     */
    public void visit_invokevirtual(VmConstMethodRef methodRef) {
        delegate.visit_invokevirtual(methodRef);
    }

    /**
     *
     */
    public void visit_ior() {
        delegate.visit_ior();
    }

    /**
     *
     */
    public void visit_irem() {
        delegate.visit_irem();
    }

    /**
     *
     */
    public void visit_ireturn() {
        delegate.visit_ireturn();
    }

    /**
     *
     */
    public void visit_ishl() {
        delegate.visit_ishl();
    }

    /**
     *
     */
    public void visit_ishr() {
        delegate.visit_ishr();
    }

    /**
     * @param index
     */
    public void visit_istore(int index) {
        delegate.visit_istore(index);
    }

    /**
     *
     */
    public void visit_isub() {
        delegate.visit_isub();
    }

    /**
     *
     */
    public void visit_iushr() {
        delegate.visit_iushr();
    }

    /**
     *
     */
    public void visit_ixor() {
        delegate.visit_ixor();
    }

    /**
     * @param address
     */
    public void visit_jsr(int address) {
        delegate.visit_jsr(address);
    }

    /**
     *
     */
    public void visit_l2d() {
        delegate.visit_l2d();
    }

    /**
     *
     */
    public void visit_l2f() {
        delegate.visit_l2f();
    }

    /**
     *
     */
    public void visit_l2i() {
        delegate.visit_l2i();
    }

    /**
     *
     */
    public void visit_ladd() {
        delegate.visit_ladd();
    }

    /**
     *
     */
    public void visit_laload() {
        delegate.visit_laload();
    }

    /**
     *
     */
    public void visit_land() {
        delegate.visit_land();
    }

    /**
     *
     */
    public void visit_lastore() {
        delegate.visit_lastore();
    }

    /**
     *
     */
    public void visit_lcmp() {
        delegate.visit_lcmp();
    }

    /**
     * @param value
     */
    public void visit_lconst(long value) {
        delegate.visit_lconst(value);
    }

    /**
     * @param value
     */
    public void visit_ldc(VmConstString value) {
        delegate.visit_ldc(value);
    }

    /**
     * @param value
     */
    public void visit_ldc(VmConstClass value) {
        delegate.visit_ldc(value);
    }

    /**
     *
     */
    public void visit_ldiv() {
        delegate.visit_ldiv();
    }

    /**
     * @param index
     */
    public void visit_lload(int index) {
        delegate.visit_lload(index);
    }

    /**
     *
     */
    public void visit_lmul() {
        delegate.visit_lmul();
    }

    /**
     *
     */
    public void visit_lneg() {
        delegate.visit_lneg();
    }

    /**
     * @param defValue
     * @param matchValues
     * @param addresses
     */
    public void visit_lookupswitch(int defValue, int[] matchValues,
                                   int[] addresses) {
        delegate.visit_lookupswitch(defValue, matchValues, addresses);
    }

    /**
     *
     */
    public void visit_lor() {
        delegate.visit_lor();
    }

    /**
     *
     */
    public void visit_lrem() {
        delegate.visit_lrem();
    }

    /**
     *
     */
    public void visit_lreturn() {
        delegate.visit_lreturn();
    }

    /**
     *
     */
    public void visit_lshl() {
        delegate.visit_lshl();
    }

    /**
     *
     */
    public void visit_lshr() {
        delegate.visit_lshr();
    }

    /**
     * @param index
     */
    public void visit_lstore(int index) {
        delegate.visit_lstore(index);
    }

    /**
     *
     */
    public void visit_lsub() {
        delegate.visit_lsub();
    }

    /**
     *
     */
    public void visit_lushr() {
        delegate.visit_lushr();
    }

    /**
     *
     */
    public void visit_lxor() {
        delegate.visit_lxor();
    }

    /**
     *
     */
    public void visit_monitorenter() {
        delegate.visit_monitorenter();
    }

    /**
     *
     */
    public void visit_monitorexit() {
        delegate.visit_monitorexit();
    }

    /**
     * @param clazz
     * @param dimensions
     */
    public void visit_multianewarray(VmConstClass clazz, int dimensions) {
        delegate.visit_multianewarray(clazz, dimensions);
    }

    /**
     * @param clazz
     */
    public void visit_new(VmConstClass clazz) {
        delegate.visit_new(clazz);
    }

    /**
     * @param type
     */
    public void visit_newarray(int type) {
        delegate.visit_newarray(type);
    }

    /**
     *
     */
    public void visit_nop() {
        delegate.visit_nop();
    }

    /**
     *
     */
    public void visit_pop() {
        delegate.visit_pop();
    }

    /**
     *
     */
    public void visit_pop2() {
        delegate.visit_pop2();
    }

    /**
     * @param fieldRef
     */
    public void visit_putfield(VmConstFieldRef fieldRef) {
        delegate.visit_putfield(fieldRef);
    }

    /**
     * @param fieldRef
     */
    public void visit_putstatic(VmConstFieldRef fieldRef) {
        delegate.visit_putstatic(fieldRef);
    }

    /**
     * @param index
     */
    public void visit_ret(int index) {
        delegate.visit_ret(index);
    }

    /**
     *
     */
    public void visit_return() {
        delegate.visit_return();
    }

    /**
     *
     */
    public void visit_saload() {
        delegate.visit_saload();
    }

    /**
     *
     */
    public void visit_sastore() {
        delegate.visit_sastore();
    }

    /**
     *
     */
    public void visit_swap() {
        delegate.visit_swap();
    }

    /**
     * @param defValue
     * @param lowValue
     * @param highValue
     * @param addresses
     */
    public void visit_tableswitch(int defValue, int lowValue, int highValue,
                                  int[] addresses) {
        delegate.visit_tableswitch(defValue, lowValue, highValue, addresses);
    }
}
