/*
 * $Id$
 */
package org.jnode.vm.x86.compiler;

import org.jnode.assembler.x86.Register;
import org.jnode.assembler.x86.X86Constants;

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
	 * Stack pointer register
	 * Do not change this constant!
	 */
	public static final Register SP = Register.ESP;
	
	/** 
	 * Frame pointer register
	 * Do not change this constant!
	 */
	public static final Register FP = Register.EBP;

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
	
	/** Interrupt number for yieldpoints */
	public static final int YIELDPOINT_INTNO = 0x30;
}
