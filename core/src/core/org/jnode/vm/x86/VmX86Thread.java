/*
 * $Id$
 */
package org.jnode.vm.x86;

import org.jnode.vm.Address;
import org.jnode.vm.VmThread;

/**
 * Thread implementation for Intel X86 processor.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class VmX86Thread extends VmThread {

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
}
