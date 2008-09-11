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

import java.io.PrintStream;

import org.jnode.vm.classmgr.VmConstClass;
import org.jnode.vm.classmgr.VmConstFieldRef;
import org.jnode.vm.classmgr.VmConstIMethodRef;
import org.jnode.vm.classmgr.VmConstMethodRef;
import org.jnode.vm.classmgr.VmConstString;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;

/**
 * <description>
 *
 * @author epr
 */
public class BytecodeViewer extends BytecodeVisitor {

    private int address;
    private ControlFlowGraph cfg;
    private String indent = "";
    private final PrintStream out;

    /**
     * @param parser
     * @see org.jnode.vm.bytecode.BytecodeVisitor#setParser(org.jnode.vm.bytecode.BytecodeParser)
     */
    public void setParser(BytecodeParser parser) {
    }

    /**
     * Constructor for BytecodeViewer.
     */
    public BytecodeViewer() {
        this(null, System.out);
    }

    /**
     * Constructor for BytecodeViewer.
     */
    public BytecodeViewer(PrintStream out) {
        this(null, out);
    }

    /**
     * Constructor for BytecodeViewer.
     *
     * @param cfg
     */
    public BytecodeViewer(ControlFlowGraph cfg) {
        this(cfg, System.out);
    }

    /**
     * Constructor for BytecodeViewer.
     *
     * @param cfg
     */
    public BytecodeViewer(ControlFlowGraph cfg, PrintStream out) {
        this.cfg = cfg;
        this.out = out;
    }

