; -----------------------------------------------
; $Id$
;
; JNode Assembler image
;
; Author       : E. Prangsma
; -----------------------------------------------

TRACE_ABSTRACT              equ 1
TRACE_ATHROW                equ 0
TRACE_INVOKE                equ 0
TRACE_UNHANDLED_EXCEPTION	equ 0
TRACE_INTERRUPTS			equ 0
    
PARANOIA					equ 1	; Be very paranoia 
QUICK_INVOKE_OPCODES		equ 1	; Use quick opcodes for invoke opcodes
QUICK_FIELD_OPCODES			equ 1	; Use quick opcodes for field opcodes
TEST_STACK_OVERFLOW			equ 1	; Test for stack overflow
FAIL_ON_ABSTRACT			equ 1	; Should the VM stop on abstract methods (1), or throw an AbstractMethodError (0)
RECORD_INVOKE				equ 1	; Should recordInvoke() be called on method that have ACC_PROFILE set? (default 1)

MAX_STACK_TRACE_LENGTH		equ 30	; Maximum methods shown in a low-level stacktrace

bits 32
 
%include "rmconfig.h"
 
%define KERNEL_STACKEND		(kernel_stack + VmThread_STACK_OVERFLOW_LIMIT)
 
	section .text

;    org 0x100000
kernel_begin:
    
%include "i386.h"
%include "java.inc"

%include "kernel.asm"
%include "ints.asm"
%include "mm.asm"
%include "console.asm"
%include "version.asm"
%include "vmi-main.asm"
%include "vmi-jumptable.asm"
%include "vmi-int.asm"
%include "vmi-long.asm"
%include "vmi-float.asm"
%include "vmi-double.asm"
%include "vmi-object.asm"
%include "vmi-array.asm"
%include "vmi-invoke.asm"
%include "vmi-field.asm"
%include "vmi-jump.asm"
%include "vmi-convert.asm"
%include "vmi-general.asm"
%include "unsafe.asm"
%include "unsafe-binop.asm"
%include "unsafe-setmulti.asm"
%include "unsafe-cpuid.asm"
%include "vm.asm"
%include "vm-invoke.asm"
%include "vm-ints.asm"
%include "vm-jumptable.asm"

		align 4096
kernel_end:

%if 0
	times VmObject_HEADER_SIZE dd 0
Luser_stack:
	; Bound area for stack overflow checking
	dd Luser_stack_low + VmThread_STACK_OVERFLOW_LIMIT
	dd Luser_esp
	; End of bound area
Luser_stack_low:
	times VmThread_DEFAULT_STACK_SIZE db 0
Luser_esp:
	dd 0
%endif

extern Luser_esp

scr_ofs:		dd 0
jnodeFinished:	dd 0

		align 4096
	global vm_start
vm_start:

