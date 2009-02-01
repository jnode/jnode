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
 
package org.jnode.vm.bytecode;

import org.jnode.vm.classmgr.VmConstClass;
import org.jnode.vm.classmgr.VmConstFieldRef;
import org.jnode.vm.classmgr.VmConstIMethodRef;
import org.jnode.vm.classmgr.VmConstMethodRef;
import org.jnode.vm.classmgr.VmConstString;
import org.jnode.vm.classmgr.VmMethod;

/**
 * <description>
 *
 * @author epr
 */
public abstract class BytecodeVisitor {

    public abstract void setParser(BytecodeParser parser);

    public abstract void startMethod(VmMethod method);

    public abstract void endMethod();

    public abstract void startInstruction(int address);

    public abstract void endInstruction();

    public abstract void visit_nop();

    public abstract void visit_aconst_null();

    public abstract void visit_iconst(int value);

    public abstract void visit_lconst(long value);

    // -- 10 --
    public abstract void visit_fconst(float value);

    public abstract void visit_dconst(double value);

    /**
     * @deprecated
     */
    public final void visit_sipush(short value) {
    }

    public abstract void visit_ldc(VmConstString value);

    public abstract void visit_ldc(VmConstClass value);

    // -- 20 --
    public abstract void visit_iload(int index);

    public abstract void visit_lload(int index);

    public abstract void visit_fload(int index);

    public abstract void visit_dload(int index);

    public abstract void visit_aload(int index);

    // -- 30 --
    public abstract void visit_iaload();

    public abstract void visit_laload();

    public abstract void visit_faload();

    public abstract void visit_daload();

    // -- 50 --
    public abstract void visit_aaload();

    public abstract void visit_baload();

    public abstract void visit_caload();

    public abstract void visit_saload();

    public abstract void visit_istore(int index);

    public abstract void visit_lstore(int index);

    public abstract void visit_fstore(int index);

    public abstract void visit_dstore(int index);

    public abstract void visit_astore(int index);

    public abstract void visit_iastore();

    // -- 80 --
    public abstract void visit_lastore();

    public abstract void visit_fastore();

    public abstract void visit_dastore();

    public abstract void visit_aastore();

    public abstract void visit_bastore();

    public abstract void visit_castore();

    public abstract void visit_sastore();

    public abstract void visit_pop();

    public abstract void visit_pop2();

    public abstract void visit_dup();

    // -- 90 --
    public abstract void visit_dup_x1();

    public abstract void visit_dup_x2();

    public abstract void visit_dup2();

    public abstract void visit_dup2_x1();

    public abstract void visit_dup2_x2();

    public abstract void visit_swap();

    public abstract void visit_iadd();

    public abstract void visit_ladd();

    public abstract void visit_fadd();

    public abstract void visit_dadd();

    // -- 100 --
    public abstract void visit_isub();

    public abstract void visit_lsub();

    public abstract void visit_fsub();

    public abstract void visit_dsub();

    public abstract void visit_imul();

    public abstract void visit_lmul();

    public abstract void visit_fmul();

    public abstract void visit_dmul();

    public abstract void visit_idiv();

    public abstract void visit_ldiv();

    // -- 110 --
    public abstract void visit_fdiv();

    public abstract void visit_ddiv();

    public abstract void visit_irem();

    public abstract void visit_lrem();

    public abstract void visit_frem();

    public abstract void visit_drem();

    public abstract void visit_ineg();

    public abstract void visit_lneg();

    public abstract void visit_fneg();

    public abstract void visit_dneg();

    // -- 120 --
    public abstract void visit_ishl();

    public abstract void visit_lshl();

    public abstract void visit_ishr();

    public abstract void visit_lshr();

    public abstract void visit_iushr();

    public abstract void visit_lushr();

    public abstract void visit_iand();

    public abstract void visit_land();

    public abstract void visit_ior();

    public abstract void visit_lor();

    // -- 130 --
    public abstract void visit_ixor();

    public abstract void visit_lxor();

    public abstract void visit_iinc(int index, int incValue);

    public abstract void visit_i2l();

    public abstract void visit_i2f();

    public abstract void visit_i2d();

    public abstract void visit_l2i();

    public abstract void visit_l2f();

    public abstract void visit_l2d();

    public abstract void visit_f2i();

    // -- 140 --
    public abstract void visit_f2l();

    public abstract void visit_f2d();

    public abstract void visit_d2i();

    public abstract void visit_d2l();

    public abstract void visit_d2f();

    public abstract void visit_i2b();

    public abstract void visit_i2c();

    public abstract void visit_i2s();

    public abstract void visit_lcmp();

    public abstract void visit_fcmpl();

    // -- 150 --
    public abstract void visit_fcmpg();

    public abstract void visit_dcmpl();

    public abstract void visit_dcmpg();

    public abstract void visit_ifeq(int address);

    public abstract void visit_ifne(int address);

    public abstract void visit_iflt(int address);

    public abstract void visit_ifge(int address);

    public abstract void visit_ifgt(int address);

    public abstract void visit_ifle(int address);

    public abstract void visit_if_icmpeq(int address);

    // -- 160 --
    public abstract void visit_if_icmpne(int address);

    public abstract void visit_if_icmplt(int address);

    public abstract void visit_if_icmpge(int address);

    public abstract void visit_if_icmpgt(int address);

    public abstract void visit_if_icmple(int address);

    public abstract void visit_if_acmpeq(int address);

    public abstract void visit_if_acmpne(int address);

    public abstract void visit_goto(int address);

    public abstract void visit_jsr(int address);

    public abstract void visit_ret(int index);

    // -- 170 --
    public abstract void visit_tableswitch(int defValue, int lowValue, int highValue, int[] addresses);

    public abstract void visit_lookupswitch(int defValue, int[] matchValues, int[] addresses);

    public abstract void visit_ireturn();

    public abstract void visit_lreturn();

    public abstract void visit_freturn();

    public abstract void visit_dreturn();

    public abstract void visit_areturn();

    public abstract void visit_return();

    public abstract void visit_getstatic(VmConstFieldRef fieldRef);

    public abstract void visit_putstatic(VmConstFieldRef fieldRef);

    // -- 180 --
    public abstract void visit_getfield(VmConstFieldRef fieldRef);

    public abstract void visit_putfield(VmConstFieldRef fieldRef);

    public abstract void visit_invokevirtual(VmConstMethodRef methodRef);

    public abstract void visit_invokespecial(VmConstMethodRef methodRef);

    public abstract void visit_invokestatic(VmConstMethodRef methodRef);

    public abstract void visit_invokeinterface(VmConstIMethodRef methodRef, int count);

    public abstract void visit_new(VmConstClass clazz);

    public abstract void visit_newarray(int type);

    public abstract void visit_anewarray(VmConstClass clazz);

    // -- 190 --
    public abstract void visit_arraylength();

    public abstract void visit_athrow();

    public abstract void visit_checkcast(VmConstClass clazz);

    public abstract void visit_instanceof(VmConstClass clazz);

    public abstract void visit_monitorenter();

    public abstract void visit_monitorexit();

    public abstract void visit_multianewarray(VmConstClass clazz, int dimensions);

    public abstract void visit_ifnull(int address);

    public abstract void visit_ifnonnull(int address);
}
