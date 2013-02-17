/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.assembler.x86;

import org.jnode.vm.objects.BootableHashMap;
import org.jnode.vm.objects.VmSystemObject;

/**
 * Registers of the x86 architecture.
 *
 * @author epr
 * @author Levente S\u00e1ntha (lsantha@users.sourceforge.net)
 */
public class X86Register extends VmSystemObject implements X86Constants {

    //Register map, it should stand ahead of any register constant !!!

    private static final BootableHashMap<String, X86Register> registerMap = new BootableHashMap<String, X86Register>();

    /* 8-bit GPR registers */
    public static final GPR8 AL = new GPR8("al", 0);

    public static final GPR8 BL = new GPR8("bl", 3);

    public static final GPR8 CL = new GPR8("cl", 1);

    public static final GPR8 DL = new GPR8("dl", 2);

    public static final GPR8 AH = new GPR8("ah", 4);

    public static final GPR8 BH = new GPR8("bh", 7);

    public static final GPR8 CH = new GPR8("ch", 5);

    public static final GPR8 DH = new GPR8("dh", 6);

    /* 16-bit GPR registers */
    public static final GPR16 AX = new GPR16("ax", 0, true);

    public static final GPR16 BX = new GPR16("bx", 3, true);

    public static final GPR16 CX = new GPR16("cx", 1, true);

    public static final GPR16 DX = new GPR16("dx", 2, true);

    public static final GPR16 SP = new GPR16("sp", 4);

    public static final GPR16 BP = new GPR16("bp", 5);

    public static final GPR16 SI = new GPR16("si", 6);

    public static final GPR16 DI = new GPR16("di", 7);

    /* 32-bit GPR registers */
    public static final GPR32 EAX = new GPR32("eax", 0, true);

    public static final GPR32 EBX = new GPR32("ebx", 3, true);

    public static final GPR32 ECX = new GPR32("ecx", 1, true);

    public static final GPR32 EDX = new GPR32("edx", 2, true);

    public static final GPR32 ESP = new GPR32("esp", 4);

    public static final GPR32 EBP = new GPR32("ebp", 5);

    public static final GPR32 ESI = new GPR32("esi", 6);

    public static final GPR32 EDI = new GPR32("edi", 7);

    /* 64-bit GPR registers */
    public static final GPR64 RAX = new GPR64("rax", 0);

    public static final GPR64 RBX = new GPR64("rbx", 3);

    public static final GPR64 RCX = new GPR64("rcx", 1);

    public static final GPR64 RDX = new GPR64("rdx", 2);

    public static final GPR64 RSP = new GPR64("rsp", 4);

    public static final GPR64 RBP = new GPR64("rbp", 5);

    public static final GPR64 RSI = new GPR64("rsi", 6);

    public static final GPR64 RDI = new GPR64("rdi", 7);

    public static final GPR64 R8 = new GPR64("r8", 8);

    public static final GPR64 R9 = new GPR64("r9", 9);

    public static final GPR64 R10 = new GPR64("r10", 10);

    public static final GPR64 R11 = new GPR64("r11", 11);

    public static final GPR64 R12 = new GPR64("r12", 12);

    public static final GPR64 R13 = new GPR64("r13", 13);

    public static final GPR64 R14 = new GPR64("r14", 14);

    public static final GPR64 R15 = new GPR64("r15", 15);

    public static final GPR32 R8d = new GPR32("r8d", 8, true);

    public static final GPR32 R9d = new GPR32("r9d", 9, true);

    public static final GPR32 R10d = new GPR32("r10d", 10, true);

    public static final GPR32 R11d = new GPR32("r11d", 11, true);

    public static final GPR32 R12d = new GPR32("r12d", 12, true);

    public static final GPR32 R13d = new GPR32("r13d", 13, true);

    public static final GPR32 R14d = new GPR32("r14d", 14, true);

    public static final GPR32 R15d = new GPR32("r15d", 15, true);

    /*Segment registers*/
    public static final SR ES = new SR("es", 0);

    public static final SR CS = new SR("cs", 1);

    public static final SR SS = new SR("ss", 2);

    public static final SR DS = new SR("ds", 3);

    public static final SR FS = new SR("fs", 4);

    public static final SR GS = new SR("gs", 5);

    /*Control registers*/
    public static final CRX CR0 = new CRX("cr0", 0);

    public static final CRX CR2 = new CRX("cr2", 2);

    public static final CRX CR3 = new CRX("cr3", 3);

    public static final CRX CR4 = new CRX("cr4", 4);

    /* Floating-point registers */
    public static final FPU ST0 = new FPU("st0", 0);

    public static final FPU ST1 = new FPU("st1", 1);

    public static final FPU ST2 = new FPU("st2", 2);

    public static final FPU ST3 = new FPU("st3", 3);

    public static final FPU ST4 = new FPU("st4", 4);

    public static final FPU ST5 = new FPU("st5", 5);

    public static final FPU ST6 = new FPU("st6", 6);

    public static final FPU ST7 = new FPU("st7", 7);

    /* MMX registers */
    public static final MMX MM0 = new MMX("mm0", 0);

    public static final MMX MM1 = new MMX("mm1", 1);

    public static final MMX MM2 = new MMX("mm2", 2);

    public static final MMX MM3 = new MMX("mm3", 3);

    public static final MMX MM4 = new MMX("mm4", 4);

    public static final MMX MM5 = new MMX("mm5", 5);

    public static final MMX MM6 = new MMX("mm6", 6);

    public static final MMX MM7 = new MMX("mm7", 7);

    /* SSE registers */
    public static final XMM XMM0 = new XMM("xmm0", 0);

    public static final XMM XMM1 = new XMM("xmm1", 1);

