/*
 * $Id$
 */
package org.jnode.vm.x86;

import org.jnode.util.NumberUtils;
import org.jnode.vm.Address;
import org.jnode.vm.VmThread;

/**
 * Thread implementation for Intel X86 processor.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class VmX86Thread extends VmThread {

	// State when not running
	volatile int eax;
	volatile int ebx;
	volatile int ecx;
	volatile int edx;
	volatile int esi;
	volatile int edi;
	volatile int eflags;
	volatile Address eip;
	volatile Address esp;
	volatile Address ebp;
	
	// State upon last system exception
	volatile int exEax;
	volatile int exEbx;
	volatile int exEcx;
	volatile int exEdx;
	volatile int exEsi;
	volatile int exEdi;
	volatile int exEflags;
	volatile int exEip;
	volatile int exEsp;
	volatile int exEbp;
	volatile int exCr2;
	
	/**
	 * 
	 */
	VmX86Thread() {
		super();
	}

	/**
	 * @param javaThread
	 */
	public VmX86Thread(Thread javaThread) {
		super(javaThread);
	}

	
	/**
	 * Gets the most current stackframe of this thread.
	 * @return Stackframe 
	 */
	protected Address getStackFrame() {
		return ebp;
	}
	
	/**
	 * Gets the most current instruction pointer of this thread.
	 * This method is only valid when this thread is not running.
	 * @return IP 
	 */
	protected Address getInstructionPointer() {
		return eip;
	}

	/**
	 * Calculate the end of the stack. 
	 * @param stack
	 * @param stackSize
	 * @return End address of the stack
	 */
	protected Address getStackEnd(Object stack, int stackSize) {
		return Address.add(Address.valueOf(stack), STACK_OVERFLOW_LIMIT);
	}
	
	/**
	 * Gets a human readable representation of the system exception state.
	 * @return String
	 */
	public String getReadableErrorState() {
		return "EAX " +NumberUtils.hex(exEax) +
			" EBX " +NumberUtils.hex(exEbx) +
			" ECX " +NumberUtils.hex(exEcx) +
			" EDX " +NumberUtils.hex(exEdx) +
			" ESI " +NumberUtils.hex(exEsi) +
			" EDI " +NumberUtils.hex(exEdi) +
			" ESP " +NumberUtils.hex(exEsp) +
			" EIP " +NumberUtils.hex(exEip) +
			" CR2 " +NumberUtils.hex(exCr2) +
			" EFLAGS " +NumberUtils.hex(exEflags);
	}
}
