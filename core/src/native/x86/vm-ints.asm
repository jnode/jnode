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
	
%define DEADLOCKCOUNTER		dword[fs:VmX86Processor_DEADLOCKCOUNTER_OFS]
%define DEVICENACOUNTER		dword[fs:VmX86Processor_DEVICENACOUNTER_OFS]
%define FXSAVECOUNTER		dword[fs:VmX86Processor_FXSAVECOUNTER_OFS]
%define FXRESTORECOUNTER	dword[fs:VmX86Processor_FXRESTORECOUNTER_OFS]

;deadLockCounter				dd 0
currentTimeMillisStaticsIdx	dd -1
	
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
	mov ebp,esp
	call yieldPointHandler
	int_exit
	
; -----------------------------------------------
; Yield Point Handler
; -----------------------------------------------

; Save a register
; Usage: SAVEREG VmX86Thread-offset, ebp-offset
%macro SAVEREG 2
	mov ecx,[ebp+%2]
	mov [edi+%1],ecx
%endmacro

; Restore a register
; Usage: RESTOREREG VmX86Thread-offset, ebp-offset
%macro RESTOREREG 2
	mov ecx,[edi+%1]
	mov [ebp+%2],ecx
%endmacro

yieldPointHandler_kernelCode:
	PRINT_STR yp_kernel_msg
	jmp int_die
	
yieldPointHandler:
	cmp dword [ebp+OLD_CS],USER_CS
	jne yieldPointHandler_kernelCode
	; Mark switch active
	or THREADSWITCHINDICATOR,VmProcessor_TSI_SWITCH_ACTIVE
	and THREADSWITCHINDICATOR,~VmProcessor_TSI_SWITCH_NEEDED
	mov DEADLOCKCOUNTER, 0
	; Setup the user stack to add a return address to the current EIP
	; and change the current EIP to yieldPointHandler_doReschedule, which will 
	; save the registers and call VmScheduler.reschedule
yieldPointHandler_reschedule:
	; Actually call VmScheduler.reschedule (in kernel mode!)
	push ebp
	xor ebp,ebp						; Make java stacktraces terminate
	mov STACKEND,KERNEL_STACKEND	; Set kernel stack end for correct stackoverflow tests
	mov eax,VmProcessor_reschedule	; Load reschedule method
	push dword vmCurProcessor		; this
	INVOKE_JAVA_METHOD
	pop ebp
	; Now save the current thread state
	mov edi,CURRENTTHREAD
	cmp edi,NEXTTHREAD
	je near yieldPointHandler_done
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
	
	; Save FPU / XMM state
yieldPointHandler_fxSave:
	; Is the FX used since the last thread switch?
	test dword [edi+VmX86Thread_FXFLAGS_OFS],VmX86Thread_FXF_USED
	jz yieldPointHandler_restore	; No... do not save anything
	; Increment counter
	inc FXSAVECOUNTER
	; Clear FXF_USED flag
	and dword [edi+VmX86Thread_FXFLAGS_OFS],~VmX86Thread_FXF_USED
	; Load fxStatePtr	
yieldPointHandler_loadFxStatePtr:
	mov ebx, [edi+VmX86Thread_FXSTATEPTR_OFS]
	test ebx,ebx
	jz yieldPointHandler_fxSaveInit
	; We have a valid fxState address in ebx
	test dword [cpu_features],FEAT_FXSR
	jz yieldPointHandler_fpuSave
	fxsave [ebx]
	jmp yieldPointHandler_restore
yieldPointHandler_fpuSave:
	fnsave [ebx]
	jmp yieldPointHandler_restore

yieldPointHandler_fxSaveInit:
	call fixFxStatePtr
	jmp yieldPointHandler_loadFxStatePtr

	; Restore the next thread
yieldPointHandler_restore:
	mov edi,NEXTTHREAD
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
	
	; Restore FPU / XMM state is delayed until actual use
	; We do set the CR0.TS flag.
	mov eax,cr0
	or eax,CR0_TS
	mov cr0,eax
	
	; Fix old stack overflows
yieldPointHandler_fixOldStackOverflow:
	mov ecx,[edi+VmThread_STACKOVERFLOW_OFS]
	test ecx,ecx
	jnz yieldPointHandler_fixStackOverflow
yieldPointHandler_afterStackOverflow:
	; Set the new thread parameters
	mov CURRENTTHREAD,edi
	; Reload stackend
	mov ebx,[edi+VmThread_STACKEND_OFS]
	mov STACKEND,ebx
yieldPointHandler_done:
	and THREADSWITCHINDICATOR,~VmProcessor_TSI_SWITCH_ACTIVE
	ret

; Fix a previous stack overflow
; EDI contains reference the VmThread
yieldPointHandler_fixStackOverflow:
	; Is the stack overflow resolved?
	mov ecx,[edi+VmThread_STACKEND_OFS]
	add ecx,VmThread_STACK_OVERFLOW_LIMIT
	; Is current ESP not beyond limit anymore
	cmp dword [edi+VmX86Thread_ESP_OFS],ecx
	jle yieldPointHandler_afterStackOverflow		; No still below limit
	; Reset stackoverflow flag
	mov [edi+VmThread_STACKEND_OFS],ecx
	mov dword [edi+VmThread_STACKOVERFLOW_OFS],0
	jmp yieldPointHandler_afterStackOverflow

