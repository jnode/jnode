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
	
%define DEADLOCKCOUNTER	dword[fs:VmX86Processor_DEADLOCKCOUNTER_OFFSET*4]

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
	mov [edi+%1*4],ecx
%endmacro

; Restore a register
; Usage: RESTOREREG VmX86Thread-offset, ebp-offset
%macro RESTOREREG 2
	mov ecx,[edi+%1*4]
	mov [ebp+%2],ecx
%endmacro

yieldPointHandler_kernelCode:
	mov eax,yp_kernel_msg
	call sys_print_str
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
	SAVEREG VmX86Thread_EAX_OFFSET, OLD_EAX
	SAVEREG VmX86Thread_EBX_OFFSET, OLD_EBX
	SAVEREG VmX86Thread_ECX_OFFSET, OLD_ECX
	SAVEREG VmX86Thread_EDX_OFFSET, OLD_EDX
	SAVEREG VmX86Thread_EDI_OFFSET, OLD_EDI
	SAVEREG VmX86Thread_ESI_OFFSET, OLD_ESI
	SAVEREG VmX86Thread_EBP_OFFSET, OLD_EBP
	SAVEREG VmX86Thread_ESP_OFFSET, OLD_ESP
	SAVEREG VmX86Thread_EIP_OFFSET, OLD_EIP
	SAVEREG VmX86Thread_EFLAGS_OFFSET, OLD_EFLAGS
	
	; Save FPU / XMM state
yieldPointHandler_fxSave:
	mov ebx, [edi+VmX86Thread_FXSTATEPTR_OFFSET*4]
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
	jmp yieldPointHandler_fxSave

	; Restore the next thread
yieldPointHandler_restore:
	mov edi,NEXTTHREAD
	RESTOREREG VmX86Thread_EAX_OFFSET, OLD_EAX
	RESTOREREG VmX86Thread_EBX_OFFSET, OLD_EBX
	RESTOREREG VmX86Thread_ECX_OFFSET, OLD_ECX
	RESTOREREG VmX86Thread_EDX_OFFSET, OLD_EDX
	RESTOREREG VmX86Thread_EDI_OFFSET, OLD_EDI
	RESTOREREG VmX86Thread_ESI_OFFSET, OLD_ESI
	RESTOREREG VmX86Thread_EBP_OFFSET, OLD_EBP
	RESTOREREG VmX86Thread_ESP_OFFSET, OLD_ESP
	RESTOREREG VmX86Thread_EIP_OFFSET, OLD_EIP
	RESTOREREG VmX86Thread_EFLAGS_OFFSET, OLD_EFLAGS
	
	; Restore FPU / XMM state
	mov ebx, [edi+VmX86Thread_FXSTATEPTR_OFFSET*4]
	test ebx,ebx
	jz yieldPointHandler_fixStackOverflow		; No valid fxStatePtr yet, do not restore
	test dword [cpu_features],FEAT_FXSR
	jz yieldPointHandler_fpuRestore
	fxrstor [ebx]
	jmp yieldPointHandler_fixOldStackOverflow
yieldPointHandler_fpuRestore:
	frstor [ebx]
	
	; Fix old stack overflows
yieldPointHandler_fixOldStackOverflow:
	mov ecx,[edi+VmThread_STACKOVERFLOW_OFFSET*4]
	test ecx,ecx
	jnz yieldPointHandler_fixStackOverflow
yieldPointHandler_afterStackOverflow:
	; Set the new thread parameters
	mov CURRENTTHREAD,edi
	; Reload stackend
	mov ebx,[edi+VmThread_STACKEND_OFFSET*4]
	mov STACKEND,ebx
yieldPointHandler_done:
	and THREADSWITCHINDICATOR,~VmProcessor_TSI_SWITCH_ACTIVE
	ret

