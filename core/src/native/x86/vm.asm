; -----------------------------------------------
; $Id$
;
; Java VM support code
;
; Author       : E. Prangsma
; -----------------------------------------------

	extern VmClass_initialize 
	extern VmClass_compile
	extern VmMethod_compile
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
		
		mov eax,vm_athrow_msg1
		call sys_print_str
		
		mov eax,[ebx+ObjectLayout_TIB_SLOT*4] ; get TIB of exception
		mov eax,[eax+VmArray_DATA_OFFSET*4] ; get class (vmt[0])
		mov eax,[eax+VmType_NAME_OFFSET*4] ; get classname of exception
		call vm_print_string
		
		mov eax,vm_athrow_msg2
		call sys_print_str
		
		mov eax,[ebx+Throwable_DETAILMESSAGE_OFFSET*4] ; get message of exception
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

	mov ecx,VMI_MAGIC	
	and ecx,VmStackFrame_MAGIC_MASK
	cmp ecx, VmStackFrame_MAGIC_COMPILED
	je vm_athrow_notrace_pop_eip
	cmp ecx, VmStackFrame_MAGIC_INTERPRETED
	je vm_athrow_notrace_pop_eip
	jmp vm_athrow_unhandled
	
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
	call vm_invoke
	; eax now contains the handler address of the exception, move it to ebx
	mov ebx,eax
	
	pop edx ; restore address
	pop eax ; restore exception
	
	mov ecx,VMI_MAGIC	
	and ecx,VmStackFrame_MAGIC_MASK
	cmp ecx, VmStackFrame_MAGIC_COMPILED
	je vm_athrow_deliver_compiled
	cmp ecx, VmStackFrame_MAGIC_INTERPRETED
	je vm_athrow_deliver_interpreted
	jmp vm_athrow_unknown_magic
	
vm_athrow_deliver_compiled:
	test ebx,ebx
	jz vm_athrow_notrace_pop_eip	
	; Jump to the compiled exception handler
	jmp ebx
	
vm_athrow_deliver_interpreted:
	cmp ebx,VmSystem_RC_HANDLER
	je vm_athrow_deliver_handler_interpreted
	cmp ebx,VmSystem_RC_DEFHANDLER
	je vm_athrow_deliver_default_handler_interpreted
	jmp vm_athrow_unknown_rc

vm_athrow_deliver_handler_interpreted:
	jmp vmi_athrow_handler
	
vm_athrow_deliver_default_handler_interpreted:
	jmp vmi_default_handler
	
vm_athrow_unhandled:
	cli
	push eax
	mov eax,vm_athrow_msg4
	call sys_print_str
	pop eax
	mov ebx,eax
	mov eax,[ebx+ObjectLayout_TIB_SLOT*4]
	mov eax,[eax+VmArray_DATA_OFFSET*4]
	mov eax,[eax+VmType_NAME_OFFSET*4]
	call vm_print_string
	mov eax,[ebx+Throwable_DETAILMESSAGE_OFFSET*4] ; get message of exception
	call vm_print_string
	cli
	hlt
	ret
	
vm_athrow_unknown_magic:
	push eax
	mov eax,vm_athrow_unknown_magic_msg
	call sys_print_str
	mov eax,VMI_MAGIC
	call sys_print_eax
	pop eax
	jmp vm_athrow_unhandled
	
vm_athrow_unknown_rc:
	push eax
	mov eax,vm_athrow_unknown_rc_msg
	call sys_print_str
	mov eax,ebx
	call sys_print_eax
	pop eax
	jmp vm_athrow_unhandled
	
; -----------------------------------------------
; Patch the return location with 'mov eax,<the value that is now in eax>'
; Input
;   EAX the value to let EAX be
;   [ESP+0] return address
;   [ESP+4] Length of patch block
; -----------------------------------------------
vm_patch_MOV_EAX_IMM32:
	push ebx
	mov ebx,esp
	push ecx
	push edi
	cli
	
	; First clear the block with NOP's
	mov ecx,[ebx+8] ; Length of patch block
	mov edi,[ebx+4] ; Return address
	sub edi,ecx
	cld
	push eax
	mov eax,0x90
	rep stosb
	pop eax
	
	; Now create the mov
	mov edi,[ebx+4] ; Return address
	sub edi,[ebx+8] ; Length of patch block
	mov byte [edi+0], 0xb8
	mov dword [edi+1], eax
	mov [ebx+4],edi ; Set return address to start of patch block
	
	sti
	pop edi
	pop ecx
	pop ebx
	
	ret 4

; -----------------------------------------------
; Patch the return location with all NOP's
; Input
;   [ESP+0] return address
;   [ESP+4] Length of patch block
; -----------------------------------------------
vm_patch_NOP:
	push ebx
	mov ebx,esp
	push ecx
	push edi
	cli
	
	; First clear the block with NOP's
	mov ecx,[ebx+8] ; Length of patch block
	mov edi,[ebx+4] ; Return address
	sub edi,ecx
	cld
	push eax
	mov eax,0x90
	rep stosb
	pop eax
	
	mov edi,[ebx+4] ; Return address
	sub edi,[ebx+8] ; Length of patch block
	mov [ebx+4],edi ; Set return address to start of patch block

	sti
	pop edi
	pop ecx
	pop ebx
	
	ret 4

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
	mov eax,vm_print_chararray_msg1
	call sys_print_str
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
	mov eax,vm_print_chararray_msg1
	call sys_print_str
	
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
    mov eax,[ebx+VmMember_DECLARINGCLASS_OFFSET*4]
    mov eax,[eax+VmType_NAME_OFFSET*4]
    call vm_print_string

    mov eax,double_colon_msg
    call sys_print_str

	; Print the methodname	
    mov eax,[ebx+VmMember_NAME_OFFSET*4]
    call vm_print_string
    
    ; Println
    mov eax,vmint_print_stack_msg1
    call sys_print_str

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
vm_athrow_unknown_magic_msg: db 'Unknown magic: ',0
vm_athrow_unknown_rc_msg: db 'Unknown returncode: ',0

vm_print_string_msg1: db 'NULL String!',0
vm_print_chararray_msg1: db 'NULL char array!',0

double_colon_msg: db '::',0
eol_msg: db 0xd,0xa,0

vmint_print_stack_msg1: db '   ',0

