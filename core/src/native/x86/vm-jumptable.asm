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

%ifdef BITS32
	align 4
%endif
%ifdef BITS64
	align 8
%endif
	
vm_jumpTable:
	DA vm_athrow
	DA vm_athrow_notrace
	DA vm_invoke_abstract
	DA vm_invoke_method_after_recompile
	
	
	