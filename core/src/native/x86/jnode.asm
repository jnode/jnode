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
%include "cmos.h"
%include "syscall.h"
%include "lock.h"
%include "java.inc"

%macro LOOPDIE 0
%%l:
	jmp %%l
%endmacro

%macro FLUSH 0
	jmp %%l
%%l:
%endmacro

%include "kernel.asm"
%include "ints.asm"
%include "mm.asm"
%include "console.asm"
%include "version.asm"
%include "syscall.asm" 

%define VMI_METHOD		dword [ebp+VmX86StackReader_METHOD_OFFSET]
%define VMI_PC			dword [ebp+VmX86StackReader_PC_OFFSET]
%define VMI_MAGIC		dword [ebp+VmX86StackReader_MAGIC_OFFSET]
%define VMI_PREV_FRAME	 [ebp+VmX86StackReader_PREVIOUS_OFFSET]
%define THREADSWITCHINDICATOR	dword[fs:VmProcessor_THREADSWITCHINDICATOR_OFFSET*4]
%define CURRENTPROCESSOR		dword[fs:VmProcessor_ME_OFFSET*4]
%define CURRENTTHREAD			dword[fs:VmProcessor_CURRENTTHREAD_OFFSET*4]
%define NEXTTHREAD				dword[fs:VmProcessor_NEXTTHREAD_OFFSET*4]
%define STACKEND 				dword[fs:VmProcessor_STACKEND_OFFSET*4]
%define VMI_SAVED_REGISTERSPACE	12

; UnConditional yieldpoint from an interpreted method
; Register EBX and ECX are used and are not saved here!
%macro VMI_YIELDPOINT 0
	; Is a switch required?
	mov ecx,VMI_METHOD
	mov ebx,THREADSWITCHINDICATOR
	; Use the mask from the current method
	and ebx,[ecx+VmMethod_THREADSWITCHINDICATORMASK_OFFSET*4]
	cmp ebx,VmProcessor_TSI_SWITCH_REQUESTED
	jne %%noYieldPoint
	int 0x30
%%noYieldPoint:
%endmacro

; UnConditional yieldpoint 
%macro UNCOND_YIELDPOINT 0
	; Is a switch required?
	cmp THREADSWITCHINDICATOR,VmProcessor_TSI_SWITCH_REQUESTED
	jne %%noYieldPoint
	int 0x30
%%noYieldPoint:
%endmacro

; Conditional yieldpoint, only when given register is negative,
; a yieldpoint is triggered.
%macro COND_VMI_YIELDPOINT 1
	test %1,%1
	jns %%done
	VMI_YIELDPOINT
%%done:
%endmacro

	extern SoftByteCodes_allocArray
	extern SoftByteCodes_allocMultiArray
	extern SoftByteCodes_allocObject
	extern SoftByteCodes_allocPrimitiveArray
	extern SoftByteCodes_anewarray
	extern SoftByteCodes_arrayStoreWriteBarrier
	extern SoftByteCodes_resolveField
	extern SoftByteCodes_putfieldWriteBarrier
	extern SoftByteCodes_putstaticWriteBarrier
	extern SoftByteCodes_resolveClass
	extern SoftByteCodes_resolveMethod
	extern SoftByteCodes_unknownOpcode
	extern MathSupport_ldiv
	extern MathSupport_lrem

; Invoke the method in EAX
%macro INVOKE_JAVA_METHOD 0
	call [eax+VmMethod_NATIVECODE_OFFSET*4]
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
SPINLOCK		console_lock
jnodeFinished:	dd 0

		align 4096
	global vm_start
vm_start:

