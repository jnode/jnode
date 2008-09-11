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

import java.nio.ByteBuffer;

import org.jnode.vm.classmgr.VmByteCode;
import org.jnode.vm.classmgr.VmCP;
import org.jnode.vm.classmgr.VmConstClass;
import org.jnode.vm.classmgr.VmConstDouble;
import org.jnode.vm.classmgr.VmConstFieldRef;
import org.jnode.vm.classmgr.VmConstFloat;
import org.jnode.vm.classmgr.VmConstIMethodRef;
import org.jnode.vm.classmgr.VmConstInt;
import org.jnode.vm.classmgr.VmConstLong;
import org.jnode.vm.classmgr.VmConstObject;
import org.jnode.vm.classmgr.VmConstString;
import org.jnode.vm.classmgr.VmMethod;

/**
 * <description>
 *
 * @author epr
 */
public class BytecodeParser {

    private final VmByteCode bc;

    private final VmCP cp;

    private ByteBuffer bytecode;

    private final BytecodeVisitor handler;

    private int address;

    private boolean wide;

    private int opcode;

    private int paddedAddress;

    private int continueAt;

    private int endPC;

    /**
     * @return The padded address
     */
    public final int getPaddedAddress() {
        return this.paddedAddress;
    }

    /**
     * Create a new instance
     *
     * @param bc
     * @param handler
     */
    protected BytecodeParser(VmByteCode bc, BytecodeVisitor handler) {
        this.bc = bc;
        this.bytecode = bc.getBytecode();
        this.cp = bc.getCP();
        this.handler = handler;
    }

    /**
     * Parse a given bytecode
     *
     * @param bc
     * @param handler
     * @throws ClassFormatError
     */
    public static void parse(VmByteCode bc, BytecodeVisitor handler)
        throws ClassFormatError {
        new BytecodeParser(bc, handler).parse();
    }

    /**
     * Parse a given bytecode
     *
     * @param bc
     * @param handler
     * @param startPC        The program counter where to start parsing (inclusive)
     * @param endPC          The program counter where to stop parsing (exclusive)
     * @param startEndMethod Should startMethod and endMethod be called.
     * @throws ClassFormatError
     */
    public static void parse(VmByteCode bc, BytecodeVisitor handler,
                             int startPC, int endPC, boolean startEndMethod)
        throws ClassFormatError {
        new BytecodeParser(bc, handler).parse(startPC, endPC, startEndMethod);
    }

    /**
     * Parse a given bytecode
     *
     * @throws ClassFormatError
     */
    public void parse() throws ClassFormatError {
        parse(0, bytecode.limit(), true);
    }

