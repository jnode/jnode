; -----------------------------------------------
; $Id$
;
; JNode Assembler image
;
; Author       : E. Prangsma
; -----------------------------------------------

%define TRACE_ABSTRACT              1
%define TRACE_ATHROW                0
%define TRACE_INVOKE                0
%define TRACE_UNHANDLED_EXCEPTION	0
%define TRACE_INTERRUPTS			0
    
%define FAIL_ON_ABSTRACT			1	; Should the VM stop on abstract methods (1), or throw an AbstractMethodError (0)

MAX_STACK_TRACE_LENGTH		equ 30	; Maximum methods shown in a low-level stacktrace

bits 32
 
%include "rmconfig.h"
 
%define KERNEL_STACKEND		(kernel_stack + VmThread_STACK_OVERFLOW_LIMIT)
 
	section .text

kernel_begin:
    
%include "i386.h"
%include "cmos.h"
%include "syscall.h"
%include "lock.h"
%include "java.inc"
%include "console.h"

; ----------------------
; JNode specifics
; ----------------------

; Default Page flags
PF_DEFAULT	equ iPF_PRESENT|iPF_WRITE|iPF_USER
PF_DEFAULT_RO	equ iPF_PRESENT|iPF_USER

; Segment selectors
KERNEL_CS   equ 0x08
KERNEL_DS   equ 0x10
USER_CS     equ 0x1B
USER_DS     equ 0x23
TSS_DS      equ 0x28
CURPROC_FS  equ 0x33

%macro LOOPDIE 0
%%l:
	jmp %%l
%endmacro

%macro FLUSH 0
	jmp %%l
%%l:
%endmacro

%include "kernel.asm"
%include "cpu.asm"
%ifdef BITS32
  %include "ints32.asm"
%else  
  %include "ints64.asm"
%endif
%include "ints.asm"
%ifdef BITS32
  %include "mm32.asm"
%endif
%include "console.asm"
%include "version.asm"
%include "syscall.asm" 

%define THREADSWITCHINDICATOR	dword[fs:VmProcessor_THREADSWITCHINDICATOR_OFS]
%define CURRENTPROCESSOR		dword[fs:VmProcessor_ME_OFS]
%define CURRENTTHREAD			dword[fs:VmProcessor_CURRENTTHREAD_OFS]
%define NEXTTHREAD				dword[fs:VmProcessor_NEXTTHREAD_OFS]
%define STACKEND 				dword[fs:VmProcessor_STACKEND_OFS]

; Invoke the method in EAX
%macro INVOKE_JAVA_METHOD 0
	%ifdef BITS32
		call [eax+VmMethod_NATIVECODE_OFS]
	%endif
	%ifdef BITS64
		call [rax+VmMethod_NATIVECODE_OFS]
	%endif
%endmacro

%include "unsafe.asm"
%include "unsafe-binop.asm"
%include "unsafe-setmulti.asm"
%include "unsafe-cpuid.asm"
%include "unsafex86.asm"
%include "vm.asm"
%include "vm-invoke.asm"
%include "vm-ints.asm"
%include "vm-compile.asm"
%include "vm-jumptable.asm"
%include "ap-boot.asm"

		align 4096
kernel_end:

extern Luser_esp

scr_ofs:		DA 0
SPINLOCK		console_lock
jnodeFinished:	DA 0

		align 4096
	global vm_start
vm_start:

