/*
 * $Id$
 */
package org.jnode.vm.x86.compiler;

import org.jnode.assembler.x86.Register;
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
	public static final Register T0 = Register.EAX;

	/** 
	 * Volatile register 1
	 * Do not change this constant!
	 */
	public static final Register T1 = Register.EDX;
	
	/** 
	 * Scratch register 0
	 */
	public static final Register S0 = Register.ECX;
	
	/** 
	 * Scratch register 1
	 */
	public static final Register S1 = Register.EBX;
	
	/** 
	 * Stack pointer register
	 * Do not change this constant!
	 */
	public static final Register SP = Register.ESP;
	
	/** 
	 * Frame pointer register
	 * Do not change this constant!
	 */
	public static final Register FP = Register.EBP;

	/** 
	 * Statics table register
	 * Do not change this constant!
	 */
	public static final Register STATICS = Register.EDI;

	/** EAX register */
	public static final Register EAX = Register.EAX;

	/** ECX register */
	public static final Register ECX = Register.ECX;

	/** EDX register */
	public static final Register EDX = Register.EDX;

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
