; -----------------------------------------------
; $Id$
;
; Java VM interrupt support code
;
; Author       : E. Prangsma
; -----------------------------------------------

	extern VmSystem_currentTimeMillis
	extern VmSystem_initialized
	extern SoftByteCodes_systemException
	extern VmProcessor_reschedule
	
deadLockCounter dd 0
	
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
	mov dword [deadLockCounter], 0
	; Setup the user stack to add a return address to the current EIP
	; and change the current EIP to yieldPointHandler_doReschedule, which will 
	; save the registers and call VmScheduler.reschedule
yieldPointHandler_reschedule:
	; Actually call VmScheduler.reschedule (in kernel mode!)
	push ebp
	xor ebp,ebp					; Make java stacktraces terminate
	mov eax,VmProcessor_reschedule
	push dword vmCurProcessor	; this
	call vm_invoke
	pop ebp
	; Now save the current thread state
	mov edi,CURRENTTHREAD
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
	mov CURRENTTHREAD,edi
yieldPointHandler_done:
	and THREADSWITCHINDICATOR,~VmProcessor_TSI_SWITCH_ACTIVE
	ret
	
; -----------------------------------------------
; Handle a timer interrupt
; -----------------------------------------------
timer_handler:
	inc dword [VmSystem_currentTimeMillis+0]
	adc dword [VmSystem_currentTimeMillis+4],0
	test dword [VmSystem_currentTimeMillis+0],0x07
	jnz timer_ret
	; Set a thread switch needed indicator
	or THREADSWITCHINDICATOR, VmProcessor_TSI_SWITCH_NEEDED
	inc dword [deadLockCounter]
	test dword [deadLockCounter], 0x400
	jnz timer_deadlock
timer_ret:
	mov al,0x60 ; EOI IRQ0
	out 0x20,al
	ret
	ret
	
timer_deadlock:
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
	jmp int_die
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
	call vm_invoke
	jmp vm_athrow
	
; -----------------------------------------------
; Handle a stackoverflow
; -----------------------------------------------
int_stack_overflow:
	mov eax,CURRENTTHREAD
	mov ecx,[eax+VmThread_STACKOVERFLOW_OFFSET*4]
	jecxz int_stack_first_overflow
	jmp doFatal_stack_overflow
		
int_stack_first_overflow:
	inc dword [eax+VmThread_STACKOVERFLOW_OFFSET*4]
	; Remove the stackoverflow limit
	mov ebx,[eax+VmThread_STACK_OFFSET*4]
	sub dword [ebx],VmThread_STACK_OVERFLOW_LIMIT
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

