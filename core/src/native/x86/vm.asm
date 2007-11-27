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
; This code gets called by the BytecodeVisitor whenever there is an
; athrow bytecode
; Input
;   eax  Exception to throw
; -----------------------------------------------
GLABEL vm_athrow
	%if TRACE_ATHROW
		push AAX
		push ABX
		mov ABX,AAX
		
		PRINT_STR vm_athrow_msg1
		
		mov AAX,[ABX+ObjectLayout_TIB_SLOT*SLOT_SIZE] 	; get TIB of exception
		mov AAX,[AAX+VmArray_DATA_OFFSET*SLOT_SIZE]		; get class (vmt[0])
		mov AAX,[AAX+VmType_NAME_OFS] 					; get classname of exception
		call vm_print_string
		
		PRINT_STR vm_athrow_msg2
		
		mov AAX,[ABX+Throwable_DETAILMESSAGE_OFS] ; get message of exception
		call vm_print_string

		pop ABX
		pop AAX
	%endif
	
GLABEL vm_athrow_notrace
	%if TRACE_ATHROW
		; Show location of exception
		PRINT_WORD [ASP+SLOT_SIZE]
	%endif
		
	; Test for unhandled exception
	test ABP,ABP
	jz vm_athrow_unhandled	
	
vm_athrow_notrace_pop_eip:
	; At this point the top of stack contains the EIP. That is, because
	; we got called by a jumptable-call from ByteCodeVisitor#visit_athrow
	pop ADX				; return address (EIP)
	; The EIP (now aka ADX) points to the end of the "call" instruction,
	; which also means, ADX now points to an assembly instruction that
	; belongs to the bytecode right after the athrow. To get an address
	; that points into an instruction that belongs to the "athrow" bytecode
	; we simply decremen the pointer by one. We use lea instead of sub to
	; not modify and flags. Basically "lea ADX,[ADX-1]" == "sub ADX,1".
	lea ADX,[ADX-1]

	push AAX			; save exception
	push ADX			; save address
		
	; Setup call to SoftByteCodes.findThrowableHandler
	push AAX			; exception
	push ABP			; frame
	push ADX			; address
	mov AAX, vm_findThrowableHandler
	INVOKE_JAVA_METHOD
	; eax now contains the handler address of the exception, move it to ebx
	mov ABX,AAX
	
	pop ADX				; restore address
	pop AAX				; restore exception
		
vm_athrow_deliver_compiled:
	test ABX,ABX
	jz vm_athrow_notrace_pop_eip	
	; Jump to the compiled exception handler
	jmp ABX
	
vm_athrow_unhandled:
	cli
	PRINT_STR vm_athrow_msg4
	mov ABX,AAX
	mov AAX,[ABX+ObjectLayout_TIB_SLOT*SLOT_SIZE]
	mov AAX,[AAX+VmArray_DATA_OFFSET*SLOT_SIZE]
	mov AAX,[AAX+VmType_NAME_OFS]
	call vm_print_string
	mov AAX,[ABX+Throwable_DETAILMESSAGE_OFS] ; get message of exception
	call vm_print_string
	cli
	hlt
	ret
	
; -----------------------------------------------
; Print a java.lang.String in EAX/RAX
; -----------------------------------------------
vm_print_string:
	push AAX
	test AAX,AAX
	jz vm_print_string_null
	mov AAX,[AAX] ; String.char[] value -> eax
	call vm_print_chararray
	jmp vm_print_string_ret
	
vm_print_string_null:
	PRINT_STR vm_print_chararray_msg1
vm_print_string_ret:
	pop AAX
	ret

; -----------------------------------------------
; Print a char array in who's reference is in EAX
; -----------------------------------------------
vm_print_chararray:
	SPINLOCK_ENTER console_lock
    push AAX
    push ACX
    push ASI
    
	test AAX,AAX
	je vm_print_chararray_null
	mov ecx,[AAX+VmArray_LENGTH_OFFSET*SLOT_SIZE]
	lea ASI,[AAX+VmArray_DATA_OFFSET*SLOT_SIZE]
	cld
	
vm_print_chararray_loop:
	test ecx,ecx
	jz vm_print_chararray_ret
	lodsw
	push ASI
%ifdef BITS32	
	call sys_do_print_char32
%else
	call sys_do_print_char64
%endif	
	pop ASI
	dec ecx
	jmp vm_print_chararray_loop	
	
vm_print_chararray_null:
	PRINT_STR vm_print_chararray_msg1
	
vm_print_chararray_ret:
	pop ASI
	pop ACX
	pop AAX
	SPINLOCK_EXIT console_lock
	ret
	
; -----------------------------------------------
; Print the stacktrace after an unhandled interrupt
; -----------------------------------------------
vmint_print_stack:
	mov ecx,MAX_STACK_TRACE_LENGTH

vmint_print_stack_loop:
	test ABP,ABP 					; Test for bottom of stack
	jz vmint_print_stack_ret
	dec ecx
	jz vmint_print_stack_ret
	
	; Get the method id of the current frame in EBX
	PRINT_INT [ABP+VmX86StackReader_METHOD_ID_OFFSET] 

    ; Println
    PRINT_STR vmint_print_stack_msg1

	; Get the previous frame
	mov ABP,[ABP+VmX86StackReader_PREVIOUS_OFFSET] 
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

