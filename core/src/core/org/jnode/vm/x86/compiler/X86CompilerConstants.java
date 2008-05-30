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

package org.jnode.vm.x86.compiler;

import org.jnode.assembler.x86.X86Constants;
import org.jnode.assembler.x86.X86Register;
import org.jnode.vm.VmStackFrame;

/**
 * @author epr
 */
public interface X86CompilerConstants extends X86Constants {

    /**
     * Statics table register in 32-bit mode.
     * Do not change this constant!
     */
    public static final X86Register.GPR32 STATICS32 = X86Register.EDI;

    /**
     * Statics table register in 64-bit mode.
     * Do not change this constant!
     */
    public static final X86Register.GPR64 STATICS64 = X86Register.RDI;

    /**
     * VmProcessor register in 64-bit mode.
     * Do not change this constant!
     */
    public static final X86Register.GPR64 PROCESSOR64 = X86Register.R12;

    /**
     * Size of a byte
     */
    public static final int BYTESIZE = X86Constants.BITS8;

    /**
     * Size of a word
     */
    public static final int WORDSIZE = X86Constants.BITS16;

    /**
     * Size of a int
     */
    public static final int INTSIZE = X86Constants.BITS32;

    /**
     * Offset of LSB part of a long entry
     */
    public static final int LSB = 0;

    /**
     * Offset of MSB part of a long entry
     */
    public static final int MSB = 4;

    /**
     * Interrupt number for yieldpoints
     */
    public static final int YIELDPOINT_INTNO = 0x30;

    /**
     * Interrupt number for abstract method
     */
    public static final int ABSTRACT_METHOD_INTNO = 0x33;

    /**
     * Magic value for stub compiler
     */
    public static final int STUB_COMPILER_MAGIC = VmStackFrame.MAGIC_COMPILED | 0x1A;

    /**
     * Magic value for L1 compiler
     */
    public static final int L1_COMPILER_MAGIC = VmStackFrame.MAGIC_COMPILED | 0x2b;

    /**
     * Magic value for L1a compiler
     */
    public static final int L1A_COMPILER_MAGIC = VmStackFrame.MAGIC_COMPILED | 0x3c;

    /**
     * Magic value for L1b compiler
     */
    public static final int L1B_COMPILER_MAGIC = VmStackFrame.MAGIC_COMPILED | 0x3d;

    /**
     * Magic value for l2 compiler
     */
    public static final int L2_COMPILER_MAGIC = VmStackFrame.MAGIC_COMPILED | 0x8e;
}
