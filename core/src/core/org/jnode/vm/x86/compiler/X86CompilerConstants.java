/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.vm.x86.compiler;

import org.jnode.assembler.x86.X86Register;
import org.jnode.assembler.x86.X86Constants;
import org.jnode.vm.VmStackFrame;

/**
 * @author epr
 */
public interface X86CompilerConstants {

	/** 
	 * Volatile register 0
	 * Do not change this constant!
	 */
	public static final X86Register T0 = X86Register.EAX;

	/** 
	 * Volatile register 1
	 * Do not change this constant!
	 */
	public static final X86Register T1 = X86Register.EDX;
	
	/** 
	 * Scratch register 0
	 */
	public static final X86Register S0 = X86Register.ECX;
	
	/** 
	 * Scratch register 1
	 */
	public static final X86Register S1 = X86Register.EBX;
	
	/** 
	 * Stack pointer register
	 * Do not change this constant!
	 */
	public static final X86Register SP = X86Register.ESP;
	
	/** 
	 * Frame pointer register
	 * Do not change this constant!
	 */
	public static final X86Register FP = X86Register.EBP;

	/** 
	 * Statics table register
	 * Do not change this constant!
	 */
	public static final X86Register STATICS = X86Register.EDI;

	/** EAX register */
	public static final X86Register EAX = X86Register.EAX;

	/** ECX register */
	public static final X86Register ECX = X86Register.ECX;

	/** EDX register */
	public static final X86Register EDX = X86Register.EDX;

	/** Size of a byte */
	public static final int BYTESIZE = X86Constants.BITS8;

	/** Size of a word */
	public static final int WORDSIZE = X86Constants.BITS16;

	/** Size of a int */
	public static final int INTSIZE = X86Constants.BITS32;
	
	/** Offset of LSB part of a long entry */
	public static final int LSB = 0;
	
	/** Offset of MSB part of a long entry */
	public static final int MSB = 4;
	
	/** Interrupt number for yieldpoints */
	public static final int YIELDPOINT_INTNO = 0x30;

	/** Interrupt number for abstract method */
	public static final int ABSTRACT_METHOD_INTNO = 0x33;

	/** Magic value for stub compiler */
	public static final int STUB_COMPILER_MAGIC = VmStackFrame.MAGIC_COMPILED | 0x1A;

	/** Magic value for L1 compiler */
	public static final int L1_COMPILER_MAGIC =  VmStackFrame.MAGIC_COMPILED  | 0x2b;

	/** Magic value for L1a compiler */
	public static final int L1A_COMPILER_MAGIC =  VmStackFrame.MAGIC_COMPILED  | 0x3c;

	/** Magic value for l2 compiler */
	public static final int L2_COMPILER_MAGIC = VmStackFrame.MAGIC_COMPILED | 0x8e;
}
