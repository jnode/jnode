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
 
package org.jnode.vm.bytecode;

import org.jnode.vm.classmgr.VmConstClass;
import org.jnode.vm.classmgr.VmConstFieldRef;
import org.jnode.vm.classmgr.VmConstIMethodRef;
import org.jnode.vm.classmgr.VmConstMethodRef;
import org.jnode.vm.classmgr.VmConstString;
import org.jnode.vm.classmgr.VmMethod;

/**
 * @author epr
 */
public abstract class BytecodeVisitorSupport extends BytecodeVisitor {

    private int instructionAddress = -1;
    private VmMethod method = null;
    private BytecodeParser parser;

    public void setParser(BytecodeParser parser) {
        this.parser = parser;
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#endInstruction()
     */
    public void endInstruction() {
        this.instructionAddress = -1;
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#endMethod()
     */
    public void endMethod() {
        this.method = null;
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#startInstruction(int)
     */
    public void startInstruction(int address) {
        this.instructionAddress = address;
    }

    /**
     * @param method
     * @see org.jnode.vm.bytecode.BytecodeVisitor#startMethod(org.jnode.vm.classmgr.VmMethod)
     */
    public void startMethod(VmMethod method) {
        this.method = method;
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_aaload()
     */
    public void visit_aaload() {
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_aastore()
     */
    public void visit_aastore() {
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_aconst_null()
     */
    public void visit_aconst_null() {

    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_aload(int)
     */
    public void visit_aload(int index) {

    }

    /**
     * @param clazz
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_anewarray(org.jnode.vm.classmgr.VmConstClass)
     */
    public void visit_anewarray(VmConstClass clazz) {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_areturn()
     */
    public void visit_areturn() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_arraylength()
     */
    public void visit_arraylength() {

    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_astore(int)
     */
    public void visit_astore(int index) {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_athrow()
     */
    public void visit_athrow() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_baload()
     */
    public void visit_baload() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_bastore()
     */
    public void visit_bastore() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_caload()
     */
    public void visit_caload() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_castore()
     */
    public void visit_castore() {

    }

    /**
     * @param clazz
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_checkcast(org.jnode.vm.classmgr.VmConstClass)
     */
    public void visit_checkcast(VmConstClass clazz) {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_d2f()
     */
    public void visit_d2f() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_d2i()
     */
    public void visit_d2i() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_d2l()
     */
    public void visit_d2l() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dadd()
     */
    public void visit_dadd() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_daload()
     */
    public void visit_daload() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dastore()
     */
    public void visit_dastore() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dcmpg()
     */
    public void visit_dcmpg() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dcmpl()
     */
    public void visit_dcmpl() {

    }

    /**
     * @param value
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dconst(double)
     */
    public void visit_dconst(double value) {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ddiv()
     */
    public void visit_ddiv() {

    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dload(int)
     */
    public void visit_dload(int index) {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dmul()
     */
    public void visit_dmul() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dneg()
     */
    public void visit_dneg() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_drem()
     */
    public void visit_drem() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dreturn()
     */
    public void visit_dreturn() {

    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dstore(int)
     */
    public void visit_dstore(int index) {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dsub()
     */
    public void visit_dsub() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup_x1()
     */
    public void visit_dup_x1() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup_x2()
     */
    public void visit_dup_x2() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup()
     */
    public void visit_dup() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup2_x1()
     */
    public void visit_dup2_x1() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup2_x2()
     */
    public void visit_dup2_x2() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup2()
     */
    public void visit_dup2() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_f2d()
     */
    public void visit_f2d() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_f2i()
     */
    public void visit_f2i() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_f2l()
     */
    public void visit_f2l() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fadd()
     */
    public void visit_fadd() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_faload()
     */
    public void visit_faload() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fastore()
     */
    public void visit_fastore() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fcmpg()
     */
    public void visit_fcmpg() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fcmpl()
     */
    public void visit_fcmpl() {

    }

    /**
     * @param value
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fconst(float)
     */
    public void visit_fconst(float value) {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fdiv()
     */
    public void visit_fdiv() {

    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fload(int)
     */
    public void visit_fload(int index) {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fmul()
     */
    public void visit_fmul() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fneg()
     */
    public void visit_fneg() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_frem()
     */
    public void visit_frem() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_freturn()
     */
    public void visit_freturn() {

    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fstore(int)
     */
    public void visit_fstore(int index) {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fsub()
     */
    public void visit_fsub() {

    }

    /**
     * @param fieldRef
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_getfield(org.jnode.vm.classmgr.VmConstFieldRef)
     */
    public void visit_getfield(VmConstFieldRef fieldRef) {

    }

    /**
     * @param fieldRef
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_getstatic(org.jnode.vm.classmgr.VmConstFieldRef)
     */
    public void visit_getstatic(VmConstFieldRef fieldRef) {

    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_goto(int)
     */
    public void visit_goto(int address) {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2b()
     */
    public void visit_i2b() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2c()
     */
    public void visit_i2c() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2d()
     */
    public void visit_i2d() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2f()
     */
    public void visit_i2f() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2l()
     */
    public void visit_i2l() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2s()
     */
    public void visit_i2s() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iadd()
     */
    public void visit_iadd() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iaload()
     */
    public void visit_iaload() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iand()
     */
    public void visit_iand() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iastore()
     */
    public void visit_iastore() {

    }

    /**
     * @param value
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iconst(int)
     */
    public void visit_iconst(int value) {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_idiv()
     */
    public void visit_idiv() {

    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_acmpeq(int)
     */
    public void visit_if_acmpeq(int address) {

    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_acmpne(int)
     */
    public void visit_if_acmpne(int address) {

    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmpeq(int)
     */
    public void visit_if_icmpeq(int address) {

    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmpge(int)
     */
    public void visit_if_icmpge(int address) {

    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmpgt(int)
     */
    public void visit_if_icmpgt(int address) {

    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmple(int)
     */
    public void visit_if_icmple(int address) {

    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmplt(int)
     */
    public void visit_if_icmplt(int address) {

    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmpne(int)
     */
    public void visit_if_icmpne(int address) {

    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifeq(int)
     */
    public void visit_ifeq(int address) {

    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifge(int)
     */
    public void visit_ifge(int address) {

    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifgt(int)
     */
    public void visit_ifgt(int address) {

    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifle(int)
     */
    public void visit_ifle(int address) {

    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iflt(int)
     */
    public void visit_iflt(int address) {

    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifne(int)
     */
    public void visit_ifne(int address) {

    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifnonnull(int)
     */
    public void visit_ifnonnull(int address) {

    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifnull(int)
     */
    public void visit_ifnull(int address) {

    }

    /**
     * @param index
     * @param incValue
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iinc(int, int)
     */
    public void visit_iinc(int index, int incValue) {

    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iload(int)
     */
    public void visit_iload(int index) {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_imul()
     */
    public void visit_imul() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ineg()
     */
    public void visit_ineg() {

    }

    /**
     * @param clazz
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_instanceof(org.jnode.vm.classmgr.VmConstClass)
     */
    public void visit_instanceof(VmConstClass clazz) {

    }

    /**
     * @param methodRef
     * @param count
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_invokeinterface(VmConstIMethodRef, int)
     */
    public void visit_invokeinterface(VmConstIMethodRef methodRef, int count) {

    }

    /**
     * @param methodRef
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_invokespecial(org.jnode.vm.classmgr.VmConstMethodRef)
     */
    public void visit_invokespecial(VmConstMethodRef methodRef) {

    }

    /**
     * @param methodRef
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_invokestatic(org.jnode.vm.classmgr.VmConstMethodRef)
     */
    public void visit_invokestatic(VmConstMethodRef methodRef) {

    }

    /**
     * @param methodRef
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_invokevirtual(org.jnode.vm.classmgr.VmConstMethodRef)
     */
    public void visit_invokevirtual(VmConstMethodRef methodRef) {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ior()
     */
    public void visit_ior() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_irem()
     */
    public void visit_irem() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ireturn()
     */
    public void visit_ireturn() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ishl()
     */
    public void visit_ishl() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ishr()
     */
    public void visit_ishr() {

    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_istore(int)
     */
    public void visit_istore(int index) {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_isub()
     */
    public void visit_isub() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iushr()
     */
    public void visit_iushr() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ixor()
     */
    public void visit_ixor() {

    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_jsr(int)
     */
    public void visit_jsr(int address) {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_l2d()
     */
    public void visit_l2d() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_l2f()
     */
    public void visit_l2f() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_l2i()
     */
    public void visit_l2i() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ladd()
     */
    public void visit_ladd() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_laload()
     */
    public void visit_laload() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_land()
     */
    public void visit_land() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lastore()
     */
    public void visit_lastore() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lcmp()
     */
    public void visit_lcmp() {

    }

    /**
     * @param value
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lconst(long)
     */
    public void visit_lconst(long value) {

    }

    /**
     * @param value
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ldc(VmConstString)
     */
    public void visit_ldc(VmConstString value) {

    }

    /**
     * @param value
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ldc(VmConstClass)
     */
    public void visit_ldc(VmConstClass value) {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ldiv()
     */
    public void visit_ldiv() {

    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lload(int)
     */
    public void visit_lload(int index) {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lmul()
     */
    public void visit_lmul() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lneg()
     */
    public void visit_lneg() {

    }

    /**
     * @param defValue
     * @param matchValues
     * @param addresses
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lookupswitch(int, int[], int[])
     */
    public void visit_lookupswitch(int defValue, int[] matchValues, int[] addresses) {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lor()
     */
    public void visit_lor() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lrem()
     */
    public void visit_lrem() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lreturn()
     */
    public void visit_lreturn() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lshl()
     */
    public void visit_lshl() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lshr()
     */
    public void visit_lshr() {

    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lstore(int)
     */
    public void visit_lstore(int index) {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lsub()
     */
    public void visit_lsub() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lushr()
     */
    public void visit_lushr() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lxor()
     */
    public void visit_lxor() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_monitorenter()
     */
    public void visit_monitorenter() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_monitorexit()
     */
    public void visit_monitorexit() {

    }

    /**
     * @param clazz
     * @param dimensions
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_multianewarray(VmConstClass, int)
     */
    public void visit_multianewarray(VmConstClass clazz, int dimensions) {

    }

    /**
     * @param clazz
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_new(org.jnode.vm.classmgr.VmConstClass)
     */
    public void visit_new(VmConstClass clazz) {

    }

    /**
     * @param type
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_newarray(int)
     */
    public void visit_newarray(int type) {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_nop()
     */
    public void visit_nop() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_pop()
     */
    public void visit_pop() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_pop2()
     */
    public void visit_pop2() {

    }

    /**
     * @param fieldRef
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_putfield(org.jnode.vm.classmgr.VmConstFieldRef)
     */
    public void visit_putfield(VmConstFieldRef fieldRef) {

    }

    /**
     * @param fieldRef
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_putstatic(org.jnode.vm.classmgr.VmConstFieldRef)
     */
    public void visit_putstatic(VmConstFieldRef fieldRef) {

    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ret(int)
     */
    public void visit_ret(int index) {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_return()
     */
    public void visit_return() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_saload()
     */
    public void visit_saload() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_sastore()
     */
    public void visit_sastore() {

    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_swap()
     */
    public void visit_swap() {

    }

    /**
     * @param defValue
     * @param lowValue
     * @param highValue
     * @param addresses
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_tableswitch(int, int, int, int[])
     */
    public void visit_tableswitch(int defValue, int lowValue, int highValue, int[] addresses) {

    }

    /**
     * Gets the address (PC) of the current instruction
     *
     * @return int
     */
    protected int getInstructionAddress() {
        return instructionAddress;
    }

    /**
     * Gets the currently visited method
     *
     * @return method
     */
    protected VmMethod getMethod() {
        return method;
    }

    /**
     * @return The parser
     */
    public final BytecodeParser getParser() {
        return this.parser;
    }

}
