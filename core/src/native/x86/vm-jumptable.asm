; -----------------------------------------------
; $Id$
;
; Native address jumptable.
; This table is used by the native code compiler to call
; native methods, without compromising code moveability.
;
; When changing this file, also change org.jnode.vm.x86.compiler.X86JumpTable
; class.
;
; Author       : E. Prangsma
; -----------------------------------------------

	align SLOT_SIZE
	
GLABEL vm_jumpTable
	DA vm_athrow
	DA vm_athrow_notrace
	DA vm_invoke_abstract
	
	
	