    /**
     * @param method
     * @see org.jnode.vm.bytecode.BytecodeVisitor#startMethod(org.jnode.vm.classmgr.VmMethod)
     */
    public void startMethod(VmMethod method) {
        out.println("Method: " + method.getName() + ", #locals " + method.getBytecode().getNoLocals());
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#endMethod()
     */
    public void endMethod() {
        out.println("end\n");
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#startInstruction(int)
     */
    public void startInstruction(int address) {
        this.address = address;
        if (cfg != null) {
            final BasicBlock bb = cfg.getBasicBlock(address);
            if (bb.getStartPC() == address) {
                out("-- Start of Basic Block " + bb + " --");
            }
        }
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#endInstruction()
     */
    public void endInstruction() {
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_nop()
     */
    public void visit_nop() {
        out("nop");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_aconst_null()
     */
    public void visit_aconst_null() {
        out("aconst_null");
    }

    /**
     * @param value
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iconst(int)
     */
    public void visit_iconst(int value) {
        out("iconst " + value);
    }

    /**
     * @param value
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lconst(long)
     */
    public void visit_lconst(long value) {
        out("lconst " + value);
    }

    /**
     * @param value
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fconst(float)
     */
    public void visit_fconst(float value) {
        out("fconst " + value);
    }

    /**
     * @param value
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dconst(double)
     */
    public void visit_dconst(double value) {
        out("dconst " + value);
    }

    /**
     * @param value
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ldc(VmConstString)
     */
    public void visit_ldc(VmConstString value) {
        out("ldc " + value);
    }

    /**
     * @param value
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ldc(VmConstClass)
     */
    public void visit_ldc(VmConstClass value) {
        out("ldc " + value);
    }

    /**
     * @param value
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ldc(VmConstClass)
     */
    public void visit_ldc(VmType<?> value) {
        out("ldc-type " + value.getName());
    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iload(int)
     */
    public void visit_iload(int index) {
        out("iload " + index);
    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lload(int)
     */
    public void visit_lload(int index) {
        out("lload " + index);
    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fload(int)
     */
    public void visit_fload(int index) {
        out("fload " + index);
    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dload(int)
     */
    public void visit_dload(int index) {
        out("dload " + index);
    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_aload(int)
     */
    public void visit_aload(int index) {
        out("aload " + index);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iaload()
     */
    public void visit_iaload() {
        out("iaload");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_laload()
     */
    public void visit_laload() {
        out("laload");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_faload()
     */
    public void visit_faload() {
        out("faload");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_daload()
     */
    public void visit_daload() {
        out("daload");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_aaload()
     */
    public void visit_aaload() {
        out("aaload");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_baload()
     */
    public void visit_baload() {
        out("baload");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_caload()
     */
    public void visit_caload() {
        out("caload");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_saload()
     */
    public void visit_saload() {
        out("saload");
    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_istore(int)
     */
    public void visit_istore(int index) {
        out("istore " + index);
    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lstore(int)
     */
    public void visit_lstore(int index) {
        out("lstore " + index);
    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fstore(int)
     */
    public void visit_fstore(int index) {
        out("fstore " + index);
    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dstore(int)
     */
    public void visit_dstore(int index) {
        out("dstore " + index);
    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_astore(int)
     */
    public void visit_astore(int index) {
        out("astore " + index);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iastore()
     */
    public void visit_iastore() {
        out("iastore");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lastore()
     */
    public void visit_lastore() {
        out("lastore");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fastore()
     */
    public void visit_fastore() {
        out("fastore");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dastore()
     */
    public void visit_dastore() {
        out("dastore");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_aastore()
     */
    public void visit_aastore() {
        out("aastore");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_bastore()
     */
    public void visit_bastore() {
        out("bastore");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_castore()
     */
    public void visit_castore() {
        out("castore");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_sastore()
     */
    public void visit_sastore() {
        out("sastore");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_pop()
     */
    public void visit_pop() {
        out("pop");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_pop2()
     */
    public void visit_pop2() {
        out("pop2");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup()
     */
    public void visit_dup() {
        out("dup");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup_x1()
     */
    public void visit_dup_x1() {
        out("dup_x1");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup_x2()
     */
    public void visit_dup_x2() {
        out("dup_x2");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup2()
     */
    public void visit_dup2() {
        out("dup2");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup2_x1()
     */
    public void visit_dup2_x1() {
        out("dup2_x1");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dup2_x2()
     */
    public void visit_dup2_x2() {
        out("dup2_x2");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_swap()
     */
    public void visit_swap() {
        out("swap");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iadd()
     */
    public void visit_iadd() {
        out("iadd");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ladd()
     */
    public void visit_ladd() {
        out("ladd");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fadd()
     */
    public void visit_fadd() {
        out("fadd");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dadd()
     */
    public void visit_dadd() {
        out("dadd");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_isub()
     */
    public void visit_isub() {
        out("isub");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lsub()
     */
    public void visit_lsub() {
        out("lsub");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fsub()
     */
    public void visit_fsub() {
        out("fsub");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dsub()
     */
    public void visit_dsub() {
        out("dsub");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_imul()
     */
    public void visit_imul() {
        out("imul");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lmul()
     */
    public void visit_lmul() {
        out("lmul");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fmul()
     */
    public void visit_fmul() {
        out("fmul");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dmul()
     */
    public void visit_dmul() {
        out("dmul");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_idiv()
     */
    public void visit_idiv() {
        out("idiv");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ldiv()
     */
    public void visit_ldiv() {
        out("ldiv");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fdiv()
     */
    public void visit_fdiv() {
        out("fdiv");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ddiv()
     */
    public void visit_ddiv() {
        out("ddiv");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_irem()
     */
    public void visit_irem() {
        out("irem");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lrem()
     */
    public void visit_lrem() {
        out("lrem");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_frem()
     */
    public void visit_frem() {
        out("frem");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_drem()
     */
    public void visit_drem() {
        out("drem");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ineg()
     */
    public void visit_ineg() {
        out("ineg");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lneg()
     */
    public void visit_lneg() {
        out("lneg");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fneg()
     */
    public void visit_fneg() {
        out("fneg");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dneg()
     */
    public void visit_dneg() {
        out("dneg");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ishl()
     */
    public void visit_ishl() {
        out("ishl");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lshl()
     */
    public void visit_lshl() {
        out("lshl");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ishr()
     */
    public void visit_ishr() {
        out("ishr");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lshr()
     */
    public void visit_lshr() {
        out("lshr");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iushr()
     */
    public void visit_iushr() {
        out("iushr");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lushr()
     */
    public void visit_lushr() {
        out("lushr");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iand()
     */
    public void visit_iand() {
        out("iand");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_land()
     */
    public void visit_land() {
        out("land");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ior()
     */
    public void visit_ior() {
        out("ior");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lor()
     */
    public void visit_lor() {
        out("lor");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ixor()
     */
    public void visit_ixor() {
        out("ixor");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lxor()
     */
    public void visit_lxor() {
        out("lxor");
    }

    /**
     * @param index
     * @param incValue
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iinc(int, int)
     */
    public void visit_iinc(int index, int incValue) {
        out("iinc index=" + index + ", incr=" + incValue);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2l()
     */
    public void visit_i2l() {
        out("i2l");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2f()
     */
    public void visit_i2f() {
        out("i2f");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2d()
     */
    public void visit_i2d() {
        out("i2d");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_l2i()
     */
    public void visit_l2i() {
        out("l2i");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_l2f()
     */
    public void visit_l2f() {
        out("l2f");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_l2d()
     */
    public void visit_l2d() {
        out("l2d");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_f2i()
     */
    public void visit_f2i() {
        out("f2i");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_f2l()
     */
    public void visit_f2l() {
        out("f2l");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_f2d()
     */
    public void visit_f2d() {
        out("f2d");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_d2i()
     */
    public void visit_d2i() {
        out("d2i");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_d2l()
     */
    public void visit_d2l() {
        out("d2l");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_d2f()
     */
    public void visit_d2f() {
        out("d2f");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2b()
     */
    public void visit_i2b() {
        out("i2b");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2c()
     */
    public void visit_i2c() {
        out("i2c");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_i2s()
     */
    public void visit_i2s() {
        out("i2s");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lcmp()
     */
    public void visit_lcmp() {
        out("lcmp");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fcmpl()
     */
    public void visit_fcmpl() {
        out("fcmpl");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_fcmpg()
     */
    public void visit_fcmpg() {
        out("fcmpg");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dcmpl()
     */
    public void visit_dcmpl() {
        out("dcmpl");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dcmpg()
     */
    public void visit_dcmpg() {
        out("dcmpg");
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifeq(int)
     */
    public void visit_ifeq(int address) {
        out("ifeq " + address);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifne(int)
     */
    public void visit_ifne(int address) {
        out("ifne " + address);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_iflt(int)
     */
    public void visit_iflt(int address) {
        out("iflt " + address);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifge(int)
     */
    public void visit_ifge(int address) {
        out("ifge " + address);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifgt(int)
     */
    public void visit_ifgt(int address) {
        out("ifgt " + address);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifle(int)
     */
    public void visit_ifle(int address) {
        out("ifle " + address);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmpeq(int)
     */
    public void visit_if_icmpeq(int address) {
        out("if_icmpeq " + address);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmpne(int)
     */
    public void visit_if_icmpne(int address) {
        out("if_icmpne " + address);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmplt(int)
     */
    public void visit_if_icmplt(int address) {
        out("if_icmplt " + address);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmpge(int)
     */
    public void visit_if_icmpge(int address) {
        out("if_icmpge " + address);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmpgt(int)
     */
    public void visit_if_icmpgt(int address) {
        out("if_icmpgt " + address);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_icmple(int)
     */
    public void visit_if_icmple(int address) {
        out("if_icmple " + address);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_acmpeq(int)
     */
    public void visit_if_acmpeq(int address) {
        out("if_acmpeq " + address);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_if_acmpne(int)
     */
    public void visit_if_acmpne(int address) {
        out("if_acmpne " + address);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_goto(int)
     */
    public void visit_goto(int address) {
        out("goto " + address);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_jsr(int)
     */
    public void visit_jsr(int address) {
        out("jsr " + address);
    }

    /**
     * @param index
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ret(int)
     */
    public void visit_ret(int index) {
        out("ret " + index);
    }

    /**
     * @param defValue
     * @param lowValue
     * @param highValue
     * @param addresses
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_tableswitch(int, int, int, int[])
     */
    public void visit_tableswitch(int defValue, int lowValue, int highValue, int[] addresses) {
        out("tableswitch def=" + defValue);
        for (int i = 0; i < addresses.length; i++) {
            out("\t" + (lowValue + i) + "\t-> " + addresses[i]);
        }
    }

    /**
     * @param defValue
     * @param matchValues
     * @param addresses
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lookupswitch(int, int[], int[])
     */
    public void visit_lookupswitch(int defValue, int[] matchValues, int[] addresses) {
        out("lookupswitch def=" + defValue);
        for (int i = 0; i < addresses.length; i++) {
            out("\t" + matchValues[i] + "\t-> " + addresses[i]);
        }
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ireturn()
     */
    public void visit_ireturn() {
        out("ireturn");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_lreturn()
     */
    public void visit_lreturn() {
        out("lreturn");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_freturn()
     */
    public void visit_freturn() {
        out("freturn");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_dreturn()
     */
    public void visit_dreturn() {
        out("dreturn");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_areturn()
     */
    public void visit_areturn() {
        out("areturn");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_return()
     */
    public void visit_return() {
        out("return");
    }

    /**
     * @param fieldRef
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_getstatic(org.jnode.vm.classmgr.VmConstFieldRef)
     */
    public void visit_getstatic(VmConstFieldRef fieldRef) {
        out("getstatic " + fieldRef);
    }

    /**
     * @param fieldRef
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_putstatic(org.jnode.vm.classmgr.VmConstFieldRef)
     */
    public void visit_putstatic(VmConstFieldRef fieldRef) {
        out("putstatic " + fieldRef);
    }

    /**
     * @param fieldRef
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_getfield(org.jnode.vm.classmgr.VmConstFieldRef)
     */
    public void visit_getfield(VmConstFieldRef fieldRef) {
        out("getfield " + fieldRef);
    }

    /**
     * @param fieldRef
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_putfield(org.jnode.vm.classmgr.VmConstFieldRef)
     */
    public void visit_putfield(VmConstFieldRef fieldRef) {
        out("putfield " + fieldRef);
    }

    /**
     * @param methodRef
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_invokevirtual(org.jnode.vm.classmgr.VmConstMethodRef)
     */
    public void visit_invokevirtual(VmConstMethodRef methodRef) {
        out("invokevirtual " + methodRef);
    }

    /**
     * @param methodRef
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_invokespecial(org.jnode.vm.classmgr.VmConstMethodRef)
     */
    public void visit_invokespecial(VmConstMethodRef methodRef) {
        out("invokespecial " + methodRef);
    }

    /**
     * @param methodRef
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_invokestatic(org.jnode.vm.classmgr.VmConstMethodRef)
     */
    public void visit_invokestatic(VmConstMethodRef methodRef) {
        out("invokestatic " + methodRef);
    }

    /**
     * @param methodRef
     * @param count
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_invokeinterface(org.jnode.vm.classmgr.VmConstIMethodRef, int)
     */
    public void visit_invokeinterface(VmConstIMethodRef methodRef, int count) {
        out("invokeinterface " + methodRef + ", count=" + count);
    }

    /**
     * @param clazz
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_new(org.jnode.vm.classmgr.VmConstClass)
     */
    public void visit_new(VmConstClass clazz) {
        out("new " + clazz);
    }

    /**
     * @param type
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_newarray(int)
     */
    public void visit_newarray(int type) {
        out("newarray " + type);
    }

    /**
     * @param clazz
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_anewarray(org.jnode.vm.classmgr.VmConstClass)
     */
    public void visit_anewarray(VmConstClass clazz) {
        out("anewarray " + clazz);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_arraylength()
     */
    public void visit_arraylength() {
        out("arraylength");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_athrow()
     */
    public void visit_athrow() {
        out("athrow");
    }

    /**
     * @param clazz
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_checkcast(org.jnode.vm.classmgr.VmConstClass)
     */
    public void visit_checkcast(VmConstClass clazz) {
        out("checkcast " + clazz);
    }

    /**
     * @param clazz
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_instanceof(org.jnode.vm.classmgr.VmConstClass)
     */
    public void visit_instanceof(VmConstClass clazz) {
        out("instanceof " + clazz);
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_monitorenter()
     */
    public void visit_monitorenter() {
        out("monitorenter");
    }

    /**
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_monitorexit()
     */
    public void visit_monitorexit() {
        out("monitorexit");
    }

    /**
     * @param clazz
     * @param dimensions
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_multianewarray(org.jnode.vm.classmgr.VmConstClass, int)
     */
    public void visit_multianewarray(VmConstClass clazz, int dimensions) {
        out("multianewarray " + clazz + " " + dimensions);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifnull(int)
     */
    public void visit_ifnull(int address) {
        out("ifnull " + address);
    }

    /**
     * @param address
     * @see org.jnode.vm.bytecode.BytecodeVisitor#visit_ifnonnull(int)
     */
    public void visit_ifnonnull(int address) {
        out("ifnonnull " + address);
    }

    public void out(String line) {
        out.print(indent);
        out.print(address);
        out.print(":\t");
        out.println(line);
    }

    public void indent() {
        indent += "\t";
    }

    public void unindent() {
        indent = indent.substring(0, indent.length() - 1);
    }

    protected void out(Object obj) {
        String str = obj.toString();
        out(str);
    }
}
