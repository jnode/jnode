; -----------------------------------------------
; $Id$
;
; Java VM interrupt support code
;
; Author       : E. Prangsma
; -----------------------------------------------

	extern VmSystem_initialized
	extern SoftByteCodes_systemException
	extern VmProcessor_reschedule
	extern VmX86Processor_broadcastTimeSliceInterrupt
	global currentTimeMillisStaticsIdx
	
currentTimeMillisStaticsIdx	DA 0
	
; -----------------------------------------------
; Low level Yield Point Handler
; This low level interrupt handler is coded
; by hand for optimal speed.
; -----------------------------------------------

stub_yieldPointHandler:
	push dword 0		; Error code
	push dword 0		; INTNO (not relevant)
	push dword 0		; Handler (not relevant)
	int_entry
	mov ABP,ASP
	call yieldPointHandler
	int_exit
	
; -----------------------------------------------
; Low level Timeslice Handler
; This low level interrupt handler is coded
; by hand for optimal speed.
; -----------------------------------------------

stub_timesliceHandler:
	push dword 0		; Error code
	push dword 0		; INTNO (not relevant)
	push dword 0		; Handler (not relevant)
	int_entry
	mov ABP,ASP
	call timesliceHandler
	int_exit
	
; -----------------------------------------------
; Yield Point Handler
; -----------------------------------------------

; Save a register
; Usage: SAVEREG VmX86Thread-offset, ebp-offset
%macro SAVEREG 2
	mov ACX,[ABP+%2]
	mov [ADI+%1],ACX
%endmacro

; Restore a register
; Usage: RESTOREREG VmX86Thread-offset, ebp-offset
%macro RESTOREREG 2
	mov ACX,[ADI+%1]
	mov [ABP+%2],ACX
%endmacro

; Save an array of MSR's
; Usage: SAVE_MSR_ARRAY array-ref
;   array-ref a reference to an MSR[]. Can be null.
%macro SAVE_MSR_ARRAY 1
	mov ASI,%1
	test ASI,ASI
	jnz %%save			; Arranged this way to optimize branch prediction
	jmp %%end			; Usually there is no restore.
%%save:	
	mov ebx,[ASI+VmArray_LENGTH_OFFSET*SLOT_SIZE]
	test ebx,ebx
	jz %%end
	lea ASI,[ASI+VmArray_DATA_OFFSET*SLOT_SIZE]
%%loop:
	mov ADX,[ASI]
	mov ecx,[ADX+MSR_ID_OFS]
	rdmsr
	mov ACX,[ASI]
	mov [ACX+MSR_VALUE_OFS+0],eax	; Save LSB value
	mov [ACX+MSR_VALUE_OFS+4],edx	; Save MSB value
	; Go to next position
	lea ASI,[ASI+SLOT_SIZE]
	dec ebx
	jnz %%loop		
%%end:	
%endmacro

; Restore an array of MSR's
; Usage: RESTORE_MSR_ARRAY array-ref
;   array-ref a reference to an MSR[]. Can be null.
%macro RESTORE_MSR_ARRAY 1
	mov ASI,%1
	test ASI,ASI
	jnz %%restore		; Arranged this way to optimize branch prediction
	jmp %%end			; Usually there is no restore.
%%restore:	
	mov ebx,[ASI+VmArray_LENGTH_OFFSET*SLOT_SIZE]
	test ebx,ebx
	jz %%end
	lea ASI,[ASI+VmArray_DATA_OFFSET*SLOT_SIZE]
%%loop:
	mov ADX,[ASI]
	mov ecx,[ADX+MSR_ID_OFS]		; Get MSR index
	mov eax,[ADX+MSR_VALUE_OFS+0]	; Get LSB value
	mov edx,[ADX+MSR_VALUE_OFS+4]	; Get MSB value
	wrmsr
	; Go to next position
	lea ASI,[ASI+SLOT_SIZE]
	dec ebx
	jnz %%loop		
%%end:	
%endmacro

yieldPointHandler_kernelCode:
	PRINT_STR yp_kernel_msg
	jmp int_die
	
yieldPointHandler:
	cmp GET_OLD_CS,USER_CS
	jne yieldPointHandler_kernelCode
	; Mark switch active
	or THREADSWITCHINDICATOR,VmProcessor_TSI_SWITCH_ACTIVE
	and THREADSWITCHINDICATOR,~VmProcessor_TSI_SWITCH_NEEDED
	mov DEADLOCKCOUNTER, 0
	; Setup the user stack to add a return address to the current EIP
	; and change the current EIP to yieldPointHandler_doReschedule, which will 
	; save the registers and call VmScheduler.reschedule
yieldPointHandler_reschedule:
	; Save current stackframe (so we can show stacktraces)
	mov ADI,CURRENTTHREAD
	SAVEREG VmX86Thread_EBP_OFS, OLD_EBP

	SAVEREG VmX86Thread_EAX_OFS, OLD_EAX
	SAVEREG VmX86Thread_EBX_OFS, OLD_EBX
	SAVEREG VmX86Thread_ECX_OFS, OLD_ECX
	SAVEREG VmX86Thread_EDX_OFS, OLD_EDX
	SAVEREG VmX86Thread_EDI_OFS, OLD_EDI
	SAVEREG VmX86Thread_ESI_OFS, OLD_ESI
	SAVEREG VmX86Thread_EBP_OFS, OLD_EBP
	SAVEREG VmX86Thread_ESP_OFS, OLD_ESP
	SAVEREG VmX86Thread_EIP_OFS, OLD_EIP
	SAVEREG VmX86Thread_EFLAGS_OFS, OLD_EFLAGS
%ifdef BITS64
	SAVEREG VmX86Thread64_R8_OFS, OLD_R8
	SAVEREG VmX86Thread64_R9_OFS, OLD_R9
	SAVEREG VmX86Thread64_R10_OFS, OLD_R10
	SAVEREG VmX86Thread64_R11_OFS, OLD_R11
	; Skip R12, because it is constant (contains processor)
	SAVEREG VmX86Thread64_R13_OFS, OLD_R13
	SAVEREG VmX86Thread64_R14_OFS, OLD_R14
	SAVEREG VmX86Thread64_R15_OFS, OLD_R15
%endif	

	; Save Read/Write MSR's
yieldPointHandler_SaveMSRs:
	SAVE_MSR_ARRAY [ADI+VmX86Thread_READWRITEMSRS_OFS]
	
	; Save FPU / XMM state
yieldPointHandler_fxSave:
	; Is the FX used since the last thread switch?
	test dword [ADI+VmX86Thread_FXFLAGS_OFS],VmX86Thread_FXF_USED
	jz yieldPointHandler_saveEnd	; No... do not save anything
	; Increment counter
	inc FXSAVECOUNTER
	; Clear FXF_USED flag
	and dword [ADI+VmX86Thread_FXFLAGS_OFS],~VmX86Thread_FXF_USED
	; Load fxStatePtr	
yieldPointHandler_loadFxStatePtr:
	mov ABX, [ADI+VmX86Thread_FXSTATEPTR_OFS]
	test ABX,ABX
	jz near yieldPointHandler_fxSaveInit
	; We have a valid fxState address in ebx
	test dword [cpu_features],FEAT_FXSR
	jz yieldPointHandler_fpuSave
	fxsave [ABX]
	jmp yieldPointHandler_saveEnd
yieldPointHandler_fpuSave:
	fnsave [ABX]
yieldPointHandler_saveEnd:

	; Now call VmScheduler.reschedule (in kernel mode!)
	push ABP
	xor ABP,ABP						; Make java stacktraces terminate
	mov AAX,KERNELSTACKEND
	mov STACKEND,AAX				; Set kernel stack end for correct stackoverflow tests
	mov AAX,VmProcessor_reschedule	; Load reschedule method
	push CURRENTPROCESSOR			; this
	INVOKE_JAVA_METHOD
	pop ABP

	; Restore the next thread