; Set the fxStatePtr in the thread given in edi.
; The fxStatePtr must be 16-byte aligned
fixFxStatePtr:
	mov ebx,[edi+VmX86Thread_FXSTATE_OFS]
	add ebx,(VmArray_DATA_OFFSET*4) + 15
	and ebx,~0xF;
	mov [edi+VmX86Thread_FXSTATEPTR_OFS],ebx
	ret	
	
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
	mov edi,CURRENTTHREAD
	; Mark FX as being used since last thread switch
	or dword [edi+VmX86Thread_FXFLAGS_OFS],VmX86Thread_FXF_USED;
	; Clear CR0.TS
	clts
	; Restore fx state (if any)
	mov ebx, [edi+VmX86Thread_FXSTATEPTR_OFS]
	test ebx,ebx
	jz int_dev_na_ret		; No valid fxStatePtr yet, do not restore
	; Increment counter
	inc FXRESTORECOUNTER
	test dword [cpu_features],FEAT_FXSR
	jz int_dev_na_fpuRestore
	fxrstor [ebx]
	ret
int_dev_na_fpuRestore:
	frstor [ebx]
int_dev_na_ret:
	ret


; -----------------------------------------------
; Handle a timer interrupt
; -----------------------------------------------
timer_handler:
	mov edi,[fs:VmProcessor_STATICSTABLE_OFS]
	mov eax,[currentTimeMillisStaticsIdx]
	lea edi,[edi+eax*4+(VmArray_DATA_OFFSET*4)]
	inc dword [edi+0]
	adc dword [edi+4],0
	test dword [edi+0],0x07
	jnz timer_ret
	; Set a thread switch needed indicator
	or THREADSWITCHINDICATOR, VmProcessor_TSI_SWITCH_NEEDED
	inc DEADLOCKCOUNTER
	test DEADLOCKCOUNTER, 0x4000
	jnz timer_deadlock
timer_ret:
	mov al,0x60 ; EOI IRQ0
	out 0x20,al
	ret
	
timer_deadlock:
	mov eax,dword [jnodeFinished]
	test eax,eax
	jnz timer_ret
	PRINT_STR deadLock_msg
	jmp int_die
	
; -----------------------------------------------
; Handle an IRQ interrupt
; -----------------------------------------------
def_irq_handler:
	cmp dword [ebp+OLD_CS],USER_CS
	jne def_irq_kernel
	; Increment the appropriate IRQ counter and set threadSwitch indicator.
	mov eax,[ebp+INTNO]
	mov edi,dword [fs:VmX86Processor_IRQCOUNT_OFS]
	inc dword [edi+(VmArray_DATA_OFFSET*4)+eax*4]
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
	mov edi,CURRENTTHREAD
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
	mov ecx,cr2
	mov [edi+VmX86Thread_EXCR2_OFS],ecx
	
	; Setup the user stack to add a return address to the current EIP
	; and change the current EIP to doSystemException, which will 
	; save the registers and call SoftByteCodes.systemException
	mov edi,[ebp+OLD_ESP]
	lea edi,[edi-4]
	mov [ebp+OLD_EAX],eax ; Exception number
	mov [ebp+OLD_EBX],ebx ; Address
	mov eax,[ebp+OLD_EIP]
	mov [edi+0],eax
	mov [ebp+OLD_ESP],edi
	mov dword [ebp+OLD_EIP],doSystemException
	ret
	
doSystemException:	
	push eax ; Exception number
	push ebx ; Address
	mov eax,SoftByteCodes_systemException
	INVOKE_JAVA_METHOD
	jmp vm_athrow
	
; -----------------------------------------------
; Handle a stackoverflow
; -----------------------------------------------
int_stack_overflow:
	cmp dword [ebp+OLD_CS],USER_CS
	jne doFatal_stack_overflow
	mov eax,CURRENTTHREAD
	mov ecx,[eax+VmThread_STACKOVERFLOW_OFS]
	jecxz int_stack_first_overflow
	jmp doFatal_stack_overflow
		
int_stack_first_overflow:
	inc dword [eax+VmThread_STACKOVERFLOW_OFS]
	; Remove the stackoverflow limit
	mov edx,[eax+VmThread_STACKEND_OFS]
	sub edx,VmThread_STACK_OVERFLOW_LIMIT
	mov [eax+VmThread_STACKEND_OFS],edx
	mov STACKEND,edx
	mov eax,SoftByteCodes_EX_STACKOVERFLOW
	mov dword [ebp+OLD_EIP],doSystemException
	jmp int_system_exception
	
doFatal_stack_overflow:
	PRINT_STR fatal_so_msg
	;call vmint_print_stack
	jmp int_die
	cli
	hlt
	
yp_kernel_msg:	db 'YieldPoint in kernel mode??? Probably a bug',0
irq_kernel_msg:	db 'IRQ in kernel mode??? Probably a bug',0
fatal_so_msg:		db 'Fatal stack overflow: ',0
deadLock_msg:		db 'Very likely deadlock detected: ',0

