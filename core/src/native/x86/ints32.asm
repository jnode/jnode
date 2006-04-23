; -----------------------------------------------
; $Id$
;
; 32-bit specific interrupt code
;
; Author       : E.. Prangsma
; -----------------------------------------------

%define RESUME_INT		dword[fs:VmX86Processor_RESUME_INT_OFS]
%define RESUME_INTNO	dword[fs:VmX86Processor_RESUME_INTNO_OFS]
%define RESUME_ERROR	dword[fs:VmX86Processor_RESUME_ERROR_OFS]
%define RESUME_HANDLER	dword[fs:VmX86Processor_RESUME_HANDLER_OFS]
    
int_die_halted:	dd 0    
    
; -------------------------------------
; Stack for inthandler & irqhandler
; -------------------------------------

OLD_SS      equ 72
OLD_ESP     equ 68
OLD_EFLAGS  equ 64
OLD_CS      equ 60
OLD_EIP     equ 56
ERROR       equ 52
INTNO   	equ 48
HANDLER     equ 44 
OLD_EAX     equ 40
OLD_ECX     equ 36
OLD_EDX     equ 32
OLD_EBX     equ 28
OLD_EBP     equ 24
OLD_ESI     equ 20
OLD_EDI     equ 16
OLD_DS      equ 12
OLD_ES      equ 8
OLD_FS      equ 4
OLD_GS      equ 0

%define GET_OLD_CS		dword [ebp+OLD_CS]
%define GET_OLD_EIP		dword [ebp+OLD_EIP]
%define GET_OLD_ESP		dword [ebp+OLD_ESP]
%define GET_OLD_EFLAGS	dword [ebp+OLD_EFLAGS]
%define GET_OLD_EAX		dword [ebp+OLD_EAX]
%define GET_OLD_EBX		dword [ebp+OLD_EBX]
%define GET_OLD_ECX		dword [ebp+OLD_ECX]
%define GET_OLD_EDX		dword [ebp+OLD_EDX]

%macro int_entry 0
	push eax
	push ecx
	push edx
	push ebx
	push ebp
	push esi
	push edi
    push ds
    push es
    push fs
    push gs
    mov eax,KERNEL_DS
    mov ds,ax
    mov es,ax
    mov gs,ax
    ; Do not overwrite FS here, since it will always contain the current processor selector
    %if TRACE_INTERRUPTS
		inc byte [0xb8000+0*2]
	%endif
    cld
	test WORD [int_die_halted],0xffffffff
	jnz near int_die_halt
%endmacro

%macro int_exit 0
    mov eax,esp
    and dword [esp+OLD_EFLAGS],~F_NT ; Clear NT flag
    and byte [gdt_tss+5],~0x02       ; Clear busy bit in TSS descriptor
    pop gs
    pop fs
    pop es
    pop ds
    pop edi
    pop esi
    pop ebp
    pop ebx
    pop edx
    pop ecx
    pop eax
    add esp,12 ; Remove HANDLER & INTNO & ERRORCODE 
    iret
%endmacro

; -------------------------
; Generic interrupt handler
; -------------------------
inthandler:
    int_entry
	mov eax,esp
	mov ebp,esp
	cmp GET_OLD_CS,USER_CS
	jne near kernel_panic
	mov ebx,[esp+HANDLER]
	call ebx
	test RESUME_INT,0xFFFFFFFF
	jz inthandler_ret
	; Resume the interrupt (caused by an an IRQ)
	;jmp int_die
	mov ebp,esp
	mov eax,RESUME_INTNO
	mov [ebp+INTNO],eax
	mov eax,RESUME_ERROR
	mov [ebp+ERROR],eax
	mov eax,RESUME_HANDLER
	mov [ebp+HANDLER],eax
	mov RESUME_INT,0
	jmp irqhandler_resume
inthandler_ret:
    int_exit
    
; -----------------------------------------------
; Unhandled interrupt. Die
; -----------------------------------------------
; EAX contains a reference to the register structure

int_die:
	cli
	mov ebx,ebp
	SPINLOCK_JUMP_IF_LOCKED die_lock, int_die_halt
	SPINLOCK_ENTER die_lock ; This lock is never released, so other CPU's just hold here
	call sys_print_intregs
	;ret
int_die_halt:
	cli
	mov dword [int_die_halted],1
	PRINT_STR int_die_halt_msg
	PRINT_WORD CURRENTPROCESSORID
	hlt

int_die_halt_msg: db 'Real panic: int_die_halt! ',0

%macro idm_print_reg 2
	PRINT_STR idm_%1
	PRINT_WORD %2
%endmacro

%macro idm_print_byte 2
	PRINT_STR idm_%1
	movzx eax,byte %2
	call sys_print_al32
%endmacro

sys_print_intregs:
	idm_print_reg procid, CURRENTPROCESSORID
	idm_print_reg intno, [ebp+INTNO]
	idm_print_reg error, [ebp+ERROR]
	idm_print_reg cr2, cr2
	idm_print_reg cr3, cr3
	idm_print_reg eip, [ebp+OLD_EIP]
	idm_print_reg cs,  [ebp+OLD_CS]
	idm_print_reg eflags, [ebp+OLD_EFLAGS]
	idm_print_reg cr0, cr0
	idm_print_reg eax, [ebp+OLD_EAX]
	idm_print_reg ebx, [ebp+OLD_EBX]
	idm_print_reg ecx, [ebp+OLD_ECX]
	idm_print_reg edx, [ebp+OLD_EDX]
	idm_print_reg ebp, [ebp+OLD_EBP]
	idm_print_reg esp, [ebp+OLD_ESP]
	idm_print_reg edi, [ebp+OLD_EDI]
	idm_print_reg esi, [ebp+OLD_ESI]
	idm_print_reg ds,  [ebp+OLD_DS]
	idm_print_reg es,  [ebp+OLD_ES]
	idm_print_reg fs,  [ebp+OLD_FS]
	idm_print_reg gs,  [ebp+OLD_GS]
	mov ebx,[ebp+OLD_ESP]
	idm_print_reg stack0,  [ebx+0]
	idm_print_reg stack1,  [ebx+4]
	idm_print_reg stack1,  [ebx+8]
	idm_print_reg stack1,  [ebx+12]
	idm_print_reg stack1,  [ebx+16]
	idm_print_reg stack1,  [ebx+20]
	mov ebx,[ebp+OLD_EIP]
	; If EIP == CR2, then we print the code as the address of the Top of
	; the stack.
	push eax
	mov eax,cr2
	cmp ebx,eax
	pop eax
	jne sys_print_intregs_code
	mov ebx,[ebp+OLD_ESP]
	mov ebx,[ebx+0]
sys_print_intregs_code:
	sub ebx,16
	idm_print_reg  ipaddr, ebx
	idm_print_byte ip0,	   [ebx+0]
	push ecx
	mov ecx,15
sys_print_intregs_loop1:
	inc ebx
	idm_print_byte ip1, [ebx]
	loop sys_print_intregs_loop1
	
	inc ebx
	idm_print_reg  ipaddr, ebx
	idm_print_byte ip0,	   [ebx+0]
	mov ecx,15
sys_print_intregs_loop2:
	inc ebx
	idm_print_byte ip1, [ebx]
	loop sys_print_intregs_loop2
	pop ecx
	ret

idm_procid: db 0xd,0xa,'proid: ',0
idm_intno:  db 0xd,0xa,'int  : ',0
idm_error:  db        ' Error: ',0
idm_cr2:    db        ' CR2  : ',0
idm_cr3:    db        ' CR3  : ',0
idm_eip:    db 0xd,0xa,'EIP  : ',0
idm_cs:     db        ' CS   : ',0
idm_eflags: db        ' FLAGS: ',0
idm_cr0:	db        ' CR0  : ',0
idm_eax:    db 0xd,0xa,'EAX  : ',0
idm_ebx:    db        ' EBX  : ',0
idm_ecx:    db        ' ECX  : ',0
idm_edx:    db        ' EDX  : ',0
idm_ebp:    db 0xd,0xa,'EBP  : ',0
idm_esp:    db        ' ESP  : ',0
idm_edi:    db        ' EDI  : ',0
idm_esi:    db        ' ESI  : ',0
idm_ds:     db 0xd,0xa,'DS   : ',0
idm_es:     db        ' ES   : ',0
idm_fs:     db        ' FS   : ',0
idm_gs:     db        ' GS   : ',0
idm_stack0: db 0xd,0xa,'STACK: ',0
idm_stack1: db        0
idm_ipaddr: db 0xd,0xa,'CODE(',0
idm_ip0:    db        '): ',0
idm_ip1:    db        0

; -------------------
; Generic IRQ handler
; -------------------

irqhandler:
    int_entry
	mov eax,esp
	mov ebp,esp
	cmp dword [esp+OLD_CS],USER_CS
	jne irqhandler_suspend
irqhandler_resume:
	call dword [esp+HANDLER]
irqhandler_ret:
	%if TRACE_INTERRUPTS
		inc byte [0xb8000+79*2]
	%endif
	int_exit
irqhandler_suspend:
	;jmp int_die
	and dword [esp+OLD_EFLAGS],~F_IF
	mov eax,[esp+INTNO]
	mov RESUME_INTNO,eax
	mov eax,[esp+ERROR]
	mov RESUME_ERROR,eax
	mov eax,[esp+HANDLER]
	mov RESUME_HANDLER,eax
	mov eax,1
	xchg RESUME_INT,eax
	; Test for resume overruns
	test eax,eax
	jz inthandler_ret ; No overrun, finish int handler
	PRINT_STR irq_resume_overrun_msg
	jmp inthandler_ret

irq_suspend_msg: db 'IRQ suspend!',0
irq_resume_overrun_msg: db 'IRQ resume overrun!',0

; Int handler code for interrupts without error code
; Parameters <label> <int no>
%macro int_noerror 2
stub_%1:
	push dword 0   ; error code
	push dword %2  ; int no
	push dword %1  ; handler
	jmp inthandler
%endmacro

; Int handler code for interrupts with error code
; Parameters <label> <int no>
%macro int_error 2
stub_%1:
	push dword %2   ; int no
	push dword %1   ; handler
	jmp inthandler
%endmacro

; IRQ handler code
; Parameters <irq no> <handler>
%macro int_irq 2
stub_irq%1:
	push dword 0  ; error code
	push dword %1 ; irq no
	push dword %2 ; handler
	jmp irqhandler
%endmacro

; intport <index> <offset> [<dpl>]
%macro intport 2-3 0
	mov eax,stub_%2
	mov edi,idtstart+((%1)*8)
	mov ebx,0x8e00 | (%3 << 13)
	call setup_idtentry
%endmacro

; trapport <index> <offset> [<dpl>]
%macro trapport 2-3 0
	mov eax,stub_%2
	mov edi,idtstart+((%1)*8)
	mov ebx,0x8f00 | (%3 << 13)
	call setup_idtentry
%endmacro

; Setup and IDT entry
; Parameters
;   EDI offset of IDT entry
;   EAX offset of handler
;   EBX type of IDT entry
;	0x8e00 = interrupt port (dpl=0)
;	0xee00 = interrupt port (dpl=3)
;	0x8f00 = trap port (dpl=0)
;	0xef00 = trap port (dpl=3)
setup_idtentry:
	mov word [edi+0],ax
	shr eax,16
	mov word [edi+2],KERNEL_CS
	mov word [edi+4],bx
	mov word [edi+6],ax
	ret

; ---------------------------
; Interrupt descr. table
; ---------------------------
idt:
	dw idtend-idtstart
	dd idtstart

idtstart:
	times (0x40)*8 db 0
idtend:

; ---------------------------
; Call int_system_exception
; Args:
;   1) Error nr
;   2) Error address
; ---------------------------
%macro SYSTEM_EXCEPTION 2
	mov eax,%1
	mov ebx,%2
	call int_system_exception
%endmacro

; -----------------------------------------------
; Specific interrupt handlers
; -----------------------------------------------

; ---------------------------
; General protected fault
; ---------------------------
int_gpf:
	mov eax,GET_OLD_EIP
	cmp byte [eax],0xf4 ; Get the instruction that caused the GPF
	jne int_gpf_2
	; A hlt was called, do a hlt
int_gpf_hlt:
	cmp eax,_halt
	je int_gpf_halt
	inc GET_OLD_EIP 			; Return past the hlt instruction
	test GET_OLD_EFLAGS,F_IF
	jz int_gpf_1
	sti
int_gpf_1:
	hlt
	ret
int_gpf_2:
	jmp int_die

int_gpf_halt:
	cli
	hlt
	jmp int_die
	
; ---------------------------
; Page fault
; ---------------------------
int_pf:
	cmp GET_OLD_CS,USER_CS
	jne int_pf_kernel
	mov eax,cr2
	test eax,0xFFFFF000 ; Error in first (null) page?
	jz int_pf_npe
	neg eax
	test eax,0xFFFFF000 ; Error in last (null) page?
	jz int_pf_npe
	SYSTEM_EXCEPTION VmThread_EX_PAGEFAULT, cr2
	ret
int_pf_npe:
	;jmp int_die
	SYSTEM_EXCEPTION VmThread_EX_NULLPOINTER, GET_OLD_EIP
	ret
	
int_pf_kernel:
	jmp int_die
	
