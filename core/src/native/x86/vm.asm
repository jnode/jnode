; -----------------------------------------------
; $Id$
;
; Java VM support code
;
; Author       : E. Prangsma
; -----------------------------------------------

	extern vm_findThrowableHandler
	extern VmMethod_Class

; -----------------------------------------------
; Throw an exception
; Input
;   eax  Exception to throw
; -----------------------------------------------
vm_athrow:
	%if TRACE_ATHROW
		push eax
		push ebx
		mov ebx,eax
		
		PRINT_STR vm_athrow_msg1
		
		mov eax,[ebx+ObjectLayout_TIB_SLOT*4] ; get TIB of exception
		mov eax,[eax+VmArray_DATA_OFFSET*4] ; get class (vmt[0])
		mov eax,[eax+VmType_NAME_OFS] ; get classname of exception
		call vm_print_string
		
		PRINT_STR vm_athrow_msg2
		
		mov eax,[ebx+Throwable_DETAILMESSAGE_OFS] ; get message of exception
		call vm_print_string

		pop ebx
		pop eax
	%endif
	
vm_athrow_notrace:
	%if TRACE_ATHROW
		push eax
		; Show location of exception
		mov eax,[esp+4]
		call sys_print_eax
		pop eax
	%endif
		
	; Test for unhandled exception
	test ebp,ebp
	jz vm_athrow_unhandled	
	
vm_athrow_notrace_pop_eip:
	pop edx ; return address
	lea edx,[edx-5] ; The call to this method is a 5 byte instruction

	push eax ; save exception
	push edx ; save address
		
	; Setup call to SoftByteCodes.findThrowableHandler
	push eax ; exception
	push ebp ; frame
	push edx ; address
	mov eax,vm_findThrowableHandler
	INVOKE_JAVA_METHOD
	; eax now contains the handler address of the exception, move it to ebx
	mov ebx,eax
	
	pop edx ; restore address
	pop eax ; restore exception
		
vm_athrow_deliver_compiled:
	test ebx,ebx
	jz vm_athrow_notrace_pop_eip	
	; Jump to the compiled exception handler
	jmp ebx
	
vm_athrow_unhandled:
	cli
	PRINT_STR vm_athrow_msg4
	mov ebx,eax
	mov eax,[ebx+ObjectLayout_TIB_SLOT*4]
	mov eax,[eax+VmArray_DATA_OFFSET*4]
	mov eax,[eax+VmType_NAME_OFS]
	call vm_print_string
	mov eax,[ebx+Throwable_DETAILMESSAGE_OFS] ; get message of exception
	call vm_print_string
	cli
	hlt
	ret
	
; -----------------------------------------------
; Print a java.lang.String in EAX
; -----------------------------------------------
vm_print_string:
	push eax
	test eax,eax
	jz vm_print_string_null
	mov eax,[eax] ; String.char[] value -> eax
	call vm_print_chararray
	jmp vm_print_string_ret
	
vm_print_string_null:
	PRINT_STR vm_print_chararray_msg1
vm_print_string_ret:
	pop eax
	ret

; -----------------------------------------------
; Print a char array in who's reference is in EAX
; -----------------------------------------------
vm_print_chararray:
    push eax
    push ecx
    push esi
    
	cmp eax,0
	je vm_print_chararray_null
	mov ecx,[eax+VmArray_LENGTH_OFFSET*4]
	lea esi,[eax+VmArray_DATA_OFFSET*4]
	cld
	
vm_print_chararray_loop:
	test ecx,ecx
	jz vm_print_chararray_ret
	lodsw
	call sys_print_char
	dec ecx
	jmp vm_print_chararray_loop	
	
vm_print_chararray_null:
	PRINT_STR vm_print_chararray_msg1
	
vm_print_chararray_ret:
	pop esi
	pop ecx
	pop eax
	ret
	
; -----------------------------------------------
; Print the stacktrace after an unhandled interrupt
; -----------------------------------------------
vmint_print_stack:
	mov ecx,MAX_STACK_TRACE_LENGTH

vmint_print_stack_loop:
	test ebp,ebp ; Test for bottom of stack
	jz vmint_print_stack_ret
	dec ecx
	jz vmint_print_stack_ret
	
	; Get the method of the current frame in EBX
	mov ebx,[ebp+VmX86StackReader_METHOD_OFFSET] ; Note do not '*4'here, since it is a java constants

	; Print the classname
    mov eax,[ebx+VmMember_DECLARINGCLASS_OFS]
    mov eax,[eax+VmType_NAME_OFS]
    call vm_print_string

    PRINT_STR double_colon_msg

	; Print the methodname	
    mov eax,[ebx+VmMember_NAME_OFS]
    call vm_print_string
    
    ; Println
    PRINT_STR vmint_print_stack_msg1

	; Get the previous frame
	mov ebp,[ebp+VmX86StackReader_PREVIOUS_OFFSET] ; Note do not '*4' here, since the offset is a java static field constant
	jmp vmint_print_stack_loop
	
vmint_print_stack_ret:
	ret
	
; -----------------------------------------------
; C-String constants
; -----------------------------------------------

vm_athrow_msg1: db 'athrow of ',0
vm_athrow_msg2: db ': ',0
vm_athrow_msg3: db 'at ',0
vm_athrow_msg4: db 'Unhandled exception ... halt',0

vm_print_string_msg1: db 'NULL String!',0
vm_print_chararray_msg1: db 'NULL char array!',0

double_colon_msg: db '::',0
eol_msg: db 0xd,0xa,0

vmint_print_stack_msg1: db '   ',0

