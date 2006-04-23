; -----------------------------------------------
; $Id$
;
; 64-bit specific interrupt code
;
; Author       : E.. Prangsma
; -----------------------------------------------

%define RESUME_INT		qword[r12+VmX86Processor_RESUME_INT_OFS]
%define RESUME_INTNO	qword[r12+VmX86Processor_RESUME_INTNO_OFS]
%define RESUME_ERROR	qword[r12+VmX86Processor_RESUME_ERROR_OFS]
%define RESUME_HANDLER	qword[r12+VmX86Processor_RESUME_HANDLER_OFS]
    
int_die_halted:	DA 0    
    
; -------------------------------------
; Stack for inthandler & irqhandler
; -------------------------------------

OLD_SS      equ 176
OLD_ESP     equ 168
OLD_EFLAGS  equ 160
OLD_CS      equ 152
OLD_EIP     equ 144
ERROR       equ 136
INTNO   	equ 128
HANDLER     equ 120 
OLD_EAX     equ 112
OLD_ECX     equ 104
OLD_EDX     equ 96
OLD_EBX     equ 88
OLD_EBP     equ 80
OLD_ESI     equ 72
OLD_EDI     equ 64
OLD_R8		equ 56
OLD_R9		equ 48
OLD_R10		equ 40
OLD_R11		equ 32
OLD_R12		equ 24
OLD_R13		equ 16
OLD_R14		equ 8
OLD_R15		equ 0

%define GET_OLD_CS		qword [rbp+OLD_CS]
%define GET_OLD_EIP		qword [rbp+OLD_EIP]
%define GET_OLD_ESP		qword [rbp+OLD_ESP]
%define GET_OLD_EFLAGS	qword [rbp+OLD_EFLAGS]
%define GET_OLD_EAX		qword [rbp+OLD_EAX]
%define GET_OLD_EBX		qword [rbp+OLD_EBX]
%define GET_OLD_ECX		qword [rbp+OLD_ECX]
%define GET_OLD_EDX		qword [rbp+OLD_EDX]

%macro int_entry 0
	push rax
	push rcx
	push rdx
	push rbx
	push rbp
	push rsi
	push rdi
	push r8
	push r9
	push r10
	push r11
	push r12
	push r13
	push r14
	push r15
    ; Do not overwrite FS here, since it will always contain the current processor selector
    %if TRACE_INTERRUPTS
		inc byte [0xb8000+0*2]
	%endif
    cld
	test WORD [int_die_halted],0xffffffff
	jnz near int_die_halt
%endmacro

%macro int_exit 0
    mov rax,rsp
    and qword [rsp+OLD_EFLAGS],~F_NT ; Clear NT flag
    pop r15
    pop r14
    pop r13
    pop r12
    pop r11
    pop r10
    pop r9
    pop r8
    pop rdi
    pop rsi
    pop rbp
    pop rbx
    pop rdx
    pop rcx
    pop rax
    add rsp,24 ; Remove HANDLER & INTNO & ERRORCODE 
    iretq
%endmacro

; -------------------------
; Generic interrupt handler
; -------------------------
inthandler:
    int_entry
	mov rax,rsp
	mov rbp,rsp
	cmp GET_OLD_CS,USER_CS
	jne near kernel_panic
	mov rbx,[rsp+HANDLER]
	call rbx
	test RESUME_INT,0xFFFFFFFF
	jz inthandler_ret
	; Resume the interrupt (caused by an an IRQ)
	;jmp int_die
	mov rbp,rsp
	mov rax,RESUME_INTNO
	mov [rbp+INTNO],rax
	mov rax,RESUME_ERROR
	mov [rbp+ERROR],rax
	mov rax,RESUME_HANDLER
	mov [rbp+HANDLER],rax
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
	mov rbx,rbp
	SPINLOCK_JUMP_IF_LOCKED die_lock, int_die_halt
	SPINLOCK_ENTER die_lock ; This lock is never released, so other CPU's just hold here
	call sys_print_intregs
	;ret
int_die_halt:
	cli
	mov WORD [int_die_halted],1
	PRINT_STR int_die_halt_msg
	PRINT_INT CURRENTPROCESSORID
	hlt

int_die_halt_msg: db 'Real panic: int_die_halt! ',0

%macro idm_print_reg 2
	PRINT_STR idm_%1
	PRINT_WORD %2
%endmacro

%macro idm_print_byte 2
	PRINT_STR idm_%1
	movzx eax,byte %2
	call sys_print_al64
%endmacro

sys_print_intregs:
	PRINT_STR idm_procid
	PRINT_INT CURRENTPROCESSORID
	idm_print_reg intno, [rbp+INTNO]
	idm_print_reg error, [rbp+ERROR]
	idm_print_reg cr2, cr2
	idm_print_reg cr3, cr3
	idm_print_reg rip, [rbp+OLD_EIP]
	idm_print_reg cs,  [rbp+OLD_CS]
	idm_print_reg eflags, [rbp+OLD_EFLAGS]
	idm_print_reg cr0, cr0
	idm_print_reg rax, [rbp+OLD_EAX]
	idm_print_reg rbx, [rbp+OLD_EBX]
	idm_print_reg rcx, [rbp+OLD_ECX]
	idm_print_reg rdx, [rbp+OLD_EDX]
	idm_print_reg rbp, [rbp+OLD_EBP]
	idm_print_reg rsp, [rbp+OLD_ESP]
	idm_print_reg rdi, [rbp+OLD_EDI]
	idm_print_reg rsi, [rbp+OLD_ESI]
	idm_print_reg r8,  [rbp+OLD_R8]
	idm_print_reg r9,  [rbp+OLD_R9]
	idm_print_reg r10, [rbp+OLD_R10]
	idm_print_reg r11, [rbp+OLD_R11]
	idm_print_reg r12, [rbp+OLD_R12]
	idm_print_reg r13, [rbp+OLD_R13]
	idm_print_reg r14, [rbp+OLD_R14]
	idm_print_reg r15, [rbp+OLD_R15]
	mov ebx,[rbp+OLD_ESP]
	idm_print_reg stack0,  [ebx+0]
	idm_print_reg stack1,  [ebx+8]
	idm_print_reg stack1,  [ebx+16]
	idm_print_reg stack1,  [ebx+24]
	idm_print_reg stack2,  [ebx+32]
	idm_print_reg stack1,  [ebx+40]
	idm_print_reg stack1,  [ebx+48]
	idm_print_reg stack1,  [ebx+56]
	mov rbx,[rbp+OLD_EIP]
	; If EIP == CR2, then we print the code as the address of the Top of
	; the stack.
	push rax
	mov rax,cr2
	cmp rbx,rax
	pop rax
	jne sys_print_intregs_code
	mov rbx,[rbp+OLD_ESP]
	mov rbx,[rbx+0]
sys_print_intregs_code:
	sub rbx,16
	idm_print_reg  ipaddr, rbx
	idm_print_byte ip0,	   [rbx+0]
	push rcx
	mov rcx,15
sys_print_intregs_loop1:
	inc rbx
	idm_print_byte ip1, [rbx]
	loop sys_print_intregs_loop1
	
	inc rbx
	idm_print_reg  ipaddr, rbx
	idm_print_byte ip0,	   [rbx+0]
	mov rcx,15
sys_print_intregs_loop2:
	inc rbx
	idm_print_byte ip1, [rbx]
	loop sys_print_intregs_loop2
	pop rcx
	ret

idm_procid: db 0xd,0xa,'proid: ',0
idm_intno:  db 0xd,0xa,'int  : ',0
idm_error:  db        ' Error: ',0
idm_cr2:    db        ' CR2  : ',0
idm_cr3:    db 0xd,0xa,'CR3  : ',0
idm_rip:    db        ' EIP  : ',0
idm_cs:     db        ' CS   : ',0
idm_eflags: db 0xd,0xa,'FLAGS: ',0
idm_cr0:	db        ' CR0  : ',0
idm_rax:    db        ' RAX  : ',0
idm_rbx:    db 0xd,0xa,'RBX  : ',0
idm_rcx:    db        ' RCX  : ',0
idm_rdx:    db        ' RDX  : ',0
idm_rbp:    db 0xd,0xa,'RBP  : ',0
idm_rsp:    db        ' RSP  : ',0
idm_rdi:    db        ' RDI  : ',0
idm_rsi:    db 0xd,0xa,'RSI  : ',0
idm_r8:     db        ' R8   : ',0
idm_r9:     db        ' R9   : ',0
idm_r10:    db 0xd,0xa,'R10  : ',0
idm_r11:    db        ' R11  : ',0
idm_r12:    db        ' R12  : ',0
idm_r13:    db 0xd,0xa,'R13  : ',0
idm_r14:    db        ' R14  : ',0
idm_r15:    db        ' R15  : ',0
idm_stack0: db 0xd,0xa,'STACK: ',0
idm_stack1: db        0
idm_stack2: db 0xd,0xa,'       ',0
idm_ipaddr: db 0xd,0xa,'CODE(',0
idm_ip0:    db        '): ',0
idm_ip1:    db        0

; -------------------
; Generic IRQ handler
; -------------------

irqhandler:
    int_entry
	mov rax,rsp
	mov rbp,rsp
	cmp GET_OLD_CS,USER_CS
	jne irqhandler_suspend
irqhandler_resume:
	call qword [rsp+HANDLER]
irqhandler_ret:
	%if TRACE_INTERRUPTS
		inc byte [0xb8000+79*2]
	%endif
	int_exit
irqhandler_suspend:
	;jmp int_die
	and qword [rsp+OLD_EFLAGS],~F_IF
	mov rax,[rsp+INTNO]
	mov RESUME_INTNO,rax
	mov rax,[rsp+ERROR]
	mov RESUME_ERROR,rax
	mov rax,[rsp+HANDLER]
	mov RESUME_HANDLER,rax
	mov rax,1
	xchg RESUME_INT,rax
	; Test for resume overruns
	test rax,rax
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
	mov rax,stub_%2
	mov rdi,idtstart+((%1)*16)
	mov ebx,0x8e00 | (%3 << 13)
	call setup_idtentry
%endmacro

; trapport <index> <offset> [<dpl>]
%macro trapport 2-3 0
	mov rax,stub_%2
	mov rdi,idtstart+((%1)*16)
	mov ebx,0x8f00 | (%3 << 13)
	call setup_idtentry
%endmacro

; Setup and IDT entry
; Parameters
;   RDI offset of IDT entry
;   RAX offset of handler
;   EBX type of IDT entry
;	0x8e00 = interrupt port (dpl=0)
;	0xee00 = interrupt port (dpl=3)
;	0x8f00 = trap port (dpl=0)
;	0xef00 = trap port (dpl=3)
setup_idtentry:
	mov word [rdi+0],ax			; Target offset 15-0
	shr rax,16
	mov word [rdi+2],KERNEL_CS	; Target selector
	mov word [rdi+4],bx			; Flags
	mov word [rdi+6],ax			; Target offset 31-16
	shr rax,16
	mov dword [rdi+8],eax		; Target offset 63-32
	ret

; ---------------------------
; Interrupt descr. table
; ---------------------------
idt:
	dw idtend-idtstart
	dq idtstart

idtstart:
	times (0x40)*16 db 0
idtend:

; ---------------------------
; Call int_system_exception
; Args:
;   1) Error nr
;   2) Error address
; ---------------------------
%macro SYSTEM_EXCEPTION 2
	mov rax,%1
	mov rbx,%2
	call int_system_exception
%endmacro

; -----------------------------------------------
; Specific interrupt handlers
; -----------------------------------------------

; ---------------------------
; General protected fault
; ---------------------------
int_gpf:
	mov rax,GET_OLD_EIP
	cmp byte [rax],0xf4 ; Get the instruction that caused the GPF
	jne int_gpf_2
	; A hlt was called, do a hlt
int_gpf_hlt:
	cmp rax,_halt
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
	mov rax,cr2
	test rax,0xFFFFF000 ; Error in first (null) page?
	jz int_pf_npe
	neg rax
	test rax,0xFFFFF000 ; Error in last (null) page?
	jz int_pf_npe
	SYSTEM_EXCEPTION VmThread_EX_PAGEFAULT, cr2
	ret
int_pf_npe:
	SYSTEM_EXCEPTION VmThread_EX_NULLPOINTER, GET_OLD_EIP
	ret
	
int_pf_kernel:
	jmp int_die
	