; Fix a previous stack overflow
; EDI contains reference the VmThread
yieldPointHandler_fixStackOverflow:
	; Is the stack overflow resolved?
	mov ecx,[edi+VmThread_STACKEND_OFFSET*4]
	add ecx,VmThread_STACK_OVERFLOW_LIMIT
	; Is current ESP not beyond limit anymore
	cmp dword [edi+VmX86Thread_ESP_OFFSET*4],ecx
	jle yieldPointHandler_afterStackOverflow		; No still below limit
	; Reset stackoverflow flag
	mov [edi+VmThread_STACKEND_OFFSET*4],ecx
	mov dword [edi+VmThread_STACKOVERFLOW_OFFSET*4],0
	jmp yieldPointHandler_afterStackOverflow

; Set the fxStatePtr in the thread given in edi.
; The fxStatePtr must be 16-byte aligned
fixFxStatePtr:
	mov ebx,[edi+VmX86Thread_FXSTATE_OFFSET*4]
	add ebx,(VmArray_DATA_OFFSET*4) + 15
	and ebx,~0xF;
	mov [edi+VmX86Thread_FXSTATEPTR_OFFSET*4],ebx
	ret	
	
; -----------------------------------------------
; Handle a timer interrupt
; -----------------------------------------------
timer_handler:
	mov edi,[fs:VmProcessor_STATICSTABLE_OFFSET*4]
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
	mov eax,deadLock_msg
	call sys_print_str
	jmp int_die
	
; -----------------------------------------------
; Handle an IRQ interrupt
; -----------------------------------------------
def_irq_handler:
	cmp dword [ebp+OLD_CS],USER_CS
	jne def_irq_kernel
	; Increment the appropriate IRQ counter and set threadSwitch indicator.
	mov eax,[ebp+INTNO]
	mov edi,dword [fs:VmX86Processor_IRQCOUNT_OFFSET*4]
	inc dword [edi+(VmArray_DATA_OFFSET*4)+eax*4]
	; Set thread switch indicator
	or THREADSWITCHINDICATOR, VmProcessor_TSI_SWITCH_NEEDED
	; Done
	ret
	
def_irq_kernel:
	mov eax,irq_kernel_msg
	call sys_print_str
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
	SAVEREG VmX86Thread_EXEAX_OFFSET, OLD_EAX
	SAVEREG VmX86Thread_EXEBX_OFFSET, OLD_EBX
	SAVEREG VmX86Thread_EXECX_OFFSET, OLD_ECX
	SAVEREG VmX86Thread_EXEDX_OFFSET, OLD_EDX
	SAVEREG VmX86Thread_EXEDI_OFFSET, OLD_EDI
	SAVEREG VmX86Thread_EXESI_OFFSET, OLD_ESI
	SAVEREG VmX86Thread_EXEBP_OFFSET, OLD_EBP
	SAVEREG VmX86Thread_EXESP_OFFSET, OLD_ESP
	SAVEREG VmX86Thread_EXEIP_OFFSET, OLD_EIP
	SAVEREG VmX86Thread_EXEFLAGS_OFFSET, OLD_EFLAGS
	mov ecx,cr2
	mov [edi+VmX86Thread_EXCR2_OFFSET*4],ecx
	
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
	mov ecx,[eax+VmThread_STACKOVERFLOW_OFFSET*4]
	jecxz int_stack_first_overflow
	jmp doFatal_stack_overflow
		
int_stack_first_overflow:
	inc dword [eax+VmThread_STACKOVERFLOW_OFFSET*4]
	; Remove the stackoverflow limit
	mov edx,[eax+VmThread_STACKEND_OFFSET*4]
	sub edx,VmThread_STACK_OVERFLOW_LIMIT
	mov [eax+VmThread_STACKEND_OFFSET*4],edx
	mov STACKEND,edx
	mov eax,SoftByteCodes_EX_STACKOVERFLOW
	mov dword [ebp+OLD_EIP],doSystemException
	jmp int_system_exception
	
doFatal_stack_overflow:
	mov eax,fatal_so_msg
	call sys_print_str
	;call vmint_print_stack
	jmp int_die
	cli
	hlt
	
yp_kernel_msg:	db 'YieldPoint in kernel mode??? Probably a bug',0
irq_kernel_msg:	db 'IRQ in kernel mode??? Probably a bug',0
fatal_so_msg:		db 'Fatal stack overflow: ',0
deadLock_msg:		db 'Very likely deadlock detected: ',0