    public static final XMM XMM2 = new XMM("xmm2", 2);

    public static final XMM XMM3 = new XMM("xmm3", 3);

    public static final XMM XMM4 = new XMM("xmm4", 4);

    public static final XMM XMM5 = new XMM("xmm5", 5);

    public static final XMM XMM6 = new XMM("xmm6", 6);

    public static final XMM XMM7 = new XMM("xmm7", 7);

    public static final XMM XMM8 = new XMM("xmm8", 8);
    public static final XMM XMM9 = new XMM("xmm9", 9);
    public static final XMM XMM10 = new XMM("xmm10", 10);
    public static final XMM XMM11 = new XMM("xmm11", 11);
    public static final XMM XMM12 = new XMM("xmm12", 12);
    public static final XMM XMM13 = new XMM("xmm13", 13);
    public static final XMM XMM14 = new XMM("xmm14", 14);
    public static final XMM XMM15 = new XMM("xmm15", 15);

    private final String name;

    private final int nr;

    private final int size;

    private final boolean suitableFor8Bit;

    public X86Register(String name, int size, int nr) {
        this(name, size, nr, false);
    }

    public X86Register(String name, int size, int nr, boolean suitableFor8Bit) {
        this.name = name;
        this.nr = nr;
        this.size = size;
        this.suitableFor8Bit = suitableFor8Bit;
        registerMap.put(name, this);
    }

    /**
     * Returns the name.
     *
     * @return String
     */
    public final String getName() {
        return name;
    }

    /**
     * Returns the nr.
     *
     * @return int
     */
    public final int getNr() {
        return nr;
    }

    /**
     * Returns the size of this register
     *
     * @return int
     * @see X86Constants#BITS8
     * @see X86Constants#BITS16
     * @see X86Constants#BITS32
     * @see X86Constants#BITS64
     * @see X86Constants#BITS80
     */
    public final int getSize() {
        return size;
    }

    public final String toString() {
        return name;
    }

    /**
     * Does this register have an 8-bit part.
     *
     * @return True for EAX, EBX, ECX, EDX, false otherwise.
     */
    public final boolean isSuitableForBits8() {
        return suitableFor8Bit;
    }

    public static GPR getGPR(String name) {
        Object obj = registerMap.get(name);
        if (obj == null) throw new IllegalArgumentException("Unknown register: " + name);
        if (!(obj instanceof GPR)) throw new IllegalArgumentException("Not a GPR: " + name);
        return (GPR) obj;
    }

    public static boolean isGPR(String name) {
        Object obj = registerMap.get(name);
        return (obj != null) && (obj instanceof GPR);
    }

    public static X86Register getRegister(String name) {
        Object obj = registerMap.get(name);
        if (obj == null) throw new IllegalArgumentException("Unknown register: " + name);
        if (!(obj instanceof X86Register)) throw new IllegalArgumentException("Not a X86Register: " + name);
        return (X86Register) obj;
    }

    public static boolean isRegister(String name) {
        Object obj = registerMap.get(name);
        return (obj != null) && (obj instanceof X86Register);
    }

    public abstract static class GPR extends X86Register {

        /**
         * @param name
         * @param size
         * @param nr
         */
        public GPR(String name, int size, int nr) {
            super(name, size, nr);
        }

        /**
         * @param name
         * @param size
         * @param nr
         * @param suitableFor8Bit
         */
        public GPR(String name, int size, int nr, boolean suitableFor8Bit) {
            super(name, size, nr, suitableFor8Bit);
        }
    }

    public static class GPR8 extends GPR {

        /**
         * @param name
         * @param nr
         */
        public GPR8(String name, int nr) {
            super(name, X86Constants.BITS8, nr, true);
        }
    }

    public static class GPR16 extends GPR {

        /**
         * @param name
         * @param nr
         */
        public GPR16(String name, int nr) {
            super(name, X86Constants.BITS16, nr);
        }

        /**
         * @param name
         * @param nr
         * @param suitableFor8Bit
         */
        public GPR16(String name, int nr, boolean suitableFor8Bit) {
            super(name, X86Constants.BITS16, nr, suitableFor8Bit);
        }
    }

    public static class GPR32 extends GPR {

        /**
         * @param name
         * @param nr
         */
        public GPR32(String name, int nr) {
            super(name, X86Constants.BITS32, nr);
        }

        /**
         * @param name
         * @param nr
         * @param suitableFor8Bit
         */
        public GPR32(String name, int nr, boolean suitableFor8Bit) {
            super(name, X86Constants.BITS32, nr, suitableFor8Bit);
        }
    }

    public static final class GPR64 extends GPR {

        /**
         * @param name
         * @param nr
         */
        public GPR64(String name, int nr) {
            super(name, X86Constants.BITS64, nr, true);
        }
    }

    public static final class FPU extends X86Register {

        /**
         * @param name
         * @param nr
         */
        public FPU(String name, int nr) {
            super(name, X86Constants.BITS80, nr);
        }
    }

    public static final class MMX extends X86Register {

        /**
         * @param name
         * @param nr
         */
        public MMX(String name, int nr) {
            super(name, X86Constants.BITS64, nr);
        }
    }

    public static final class XMM extends X86Register {

        /**
         * @param name
         * @param nr
         */
        public XMM(String name, int nr) {
            super(name, X86Constants.BITS128, nr);
        }
    }

    public static final class CRX extends X86Register {
        /**
         * @param name
         * @param nr
         */
        public CRX(String name, int nr) {
            super(name, X86Constants.BITS32, nr, false);
        }
    }

    public static final class SR extends X86Register {
        /**
         * @param name
         * @param nr
         */
        public SR(String name, int nr) {
            super(name, X86Constants.BITS16, nr, false);
        }
    }
}
