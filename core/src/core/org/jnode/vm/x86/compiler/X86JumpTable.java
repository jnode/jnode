/*
 * $Id$
 */
package org.jnode.vm.x86.compiler;

import org.jnode.vm.Address;
import org.jnode.vm.Unsafe;

/**
 * Jumptable constants for the X86 architecture.
 * 
 * The constants is this class must match the structure of the jumptable
 * in vm-jumptable.asm.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class X86JumpTable {

	/** Jumptable offset of vm_patch_MOV_EAX_IMM32 */
	public static final int VM_PATCH_MOV_EAX_IMM32_OFS = 0;
	/** Jumptable offset of vm_patch_NOP */
	public static final int VM_PATCH_NOP_OFS = 4;
	/** Jumptable offset of vm_athrow */
	public static final int VM_ATHROW_OFS = 8;
	/** Jumptable offset of vm_athrow_notrace */
	public static final int VM_ATHROW_NOTRACE_OFS = 12;
	/** Jumptable offset of vm_invoke */
	public static final int VM_INVOKE_OFS = 16;
	/** Jumptable offset of vm_interpreter */
	public static final int VM_INTERPRETER_OFS = 20;
	/** Jumptable offset of vm_invoke_abstract */
	public static final int VM_INVOKE_ABSTRACT_OFS = 24;
	
	/** Label name of the jumptable */
	public static final String JUMPTABLE_NAME = "vm_jumpTable";
	
	/**
	 * Gets a jumptable entry.
	 * This method can only be called at runtime.
	 * @param offset
	 * @return The jumptable entry.
	 * @see #VM_PATCH_MOV_EAX_IMM32_IDX
	 * @see #VM_PATCH_NOP_IDX
	 * @see #VM_ATHROW_IDX
	 * @see #VM_ATHROW_NOTRACE_IDX
	 * @see #VM_INVOKE_IDX
	 * @see #VM_INTERPRETER_IDX
	 */
	public static Address getJumpTableEntry(int offset) {
		return Unsafe.getAddress(Unsafe.getJumpTable(), offset);
	}
}
