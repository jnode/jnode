; -----------------------------------------------
; $Id$
;
; Java VM method invocation support code
;
; Author       : E. Prangsma
; -----------------------------------------------

	extern vm_findThrowableHandler
	extern VmMethod_Class

vm_invoke_abstract:
	%if TRACE_ABSTRACT
	    PRINT_STR vm_invoke_abstract_msg1
		push eax
	    call sys_print_eax
	    ;mov eax,[eax+VmMember_NAME_OFFSET*4]
	    ;call vm_print_string
	    pop eax
    %endif
	%if FAIL_ON_ABSTRACT
	    int 0x50
    %endif
	push dword SoftByteCodes_EX_ABSTRACTMETHOD ; Exception number
	push eax ; Address
	mov eax,SoftByteCodes_systemException
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


