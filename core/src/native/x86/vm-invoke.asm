; -----------------------------------------------
; $Id$
;
; Java VM method invocation support code
;
; Author       : E. Prangsma
; -----------------------------------------------

	extern VmClass_initialize 
	extern VmClass_compile
	extern VmMethod_compile
	extern VmMethod_recordInvoke
	extern vm_findThrowableHandler
	extern VmMethod_Class

; -----------------------------------------------
; Invoke a method 
; Input:
;   EAX Reference to VmMethod object
;   Stack setup for parameters
; Note: Always call to this label, to avoid errors in the stack layout
; Note: All registers except EAX, ECX and EDX are preserved. 
;    FOR NOW THIS IS NOT TRUE, BUT INDEED A GOAL!
; -----------------------------------------------
vm_invoke:

	%if TEST_STACK_OVERFLOW
		mov edx,cs
		cmp edx,USER_CS
		jne vm_invoke_testStackOverflowDone
		mov edx,CURRENTTHREAD
		mov edx,[edx+VmThread_STACK_OFFSET*4]
vm_invoke_testStackOverflow:
		bound esp,[edx]
vm_invoke_testStackOverflowDone:
	%endif

	; Increment invocation count
    inc dword [eax+VmMethod_INVOCATIONCOUNT_OFFSET*4]
	
    %if TRACE_INVOKE
    	; Trace method calls
  		push eax
  		push edx  		
  		mov edx,eax
  		
  		test dword [eax+VmMember_MODIFIERS_OFFSET*4],Modifier_ACC_NATIVE
  		jnz vm_invoke_trace_end
  		
	    mov eax,vm_invoke_msg1
	    call sys_print_str
	    
	    mov eax,[edx+VmMember_DECLARINGCLASS_OFFSET*4]
	    mov eax,[eax+VmType_NAME_OFFSET*4]
	    call vm_print_string
	    
	    mov eax,double_colon_msg
	    call sys_print_str

	    mov eax,[edx+VmMember_NAME_OFFSET*4]
	    call vm_print_string
	    jmp vm_invoke_trace_end
	    
	    mov eax,vm_invoke_msg3
	    call sys_print_str
	    
	    mov eax,[edx+VmMethod_INVOCATIONCOUNT_OFFSET*4]
	    call sys_print_eax

	    mov eax,vm_invoke_msg4
	    call sys_print_str
	    
vm_invoke_trace_end:
	    pop edx
	    pop eax
    %endif

	; ---------------------------------
	; Do several tests on the modifiers
	; The modifiers of the method are
	; stored in EBX. 
	; Make sure to preserve EBX during
	; the tests.
	; ---------------------------------

	; Get the modifiers
    mov edx,[eax+VmMember_MODIFIERS_OFFSET*4]
	
	; Is the method abstract?
    test edx,Modifier_ACC_ABSTRACT
	jnz vm_invoke_abstract ; Abstract, goto exception caller

vm_invoke_nonabstract:

	%if RECORD_INVOKE
		test edx,Modifier_ACC_PROFILE
		jz vm_invoke_nonProfile
		; Call VmMethod.recordInvoke
		push eax
		push edx
		push eax	; Method parameter
		mov eax,VmMethod_recordInvoke
		call vm_invoke
		pop edx
		pop eax
vm_invoke_nonProfile:
	%endif

	%if FORCE_COMPILE_METHODS
		; Is the class we're calling already compiled? 
    	test edx, Modifier_ACC_COMPILED
		jz vm_invoke_compile
	%endif
	
vm_invoke_after_compile:
	; If a non-static method, skip initialization tests
	test edx,Modifier_ACC_STATIC
	jz vm_invoke_after_initialize
	
	; Is the class we're calling already initialized? 
	push edx
    mov edx,[eax+VmMember_DECLARINGCLASS_OFFSET*4]
	test dword [edx+VmType_MODIFIERS_OFFSET*4],Modifier_ACC_INITIALIZED
	pop edx
	jnz vm_invoke_after_initialize
	; Are we calling the <clinit> method?
	test edx,Modifier_ACC_INITIALIZER
	jz vm_invoke_initialize
	
vm_invoke_after_initialize:
    ; Now we can invoke the actual method
    test edx, Modifier_ACC_COMPILED
	jz vm_invoke_interpreter
	jmp [eax+VmMethod_NATIVECODE_OFFSET*4]
	
vm_invoke_interpreter:
	jmp vm_interpreter
	
vm_invoke_abstract:
	%if TRACE_ABSTRACT
		push eax
	    mov eax,vm_invoke_abstract_msg1
	    call sys_print_str
	    pop eax
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
	call vm_invoke
	jmp vm_athrow
	ret

vm_invoke_compile:
	; Compile the class
	push eax
	push edx
	%if TRACE_COMPILE
		push eax
		push edx
		mov edx,eax
		
		mov eax,vm_invoke_compile_msg1
		call sys_print_str
		
    	mov eax,[edx+VmMember_NAME_OFFSET*4]
    	call vm_print_string
    	
		mov eax,vm_invoke_compile_msg2
		call sys_print_str
		
    	pop edx
		pop eax
	%endif
    push eax ; VmMethod
    mov eax,VmMethod_compile
    call vm_invoke
    pop edx
	pop eax
	jmp vm_invoke_after_compile

vm_invoke_initialize:
	; Initialize the class
	push eax
	push edx
	%if TRACE_CLINIT
		push eax
		push edx
		mov edx,eax
		
		mov eax,vm_invoke_init_msg1
		call sys_print_str
		
    	mov edx,[edx+VmMember_VMCLASS_OFFSET*4]
	    mov eax,[edx+VmType_NAME_OFFSET*4]
    	call vm_print_string
    	
    	pop edx
		pop eax
	%endif
    push dword [eax+VmMember_DECLARINGCLASS_OFFSET*4]
    mov eax,VmClass_initialize
    call vm_invoke
    pop edx
	pop eax
	
	%if TRACE_CLINIT
		push eax		
		mov eax,vm_invoke_init_msg2
		call sys_print_str
		pop eax
	%endif
	jmp vm_invoke_after_initialize		
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