yieldPointHandler_restore:
	mov ADI,NEXTTHREAD
	; Shortcut, if we run the same thread as we used to run, do not restore register state
	cmp ADI,CURRENTTHREAD
	je near yieldPointHandler_done
	; For added safety, test if NEXTHREAD != null
	test ADI,ADI
	jz near yieldPointHandler_done
	RESTOREREG VmX86Thread_EAX_OFS, OLD_EAX
	RESTOREREG VmX86Thread_EBX_OFS, OLD_EBX
	RESTOREREG VmX86Thread_ECX_OFS, OLD_ECX
	RESTOREREG VmX86Thread_EDX_OFS, OLD_EDX
	RESTOREREG VmX86Thread_EDI_OFS, OLD_EDI
	RESTOREREG VmX86Thread_ESI_OFS, OLD_ESI
	RESTOREREG VmX86Thread_EBP_OFS, OLD_EBP
	RESTOREREG VmX86Thread_ESP_OFS, OLD_ESP
	RESTOREREG VmX86Thread_EIP_OFS, OLD_EIP
	RESTOREREG VmX86Thread_EFLAGS_OFS, OLD_EFLAGS
%ifdef BITS64
	RESTOREREG VmX86Thread64_R8_OFS, OLD_R8
	RESTOREREG VmX86Thread64_R9_OFS, OLD_R9
	RESTOREREG VmX86Thread64_R10_OFS, OLD_R10
	RESTOREREG VmX86Thread64_R11_OFS, OLD_R11
	; Skip R12, because it is constant (contains processor)
	RESTOREREG VmX86Thread64_R13_OFS, OLD_R13
	RESTOREREG VmX86Thread64_R14_OFS, OLD_R14
	RESTOREREG VmX86Thread64_R15_OFS, OLD_R15
%endif	
	
	; Restore FPU / XMM state is delayed until actual use
	; We do set the CR0.TS flag.
	mov AAX,cr0
	or AAX,CR0_TS
	mov cr0,AAX
	
	; Restore MSR's
yieldPointHandler_RestoreMSRs:
	RESTORE_MSR_ARRAY [ADI+VmX86Thread_READWRITEMSRS_OFS]
	RESTORE_MSR_ARRAY [ADI+VmX86Thread_WRITEONLYMSRS_OFS]
	
	; Fix old stack overflows
yieldPointHandler_fixOldStackOverflow:
	mov cl,[ADI+VmThread_STACKOVERFLOW_S1_OFS]
	test cl,cl
	jnz yieldPointHandler_fixStackOverflow
yieldPointHandler_afterStackOverflow:
	; Set the new thread parameters
	mov CURRENTTHREAD,ADI
	; Set the isolatedStatics of the new thread
	mov ABX,[ADI+VmThread_ISOLATEDSTATICS_OFS]
	mov ISOLATEDSTATICS,ABX
	; Set the isolatedStaticsTable of the new thread
	mov ABX,[ABX+VmStatics_STATICS_OFS]
	mov ISOLATEDSTATICSTABLE,ABX
	; Reload stackend
	mov ABX,[ADI+VmThread_STACKEND_OFS]
	mov STACKEND,ABX
yieldPointHandler_done:
	and THREADSWITCHINDICATOR,~VmProcessor_TSI_SWITCH_ACTIVE
	ret

; Fix a previous stack overflow
; EDI contains reference the VmThread
yieldPointHandler_fixStackOverflow:
	; Is the stack overflow resolved?
	mov ACX,[ADI+VmThread_STACKEND_OFS]
	add ACX,(VmThread_STACK_OVERFLOW_LIMIT_SLOTS * SLOT_SIZE)
	; Is current ESP not beyond limit anymore
	cmp [ADI+VmX86Thread_ESP_OFS],ACX
	jle yieldPointHandler_afterStackOverflow		; No still below limit
	; Reset stackoverflow flag
	mov [ADI+VmThread_STACKEND_OFS],ACX
	mov byte [ADI+VmThread_STACKOVERFLOW_S1_OFS],0
	jmp yieldPointHandler_afterStackOverflow

; Set the fxStatePtr in the thread given in edi.
; The fxStatePtr must be 16-byte aligned
fixFxStatePtr:
	mov ABX,[ADI+VmX86Thread_FXSTATE_OFS]
	add ABX,(VmArray_DATA_OFFSET*SLOT_SIZE) + 15
	and ABX,~0xF;
	mov [ADI+VmX86Thread_FXSTATEPTR_OFS],ABX
	ret	

