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

	align 4
vm_jumpTable:
	dd vm_patch_MOV_EAX_IMM32
	dd vm_patch_NOP
	dd vm_athrow
	dd vm_athrow_notrace
	dd 0
	dd vm_invoke_abstract
	dd vm_invoke_method_after_recompile
	
	