    /**
     * Parse a selected region of the bytecode
     *
     * @param startPC  The program counter where to start parsing (inclusive)
     * @param endPCArg The program counter where to stop parsing (exclusive)
     * @throws ClassFormatError
     */
    public void parse(int startPC, int endPCArg, boolean startEndMethod)
        throws ClassFormatError {

        final BytecodeVisitor handler = this.handler;
        bytecode.position(startPC);
        this.endPC = endPCArg;
        handler.setParser(this);
        if (startEndMethod) {
            fireStartMethod(bc.getMethod());
        }

        while (bytecode.position() < endPC) {

            // The address(offset) of the current instruction
            this.continueAt = -1;
            this.address = bytecode.position();
            this.wide = false;
            fireStartInstruction(address);
            this.opcode = getu1();
            final int cpIdx;

            //counters[ opcode]++;
            switch (opcode) {
                // -- 0 --
                case 0x00:
                    handler.visit_nop();
                    break;
                case 0x01:
                    handler.visit_aconst_null();
                    break;
                case 0x02:
                    handler.visit_iconst(-1);
                    break;
                case 0x03:
                    handler.visit_iconst(0);
                    break;
                case 0x04:
                    handler.visit_iconst(1);
                    break;
                case 0x05:
                    handler.visit_iconst(2);
                    break;
                case 0x06:
                    handler.visit_iconst(3);
                    break;
                case 0x07:
                    handler.visit_iconst(4);
                    break;
                case 0x08:
                    handler.visit_iconst(5);
                    break;
                case 0x09:
                    handler.visit_lconst(0L);
                    break;
                    // -- 10 --
                case 0x0a:
                    handler.visit_lconst(1l);
                    break;
                case 0x0b:
                    handler.visit_fconst(0.0f);
                    break;
                case 0x0c:
                    handler.visit_fconst(1.0f);
                    break;
                case 0x0d:
                    handler.visit_fconst(2.0f);
                    break;
                case 0x0e:
                    handler.visit_dconst(0.0);
                    break;
                case 0x0f:
                    handler.visit_dconst(1.0);
                    break;
                case 0x10:
                    handler.visit_iconst(gets1()); // bipush
                    break;
                case 0x11:
                    handler.visit_iconst(gets2()); // sipush
                    break;
                case 0x12:
                case 0x13: {
                    if (opcode == 0x12) {
                        cpIdx = getu1();
                    } else {
                        cpIdx = getu2();
                    }
                    final VmConstObject o = (VmConstObject) cp.getAny(cpIdx);
                    switch (o.getConstType()) {
                        case VmConstObject.CONST_INT:
                            handler.visit_iconst(((VmConstInt) o).intValue());
                            break;
                        case VmConstObject.CONST_FLOAT:
                            handler.visit_fconst(((VmConstFloat) o).floatValue());
                            break;
                        case VmConstObject.CONST_CLASS:
                            handler.visit_ldc((VmConstClass) o);
                            break;
                        case VmConstObject.CONST_STRING:
                            handler.visit_ldc((VmConstString) o);
                            break;
                        default:
                            throw new ClassFormatError("Unknown constant pool type: " + o.getConstType());
                    }
                    break;
                }
                // -- 20 --
                case 0x14: {
                    final VmConstObject o = (VmConstObject) cp.getAny(getu2());
                    switch (o.getConstType()) {
                        case VmConstObject.CONST_LONG:
                            handler.visit_lconst(((VmConstLong) o).longValue());
                            break;
                        case VmConstObject.CONST_DOUBLE:
                            handler.visit_dconst(((VmConstDouble) o).doubleValue());
                            break;
                        default:
                            throw new ClassFormatError("Unknown constant pool type: " + o.getConstType());
                    }
                    break;
                }
                case 0x15:
                    handler.visit_iload(getu1());
                    break;
                case 0x16:
                    handler.visit_lload(getu1());
                    break;
                case 0x17:
                    handler.visit_fload(getu1());
                    break;
                case 0x18:
                    handler.visit_dload(getu1());
                    break;
                case 0x19:
                    handler.visit_aload(getu1());
                    break;
                case 0x1a:
                    handler.visit_iload(0);
                    break;
                case 0x1b:
                    handler.visit_iload(1);
                    break;
                case 0x1c:
                    handler.visit_iload(2);
                    break;
                case 0x1d:
                    handler.visit_iload(3);
                    break;
                    // -- 30 --
                case 0x1e:
                    handler.visit_lload(0);
                    break;
                case 0x1f:
                    handler.visit_lload(1);
                    break;
                case 0x20:
                    handler.visit_lload(2);
                    break;
                case 0x21:
                    handler.visit_lload(3);
                    break;
                case 0x22:
                    handler.visit_fload(0);
                    break;
                case 0x23:
                    handler.visit_fload(1);
                    break;
                case 0x24:
                    handler.visit_fload(2);
                    break;
                case 0x25:
                    handler.visit_fload(3);
                    break;
                case 0x26:
                    handler.visit_dload(0);
                    break;
                case 0x27:
                    handler.visit_dload(1);
                    break;
                    // -- 40 --
                case 0x28:
                    handler.visit_dload(2);
                    break;
                case 0x29:
                    handler.visit_dload(3);
                    break;
                case 0x2a:
                    handler.visit_aload(0);
                    break;
                case 0x2b:
                    handler.visit_aload(1);
                    break;
                case 0x2c:
                    handler.visit_aload(2);
                    break;
                case 0x2d:
                    handler.visit_aload(3);
                    break;
                case 0x2e:
                    handler.visit_iaload();
                    break;
                case 0x2f:
                    handler.visit_laload();
                    break;
                case 0x30:
                    handler.visit_faload();
                    break;
                case 0x31:
                    handler.visit_daload();
                    break;
                    // -- 50 --
                case 0x32:
                    handler.visit_aaload();
                    break;
                case 0x33:
                    handler.visit_baload();
                    break;
                case 0x34:
                    handler.visit_caload();
                    break;
                case 0x35:
                    handler.visit_saload();
                    break;
                case 0x36:
                    handler.visit_istore(getu1());
                    break;
                case 0x37:
                    handler.visit_lstore(getu1());
                    break;
                case 0x38:
                    handler.visit_fstore(getu1());
                    break;
                case 0x39:
                    handler.visit_dstore(getu1());
                    break;
                case 0x3a:
                    handler.visit_astore(getu1());
                    break;
                case 0x3b:
                    handler.visit_istore(0);
                    break;
                    // -- 60 --
                case 0x3c:
                    handler.visit_istore(1);
                    break;
                case 0x3d:
                    handler.visit_istore(2);
                    break;
                case 0x3e:
                    handler.visit_istore(3);
                    break;
                case 0x3f:
                    handler.visit_lstore(0);
                    break;
                case 0x40:
                    handler.visit_lstore(1);
                    break;
                case 0x41:
                    handler.visit_lstore(2);
                    break;
                case 0x42:
                    handler.visit_lstore(3);
                    break;
                case 0x43:
                    handler.visit_fstore(0);
                    break;
                case 0x44:
                    handler.visit_fstore(1);
                    break;
                case 0x45:
                    handler.visit_fstore(2);
                    break;
                    // -- 70 --
                case 0x46:
                    handler.visit_fstore(3);
                    break;
                case 0x47:
                    handler.visit_dstore(0);
                    break;
                case 0x48:
                    handler.visit_dstore(1);
                    break;
                case 0x49:
                    handler.visit_dstore(2);
                    break;
                case 0x4a:
                    handler.visit_dstore(3);
                    break;
                case 0x4b:
                    handler.visit_astore(0);
                    break;
                case 0x4c:
                    handler.visit_astore(1);
                    break;
                case 0x4d:
                    handler.visit_astore(2);
                    break;
                case 0x4e:
                    handler.visit_astore(3);
                    break;
                case 0x4f:
                    handler.visit_iastore();
                    break;
                    // -- 80 --
                case 0x50:
                    handler.visit_lastore();
                    break;
                case 0x51:
                    handler.visit_fastore();
                    break;
                case 0x52:
                    handler.visit_dastore();
                    break;
                case 0x53:
                    handler.visit_aastore();
                    break;
                case 0x54:
                    handler.visit_bastore();
                    break;
                case 0x55:
                    handler.visit_castore();
                    break;
                case 0x56:
                    handler.visit_sastore();
                    break;
                case 0x57:
                    handler.visit_pop();
                    break;
                case 0x58:
                    handler.visit_pop2();
                    break;
                case 0x59:
                    handler.visit_dup();
                    break;
                    // -- 90 --
                case 0x5a:
                    handler.visit_dup_x1();
                    break;
                case 0x5b:
                    handler.visit_dup_x2();
                    break;
                case 0x5c:
                    handler.visit_dup2();
                    break;
                case 0x5d:
                    handler.visit_dup2_x1();
                    break;
                case 0x5e:
                    handler.visit_dup2_x2();
                    break;
                case 0x5f:
                    handler.visit_swap();
                    break;
                case 0x60:
                    handler.visit_iadd();
                    break;
                case 0x61:
                    handler.visit_ladd();
                    break;
                case 0x62:
                    handler.visit_fadd();
                    break;
                case 0x63:
                    handler.visit_dadd();
                    break;
                    // -- 100 --
                case 0x64:
                    handler.visit_isub();
                    break;
                case 0x65:
                    handler.visit_lsub();
                    break;
                case 0x66:
                    handler.visit_fsub();
                    break;
                case 0x67:
                    handler.visit_dsub();
                    break;
                case 0x68:
                    handler.visit_imul();
                    break;
                case 0x69:
                    handler.visit_lmul();
                    break;
                case 0x6a:
                    handler.visit_fmul();
                    break;
                case 0x6b:
                    handler.visit_dmul();
                    break;
                case 0x6c:
                    handler.visit_idiv();
                    break;
                case 0x6d:
                    handler.visit_ldiv();
                    break;
                    // -- 110 --
                case 0x6e:
                    handler.visit_fdiv();
                    break;
                case 0x6f:
                    handler.visit_ddiv();
                    break;
                case 0x70:
                    handler.visit_irem();
                    break;
                case 0x71:
                    handler.visit_lrem();
                    break;
                case 0x72:
                    handler.visit_frem();
                    break;
                case 0x73:
                    handler.visit_drem();
                    break;
                case 0x74:
                    handler.visit_ineg();
                    break;
                case 0x75:
                    handler.visit_lneg();
                    break;
                case 0x76:
                    handler.visit_fneg();
                    break;
                case 0x77:
                    handler.visit_dneg();
                    break;
                    // -- 120 --
                case 0x78:
                    handler.visit_ishl();
                    break;
                case 0x79:
                    handler.visit_lshl();
                    break;
                case 0x7a:
                    handler.visit_ishr();
                    break;
                case 0x7b:
                    handler.visit_lshr();
                    break;
                case 0x7c:
                    handler.visit_iushr();
                    break;
                case 0x7d:
                    handler.visit_lushr();
                    break;
                case 0x7e:
                    handler.visit_iand();
                    break;
                case 0x7f:
                    handler.visit_land();
                    break;
                case 0x80:
                    handler.visit_ior();
                    break;
                case 0x81:
                    handler.visit_lor();
                    break;
                    // -- 130 --
                case 0x82:
                    handler.visit_ixor();
                    break;
                case 0x83:
                    handler.visit_lxor();
                    break;
                case 0x84: {
                    int idx = getu1();
                    handler.visit_iinc(idx, gets1());
                    break;
                }
                case 0x85:
                    handler.visit_i2l();
                    break;
                case 0x86:
                    handler.visit_i2f();
                    break;
                case 0x87:
                    handler.visit_i2d();
                    break;
                case 0x88:
                    handler.visit_l2i();
                    break;
                case 0x89:
                    handler.visit_l2f();
                    break;
                case 0x8a:
                    handler.visit_l2d();
                    break;
                case 0x8b:
                    handler.visit_f2i();
                    break;
                    // -- 140 --
                case 0x8c:
                    handler.visit_f2l();
                    break;
                case 0x8d:
                    handler.visit_f2d();
                    break;
                case 0x8e:
                    handler.visit_d2i();
                    break;
                case 0x8f:
                    handler.visit_d2l();
                    break;
                case 0x90:
                    handler.visit_d2f();
                    break;
                case 0x91:
                    handler.visit_i2b();
                    break;
                case 0x92:
                    handler.visit_i2c();
                    break;
                case 0x93:
                    handler.visit_i2s();
                    break;
                case 0x94:
                    handler.visit_lcmp();
                    break;
                case 0x95:
                    handler.visit_fcmpl();
                    break;
                    // -- 150 --
                case 0x96:
                    handler.visit_fcmpg();
                    break;
                case 0x97:
                    handler.visit_dcmpl();
                    break;
                case 0x98:
                    handler.visit_dcmpg();
                    break;
                case 0x99:
                    handler.visit_ifeq(address + gets2());
                    break;
                case 0x9a:
                    handler.visit_ifne(address + gets2());
                    break;
                case 0x9b:
                    handler.visit_iflt(address + gets2());
                    break;
                case 0x9c:
                    handler.visit_ifge(address + gets2());
                    break;
                case 0x9d:
                    handler.visit_ifgt(address + gets2());
                    break;
                case 0x9e:
                    handler.visit_ifle(address + gets2());
                    break;
                case 0x9f:
                    handler.visit_if_icmpeq(address + gets2());
                    break;
                    // -- 160 --
                case 0xa0:
                    handler.visit_if_icmpne(address + gets2());
                    break;
                case 0xa1:
                    handler.visit_if_icmplt(address + gets2());
                    break;
                case 0xa2:
                    handler.visit_if_icmpge(address + gets2());
                    break;
                case 0xa3:
                    handler.visit_if_icmpgt(address + gets2());
                    break;
                case 0xa4:
                    handler.visit_if_icmple(address + gets2());
                    break;
                case 0xa5:
                    handler.visit_if_acmpeq(address + gets2());
                    break;
                case 0xa6:
                    handler.visit_if_acmpne(address + gets2());
                    break;
                case 0xa7:
                    handler.visit_goto(address + gets2());
                    break;
                case 0xa8:
                    handler.visit_jsr(address + gets2());
                    break;
                case 0xa9:
                    handler.visit_ret(getu1());
                    break;
                    // -- 170 --
                case 0xaa: {
                    skipPadding();
                    int defAddress = address + gets4();
                    int lowValue = gets4();
                    int highValue = gets4();
                    if (highValue < lowValue) {
                        throw new ClassFormatError(
                            "tableSwitch high < low! (high=" + highValue
                                + ", low=" + lowValue + ")");
                    }
                    int cnt = highValue - lowValue + 1;
                    int addresses[] = new int[cnt];
                    for (int i = 0; i < cnt; i++) {
                        addresses[i] = address + gets4();
                    }
                    handler.visit_tableswitch(defAddress, lowValue, highValue, addresses);
                    break;
                }
                case 0xab: {
                    skipPadding();
                    int defAddress = address + gets4();
                    int cnt = getu4();
                    int matches[] = new int[cnt];
                    int addresses[] = new int[cnt];
                    for (int i = 0; i < cnt; i++) {
                        matches[i] = gets4();
                        addresses[i] = address + gets4();
                    }
                    handler.visit_lookupswitch(defAddress, matches, addresses);
                    break;
                }
                case 0xac:
                    handler.visit_ireturn();
                    break;
                case 0xad:
                    handler.visit_lreturn();
                    break;
                case 0xae:
                    handler.visit_freturn();
                    break;
                case 0xaf:
                    handler.visit_dreturn();
                    break;
                case 0xb0:
                    handler.visit_areturn();
                    break;
                case 0xb1:
                    handler.visit_return();
                    break;
                case 0xb2: {
                    VmConstFieldRef field = cp.getConstFieldRef(getu2());
                    handler.visit_getstatic(field);
                    break;
                }
                case 0xb3: {
                    VmConstFieldRef field = cp.getConstFieldRef(getu2());
                    handler.visit_putstatic(field);
                    break;
                }
                // -- 180 --
                case 0xb4: {
                    VmConstFieldRef field = cp.getConstFieldRef(getu2());
                    handler.visit_getfield(field);
                    break;
                }
                case 0xb5: {
                    VmConstFieldRef field = cp.getConstFieldRef(getu2());
                    handler.visit_putfield(field);
                    break;
                }
                case 0xb6:
                    handler.visit_invokevirtual(cp.getConstMethodRef(getu2()));
                    break;
                case 0xb7:
                    handler.visit_invokespecial(cp.getConstMethodRef(getu2()));
                    break;
                case 0xb8:
                    handler.visit_invokestatic(cp.getConstMethodRef(getu2()));
                    break;
                case 0xb9: {
                    VmConstIMethodRef ref = cp.getConstIMethodRef(getu2());
                    int count = getu1();
                    skip();
                    handler.visit_invokeinterface(ref, count);
                    break;
                }
                //case 0xba: handler.throw_invalid_opcode ; unused
                case 0xbb:
                    handler.visit_new(cp.getConstClass(getu2()));
                    break;
                case 0xbc:
                    handler.visit_newarray(getu1());
                    break;
                case 0xbd:
                    handler.visit_anewarray(cp.getConstClass(getu2()));
                    break;
                    // -- 190 --
                case 0xbe:
                    handler.visit_arraylength();
                    break;
                case 0xbf:
                    handler.visit_athrow();
                    break;
                case 0xc0:
                    handler.visit_checkcast(cp.getConstClass(getu2()));
                    break;
                case 0xc1:
                    handler.visit_instanceof(cp.getConstClass(getu2()));
                    break;
                case 0xc2:
                    handler.visit_monitorenter();
                    break;
                case 0xc3:
                    handler.visit_monitorexit();
                    break;
                case 0xc4: {
                    wide = true;
                    int opcode = getu1();
                    if (opcode == 0x84) {
                        int idx = getu2();
                        int constValue = gets2();
                        handler.visit_iinc(idx, constValue);
                    } else {
                        int idx = getu2();
                        switch (opcode) {
                            case 0x15:
                                handler.visit_iload(idx);
                                break;
                            case 0x16:
                                handler.visit_lload(idx);
                                break;
                            case 0x17:
                                handler.visit_fload(idx);
                                break;
                            case 0x18:
                                handler.visit_dload(idx);
                                break;
                            case 0x19:
                                handler.visit_aload(idx);
                                break;
                            case 0x36:
                                handler.visit_istore(idx);
                                break;
                            case 0x37:
                                handler.visit_lstore(idx);
                                break;
                            case 0x38:
                                handler.visit_fstore(idx);
                                break;
                            case 0x39:
                                handler.visit_dstore(idx);
                                break;
                            case 0x3a:
                                handler.visit_astore(idx);
                                break;
                            default:
                                throw new ClassFormatError(
                                    "Invalid opcode in wide instruction");
                        }
                    }
                    break;
                }
                case 0xc5: {
                    VmConstClass clazz = cp.getConstClass(getu2());
                    int dims = getu1();
                    handler.visit_multianewarray(clazz, dims);
                    break;
                }
                case 0xc6:
                    handler.visit_ifnull(address + gets2());
                    break;
                case 0xc7:
                    handler.visit_ifnonnull(address + gets2());
                    break;
                    // -- 200 --
                case 0xc8:
                    handler.visit_goto(address + gets4());
                    break;
                case 0xc9:
                    handler.visit_jsr(address + gets4());
                    break;
                default:
                    throw new ClassFormatError("Invalid opcode");
            }
            fireEndInstruction();
            if (continueAt >= 0) {
                bytecode.position(continueAt);
            }
        }
        if (startEndMethod) {
            fireEndMethod();
        }
    }

