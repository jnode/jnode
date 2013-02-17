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

/**
 * <description>
 *
 * @author epr
 * @author Levente S\u00e1ntha (lsantha@users.sourceforge.net)
 */
public interface X86Constants {

    /* Prefixes */
    /**
     * Operand size prefix
     */
    public static final int OSIZE_PREFIX = 0x66;

    /**
     * Address size prefix
     */
    public static final int ASIZE_PREFIX = 0x67;

    /**
     * FS prefix
     */
    public static final int FS_PREFIX = 0x64;

    /**
     * Lock prefix
     */
    public static final int LOCK_PREFIX = 0xF0;

    /**
     * rep prefix
     */
    public static final int REP_PREFIX = 0xF3;

    /**
     * CRX prefix
     */
    public static final int CRX_PREFIX = 0x0F;

    /**
     * high bit extension for SIB base
     */
    public static final int REX_B_PREFIX = 0x41;

    /**
     * high bit extension for SIB index
     */
    public static final int REX_X_PREFIX = 0x42;

    /**
     * high bit extension for ModR/M reg
     */
    public static final int REX_R_PREFIX = 0x44;

    /**
     * 64-bit operand size
     */
    public static final int REX_W_PREFIX = 0x48;

    /* Jump opcodes (after 0x0f) */
    public static final int JA = 0x87;

    public static final int JAE = 0x83;

    public static final int JB = 0x82;

    public static final int JBE = 0x86;

    public static final int JC = 0x82;

    public static final int JE = 0x84;

    public static final int JZ = 0x84;

    public static final int JG = 0x8f;

    public static final int JGE = 0x8d;

    public static final int JL = 0x8c;

    public static final int JLE = 0x8e;

    public static final int JNA = 0x86;

    public static final int JNAE = 0x82;

    public static final int JNB = 0x83;

    public static final int JNBE = 0x87;

    public static final int JNC = 0x83;

    public static final int JNE = 0x85;

    public static final int JNG = 0x8e;

    public static final int JNGE = 0x8c;

    public static final int JNL = 0x8d;

    public static final int JNLE = 0x8f;

    public static final int JNO = 0x81;

    public static final int JNP = 0x8b;

    public static final int JNS = 0x89;

    public static final int JNZ = 0x85;

    public static final int JO = 0x80;

    public static final int JP = 0x8a;

    /* size, and other attributes, of the operand */
    public static final int BITS8 = 0x00000001;

    public static final int BITS16 = 0x00000002;

    public static final int BITS32 = 0x00000004;

    public static final int BITS64 = 0x00000008; /* FPU only, or 64-bit mode */

    public static final int BITS80 = 0x00000010; /* FPU only */
    public static final int BITS128 = 0x00000020; /* XMM only */

    // Flags
    public static final int F_CF = 0x00000001;
    public static final int F_1 = 0x00000002;
    public static final int F_PF = 0x00000004;
    public static final int F_01 = 0x00000008;
    public static final int F_AF = 0x00000010;
    public static final int F_02 = 0x00000020;
    public static final int F_ZF = 0x00000040;
    public static final int F_SF = 0x00000080;
    public static final int F_TF = 0x00000100;
    public static final int F_IF = 0x00000200;
    public static final int F_DF = 0x00000400;
    public static final int F_OF = 0x00000800;
    public static final int F_IOPL1 = 0x00001000;
    public static final int F_IOPL2 = 0x00002000;
    public static final int F_NT = 0x00004000;  // Nested task
    public static final int F_03 = 0x00008000;
    public static final int F_RF = 0x00010000;  // Resume flag
    public static final int F_VM = 0x00020000;  // Virtual 8086 mode
    public static final int F_AC = 0x00040000;  // Alignment check
    public static final int F_VIF = 0x00080000;  // Virtual interrupt flag
    public static final int F_VIP = 0x00100000;  // Virtual interrupt pending
    public static final int F_ID = 0x00200000;  // ID flag

    public static final class Mode {
        private final int operandSize;

        /**
         * Use 32-bit code
         */
        public static final Mode CODE32 = new Mode(BITS32);

        /**
         * Use 64-bit code
         */
        public static final Mode CODE64 = new Mode(BITS64);

        private Mode(int operandSize) {
            this.operandSize = operandSize;
        }

        public boolean is32() {
            return (operandSize == BITS32);
        }

        public boolean is64() {
            return (operandSize == BITS64);
        }

        /**
         * Gets the size of this mode.
         *
         * @return BITS32 or BITS64
         */
        public int getSize() {
            return operandSize;
        }
    }

}