yieldPointHandler_fxSaveInit:
	call fixFxStatePtr
	jmp yieldPointHandler_loadFxStatePtr

	
; -----------------------------------------------
; Device not available
; An FPU / MMX / SSE instruction is executed
; while CR0.TS is active.
; Restore the fx state of the current thread
; and clear CR0.TS
; -----------------------------------------------
int_dev_na:
	; Increment counter
	inc DEVICENACOUNTER
	mov ADI,CURRENTTHREAD
	; Mark FX as being used since last thread switch
	or dword [ADI+VmX86Thread_FXFLAGS_OFS],VmX86Thread_FXF_USED;
	; Clear CR0.TS
	clts
	; Restore fx state (if any)
	mov ABX, [ADI+VmX86Thread_FXSTATEPTR_OFS]
	test ABX,ABX
	jz int_dev_na_ret		; No valid fxStatePtr yet, do not restore
	; Increment counter
	inc FXRESTORECOUNTER
	test dword [cpu_features],FEAT_FXSR
	jz int_dev_na_fpuRestore
	fxrstor [ABX]
	ret
int_dev_na_fpuRestore:
	frstor [ABX]
int_dev_na_ret:
	ret


; -----------------------------------------------
; Handle a timer interrupt
; -----------------------------------------------
timer_handler:
	mov ADI,STATICSTABLE
	mov AAX,[currentTimeMillisStaticsIdx]
	lea ADI,[ADI+AAX*4+(VmArray_DATA_OFFSET*SLOT_SIZE)]
%ifdef BITS32	
	add dword [edi+0],1
	adc dword [edi+4],0
	test dword [edi+0],0x07
%else
	add qword [rdi+0],1
	test qword [rdi+0],0x07
%endif	
	jnz timer_ret
	; Set a thread switch needed indicator
	or THREADSWITCHINDICATOR, VmProcessor_TSI_SWITCH_NEEDED
	add DEADLOCKCOUNTER, 1
	test DEADLOCKCOUNTER, 0x4000
	jnz timer_deadlock
	
	; Broadcast timeslice interrupt (if needed)
	test SENDTIMESLICEINTERRUPT, 1
	jz timer_ret
	; Call VmX86Processor.broadcastTimeSliceInterrupt (in kernel mode!)
	push ABP
	xor ABP,ABP						; Make java stacktraces terminate
	mov AAX,KERNELSTACKEND
	mov STACKEND,AAX				; Set kernel stack end for correct stackoverflow tests
	mov AAX,VmX86Processor_broadcastTimeSliceInterrupt ; Load broadcastTimeSliceInterrupt method
	mov byte [0xb8000+79*2],'1'
	push CURRENTPROCESSOR			; this
	INVOKE_JAVA_METHOD
	mov byte [0xb8000+79*2],'2'
	pop ABP
timer_ret:
	mov al,0x60 ; EOI IRQ0
	out 0x20,al
	ret
	
timer_deadlock:
	mov AAX,WORD [jnodeFinished]
	test AAX,AAX
	jnz timer_ret
	PRINT_STR deadLock_msg
	jmp int_die
	
; -----------------------------------------------
; Handle a timeslice interrupt broadcasted by the boot processor
; -----------------------------------------------
timesliceHandler:
	; Set a thread switch needed indicator
	inc byte [0xb8000+78*2]
	or THREADSWITCHINDICATOR, VmProcessor_TSI_SWITCH_NEEDED
	add DEADLOCKCOUNTER, 1
	test DEADLOCKCOUNTER, 0x4000
	jnz timer_deadlock
	; Send EOI to local APIC
	mov AAX,LOCALAPICEOI
	mov dword [AAX],0
	ret
	
