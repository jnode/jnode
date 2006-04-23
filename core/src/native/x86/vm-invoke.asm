; -----------------------------------------------
; $Id$
;
; Java VM method invocation support code
;
; Author       : E. Prangsma
; -----------------------------------------------

	extern vm_findThrowableHandler
	extern VmMethod_Class

GLABEL vm_invoke_abstract
	push dword VmThread_EX_ABSTRACTMETHOD ; Exception number
	push AAX ; Address
	mov AAX,SoftByteCodes_systemException
	INVOKE_JAVA_METHOD
	jmp vm_athrow
	ret

; -----------------------------------------------
; C-String constants
; -----------------------------------------------

vm_invoke_msg1: db '{inv:',0
vm_invoke_msg3: db ' (cnt:',0
vm_invoke_msg4: db ')} ',0

vm_invoke_abstract_msg1: db 'Abstract method called: ',0

vm_invoke_compile_msg1: db '@#@# compile class ',0
vm_invoke_compile_msg2: db ' #@#@ ',0

vm_invoke_init_msg1: db '{clinit:',0
vm_invoke_init_msg2: db '} ',0