    /**
     * Get an unsigned byte from the next bytecode position
     *
     * @return int
     */
    private final int getu1() {
        return bytecode.get() & 0xFF;
    }

    /**
     * Get an unsigned short from the next bytecode position
     *
     * @return int
     */
    private final int getu2() {
        int v1 = bytecode.get() & 0xFF;
        int v2 = bytecode.get() & 0xFF;
        return (v1 << 8) | v2;
    }

    /**
     * Get an unsigned int from the next bytecode position
     *
     * @return int
     */
    private final int getu4() {
        int v1 = bytecode.get() & 0xFF;
        int v2 = bytecode.get() & 0xFF;
        int v3 = bytecode.get() & 0xFF;
        int v4 = bytecode.get() & 0xFF;
        return (v1 << 24) | (v2 << 16) | (v3 << 8) | v4;
    }

    /**
     * Get a byte from the next bytecode position
     *
     * @return byte
     */
    private final byte gets1() {
        return bytecode.get();
    }

    /**
     * Get a short from the next bytecode positions
     *
     * @return short
     */
    private final short gets2() {
        int v1 = bytecode.get() & 0xFF;
        int v2 = bytecode.get() & 0xFF;
        return (short) ((v1 << 8) | v2);
    }

    /**
     * Get an int from the next bytecode position
     *
     * @return int
     */
    private final int gets4() {
        int v1 = bytecode.get() & 0xFF;
        int v2 = bytecode.get() & 0xFF;
        int v3 = bytecode.get() & 0xFF;
        int v4 = bytecode.get() & 0xFF;
        return (v1 << 24) | (v2 << 16) | (v3 << 8) | v4;
    }

    private final void skipPadding() {
        while (bytecode.position() % 4 != 0) {
            bytecode.get();
        }
        paddedAddress = bytecode.position();
    }

    private final void skip() {
        bytecode.get();
    }

    /**
     * Gets the address of the current instruction in the parse method.
     *
     * @return int
     */
    public final int getAddress() {
        return this.address;
    }

    /**
     * Gets the address of the next instruction in the parse method.
     *
     * @return int
     */
    public final int getNextAddress() {
        return bytecode.position();
    }

    /**
     * Is the current instruction a wide instruction
     *
     * @return boolean
     */
    public final boolean isWide() {
        return this.wide;
    }

    /**
     * Gets the opcode of the current instruction
     *
     * @return int
     */
    public final int getOpcode() {
        return this.opcode;
    }

    public final void setContinueAt(int offset) {
        continueAt = offset;
    }

    public final void setCode(ByteBuffer bytecode) {
        this.bytecode = bytecode;
    }

    public final void adjustEndPC(int delta) {
        endPC += delta;
    }

    public final void setEndPC(int offset) {
        endPC = offset;
    }

    /**
     * @return The end PC of the parsable block
     */
    public int getEndPC() {
        return this.endPC;
    }

    /**
     * Call the startInstruction method of the handler.
     *
     * @param address
     */
    protected void fireStartInstruction(int address) {
        handler.startInstruction(address);
    }

    /**
     * Call the endInstruction method of the handler.
     */
    protected void fireEndInstruction() {
        handler.endInstruction();
    }

    /**
     * Call the startInstruction method of the handler.
     *
     * @param method
     */
    protected void fireStartMethod(VmMethod method) {
        handler.startMethod(method);
    }

    /**
     * Call the endMethod method of the handler.
     */
    protected void fireEndMethod() {
        handler.endMethod();
    }

    public static void dumpStatistics() {
        /*for (int i = 0; i < 256; i++) {
            if (counters[ i] != 0) {
                System.out.println("0x" + Integer.toHexString(i) + "\t" + counters[ i]);
            }
        }*/
    }
}