; -----------------------------------------------
; Handle an IRQ interrupt
; -----------------------------------------------
def_irq_handler:
	cmp GET_OLD_CS,USER_CS
	jne def_irq_kernel
	; Increment the appropriate IRQ counter and set threadSwitch indicator.
	mov AAX,[ABP+INTNO]
	mov ADI,IRQCOUNT
	inc dword [ADI+AAX*4+(VmArray_DATA_OFFSET*SLOT_SIZE)]
	; Set thread switch indicator
	or THREADSWITCHINDICATOR, VmProcessor_TSI_SWITCH_NEEDED
	; Done
	ret
	
def_irq_kernel:
	PRINT_STR irq_kernel_msg
	ret
	
; -----------------------------------------------
; Throw a system-trapped exception. This method can only be called from an interrupt handler.
; Input: 
; EAX contains exception number.
; EBX Address parameter
; EBP Old register block
; -----------------------------------------------
int_system_exception:
	test THREADSWITCHINDICATOR,VmProcessor_TSI_SYSTEM_READY
	jz near int_die
	;jmp int_die
	; Save the exception state
	mov ADI,CURRENTTHREAD
	SAVEREG VmX86Thread_EXEAX_OFS, OLD_EAX
	SAVEREG VmX86Thread_EXEBX_OFS, OLD_EBX
	SAVEREG VmX86Thread_EXECX_OFS, OLD_ECX
	SAVEREG VmX86Thread_EXEDX_OFS, OLD_EDX
	SAVEREG VmX86Thread_EXEDI_OFS, OLD_EDI
	SAVEREG VmX86Thread_EXESI_OFS, OLD_ESI
	SAVEREG VmX86Thread_EXEBP_OFS, OLD_EBP
	SAVEREG VmX86Thread_EXESP_OFS, OLD_ESP
	SAVEREG VmX86Thread_EXEIP_OFS, OLD_EIP
	SAVEREG VmX86Thread_EXEFLAGS_OFS, OLD_EFLAGS
	mov ACX,cr2
	mov [ADI+VmX86Thread_EXCR2_OFS],ACX
	
	; Setup the user stack to add a return address to the current EIP
	; and change the current EIP to doSystemException, which will 
	; save the registers and call SoftByteCodes.systemException
	mov ADI,[ABP+OLD_ESP]
	lea ADI,[ADI-SLOT_SIZE]
	mov [ABP+OLD_EAX],AAX		; Exception number
	mov [ABP+OLD_EBX],ABX		; Address
	mov AAX,[ABP+OLD_EIP]
	mov [ADI+0],AAX
	mov [ABP+OLD_ESP],ADI
	mov WORD [ABP+OLD_EIP],doSystemException
	ret
	
doSystemException:	
	push AAX ; Exception number
	push ABX ; Address
	mov AAX,SoftByteCodes_systemException
	INVOKE_JAVA_METHOD
	jmp vm_athrow
	
; -----------------------------------------------
; Handle a stackoverflow
; -----------------------------------------------
int_stack_overflow:
	cmp GET_OLD_CS,USER_CS
	jne doFatal_stack_overflow
	mov AAX,CURRENTTHREAD
	mov cl,[AAX+VmThread_STACKOVERFLOW_S1_OFS]
	test cl,cl
	jz int_stack_first_overflow
	jmp doFatal_stack_overflow
		
int_stack_first_overflow:
	inc byte [AAX+VmThread_STACKOVERFLOW_S1_OFS]
	; Remove the stackoverflow limit
	mov ADX,[AAX+VmThread_STACKEND_OFS]
	sub ADX,(VmThread_STACK_OVERFLOW_LIMIT_SLOTS * SLOT_SIZE)
	mov [AAX+VmThread_STACKEND_OFS],ADX
	mov STACKEND,ADX
	mov AAX,VmThread_EX_STACKOVERFLOW
	mov WORD [ABP+OLD_EIP],doSystemException
	jmp int_system_exception
	
doFatal_stack_overflow:
	PRINT_STR fatal_so_msg
	call vmint_print_stack
	jmp int_die
	cli
	hlt
	
yp_kernel_msg:	db 'YieldPoint in kernel mode??? Probably a bug',0
irq_kernel_msg:	db 'IRQ in kernel mode??? Probably a bug',0
fatal_so_msg:		db 'Fatal stack overflow: ',0
deadLock_msg:		db 'Very likely deadlock detected: ',0

