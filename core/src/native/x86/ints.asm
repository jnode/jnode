; -----------------------------------------------
; $Id$
;
; Interrupts code
;
; Author       : E.. Prangsma
; -----------------------------------------------

    global Lsetup_idt

%define RESUME_INT		dword[fs:VmX86Processor_RESUME_INT_OFFSET*4]
%define RESUME_INTNO	dword[fs:VmX86Processor_RESUME_INTNO_OFFSET*4]
%define RESUME_ERROR	dword[fs:VmX86Processor_RESUME_ERROR_OFFSET*4]
%define RESUME_HANDLER	dword[fs:VmX86Processor_RESUME_HANDLER_OFFSET*4]
    
int_die_halted:	dd 0    
    
; -------------------------------------
; Stack for inthandler & irqhandler
; -------------------------------------

OLD_SS      equ 76
OLD_ESP     equ 72
OLD_EFLAGS  equ 68
OLD_CS      equ 64
OLD_EIP     equ 60
ERROR       equ 56
INTNO   	equ 52
HANDLER     equ 48 
OLD_EAX     equ 44
OLD_ECX     equ 40
OLD_EDX     equ 36
OLD_EBX     equ 32
;OLD_ESP     equ 28
OLD_EBP     equ 24
OLD_ESI     equ 20
OLD_EDI     equ 16
OLD_DS      equ 12
OLD_ES      equ 8
OLD_FS      equ 4
OLD_GS      equ 0

%macro int_entry 0
    pusha 
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
	test dword [int_die_halted],0xffffffff
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
    popa
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
	cmp dword [ebp+OLD_CS],USER_CS
	jne kernel_panic
	mov ebx,[esp+HANDLER]
	call ebx
	test dword RESUME_INT,0xFFFFFFFF
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
	mov dword RESUME_INT,0
	jmp irqhandler_resume
inthandler_ret:
    int_exit
    
kernel_panic:
	mov eax,kernel_panic_msg
	call sys_print_str
	jmp int_die
	
kernel_irq_panic:
	mov eax,kernel_irq_panic_msg
	call sys_print_str
	jmp int_die
	
kernel_panic_msg: db 'Kernel panic!',0xd,0xa,0
kernel_irq_panic_msg: db 'Kernel panic in IRQ!',0xd,0xa,0

; -----------------------------------------------
; Unhandled interrupt. Die
; -----------------------------------------------
; EAX contains a reference to the register structure

int_die:
	cli
	mov ebx,ebp
	call sys_print_intregs
	;ret
int_die_halt:
	cli
	mov dword [int_die_halted],1
	mov eax,int_die_halt_msg
	call sys_print_str
	hlt

int_die_halt_msg: db 'Real panic: int_die_halt!',0

%macro idm_print_reg 2
	push eax
	mov eax,idm_%1
	call sys_print_str
	pop eax
	mov eax,%2
	call sys_print_eax
%endmacro

%macro idm_print_byte 2
	push eax
	mov eax,idm_%1
	call sys_print_str
	pop eax
	movzx eax,byte %2
	call sys_print_al
%endmacro

sys_print_intregs:
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
	mov eax,irq_resume_overrun_msg
	call sys_print_str
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

int_noerror int_div, 0			; Division by zero
int_noerror int_debug, 1		; Debug
int_noerror int_nmi, 2			; Non maskable Interrupt
int_noerror int_bp, 3			; Breakpoint
int_noerror int_of, 4			; Overflow
int_noerror int_bc, 5			; Bounds check
int_noerror int_inv_oc, 6		; Invalid opcode
int_noerror int_dev_na, 7		; Device not available
int_error   int_df, 8			; Double fault
int_noerror int_copro_or, 9		; Coprocessor overrun
int_error   int_inv_tss, 10		; Invalid TSS
int_error   int_snp, 11			; Segment not present
int_error   int_sf, 12			; Stack fault
int_error   int_gpf, 13			; General protection fault
int_error   int_pf, 14			; Page fault
int_noerror int_copro_err, 16	; Coprocessor error
int_error int_alignment, 17		; Alignment error
int_noerror int_mce, 18			; Machine check exception
int_noerror int_xf, 19			; SIMD floating point exception
int_noerror int_stack_overflow,0x31	; Stack overflow trap

; IRQ handler code
; Parameters <irq no> <handler>
%macro int_irq 2
stub_irq%1:
	push dword 0  ; error code
	push dword %1 ; irq no
	push dword %2 ; handler
	jmp irqhandler
%endmacro

int_irq 0, timer_handler
int_irq 1, def_irq_handler
int_irq 2, def_irq_handler
int_irq 3, def_irq_handler
int_irq 4, def_irq_handler
int_irq 5, def_irq_handler
int_irq 6, def_irq_handler
int_irq 7, def_irq_handler
int_irq 8, def_irq_handler
int_irq 9, def_irq_handler
int_irq 10, def_irq_handler
int_irq 11, def_irq_handler
int_irq 12, def_irq_handler
int_irq 13, def_irq_handler
int_irq 14, def_irq_handler
int_irq 15, def_irq_handler

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

idt:
	dw idtend-idtstart
	dd idtstart

idtstart:
	times (0x40)*8 db 0
idtend:

Lsetup_idt:
    ; First disable NMI
	in al,0x70
	or al,0x80
	out 0x70,al

	intport 0, int_div			; Division by 0
	intport 1, int_debug	    ; Debug exception
	intport 2, int_nmi			; NMI
	intport 3, int_bp, 3		; Breakpoint
	intport 4, int_of			; Overflow
	intport 5, int_bc, 3		; Bounds check
	intport 6, int_inv_oc		; Invalid opcode
	intport 7, int_dev_na		; Device not available
	intport 8, int_df			; Double fault
	intport 9, int_copro_or		; Coprocessor overrun
	intport 10, int_inv_tss		; Invalid TSS
	intport 11, int_snp			; Segment not present
	intport 12, int_sf			; Stack exception
	intport 13, int_gpf			; General protected fault
	intport 14, int_pf			; Page fault
	intport 16, int_copro_err	; Coprocessor error
	intport 17, int_alignment	; Alignment error
	intport 18, int_mce			; Machine check exception
	intport 19, int_xf			; SIMD floating point exception

	intport 0x20, irq0
	intport 0x21, irq1
	intport 0x22, irq2
	intport 0x23, irq3
	intport 0x24, irq4
	intport 0x25, irq5
	intport 0x26, irq6
	intport 0x27, irq7
	intport 0x28, irq8
	intport 0x29, irq9
	intport 0x2A, irq10
	intport 0x2B, irq11
	intport 0x2C, irq12
	intport 0x2D, irq13
	intport 0x2E, irq14
	intport 0x2F, irq15
	
	intport 0x30, yieldPointHandler, 3
	intport 0x31, int_stack_overflow, 3
	intport 0x32, syscallHandler, 3

	lidt [idt]

; Now we have to reprogram the interrupts :-(
; we put them right after the intel-reserved hardware interrupts, at
; int 0x20-0x2F. There they won't mess up anything. Sadly IBM really
; messed this up with the original PC, and they haven't been able to
; rectify it afterwards. Thus the bios puts interrupts at 0x08-0x0f,
; which is used for the internal hardware interrupts as well. We just
; have to reprogram the 8259's, and it isn't fun.

	mov	al,0x11		; initialization sequence
	out	0x20,al		; send it to 8259A-1
	call	delay
	mov	al,0x20		; start of hardware int's (0x20)
	out	0x21,al
	call	delay
	mov	al,0x04		; 8259-1 is master
	out	0x21,al
	call	delay
	mov	al,0x01		; 8086 mode for both
	out	0x21,al
	call	delay

	mov	al,0x11		; initialization sequence
	out	0xA0,al		; and to 8259A-2
	call	delay
	mov	al,0x28		; start of hardware int's 2 (0x28)
	out	0xA1,al
	call	delay
	mov	al,0x02		; 8259-2 is slave
	out	0xA1,al
	call	delay
	mov	al,0x01		; 8086 mode for both
	out	0xA1,al
	call	delay

; Reprogram timer 0 to 1000 times/second
; Set timer count to 1.193.200/1.000 = 1193,2
	mov eax,0x36	; Command for 16-bit, mode 3, binary operation
	out 0x43,al
	call delay
	mov eax,1193
	out 0x40,al		; Load timer 0 count LSB
	call delay
	mov al,ah
	out 0x40,al		; Load timer 0 count MSB
	call delay

; Enable NMI
	in al,0x70
	and al,0x7F
	out 0x70,al

; Enable IRQ's
	xor eax,eax
	out 0x21,al
	out 0xA1,al
	
	ret

;
; Delay is needed after doing i/o
;
delay:
	jmp d1
d1:
	jmp d2
d2:
	ret

; -----------------------------------------------
; Specific interrupt handlers
; -----------------------------------------------

; ---------------------------
; Debug exception
; ---------------------------
int_debug:
	jmp int_die
    mov ebp,eax
    mov eax,dbg_msg1
    call sys_print_str
	mov eax,[ebp+OLD_EIP]
	call sys_print_eax
	mov eax,[ebp+OLD_EAX]
	call sys_print_eax
	mov eax,[ebp+OLD_EBX]
	call sys_print_eax
	mov eax,[ebp+OLD_ECX]
	call sys_print_eax
	mov eax,[ebp+OLD_EDX]
	call sys_print_eax
	mov eax,[ebp+OLD_ESP]
	call sys_print_eax
    mov eax,dbg_msg2
    call sys_print_str
	ret

dbg_msg1: db 'debug eip,eabcdx,sp=',0
dbg_msg2: db 0xd,0xa,0


; ---------------------------
; Breakpoint
; ---------------------------
int_bp:
    mov ebp,eax
    mov eax,bp_msg1
    call sys_print_str
	mov eax,[ebp+OLD_EIP]
	call sys_print_eax
	mov eax,[ebp+OLD_EAX]
	call sys_print_eax
    mov eax,bp_msg2
    call sys_print_str
    call sys_print_intregs
	ret

bp_msg1: db 'breakpoint eip,eax=',0
bp_msg2: db 0xd,0xa,0

; ---------------------------
; General protected fault
; ---------------------------
int_gpf:
	mov eax,[ebp+OLD_EIP]
	cmp byte [eax],0xf4 ; Get the instruction that caused the GPF
	jne int_gpf_2
	; A hlt was called, do a hlt
int_gpf_hlt:
	cmp eax,_halt
	je int_gpf_halt
	inc dword [ebp+OLD_EIP] ; Return past the hlt instruction
	test dword [ebp+OLD_EFLAGS],F_IF
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
	cmp dword [ebp+OLD_CS],USER_CS
	jne int_pf_kernel
	mov eax,cr2
	test eax,0xFFFFF000 ; Error in first (null) page?
	jz int_pf_npe
	neg eax
	test eax,0xFFFFF000 ; Error in last (null) page?
	jz int_pf_npe
	mov eax,SoftByteCodes_EX_PAGEFAULT
	mov ebx,cr2
	;jmp int_die
	call int_system_exception
	ret
int_pf_npe:
	;jmp int_die
	mov eax,SoftByteCodes_EX_NULLPOINTER
	mov ebx,[ebp+OLD_EIP]
	call int_system_exception
	ret
	
int_pf_kernel:
	jmp int_die
	
; ---------------------------
; Double fault
; ---------------------------
int_df:
	cli
	mov eax,int_df_msg
	call sys_print_eax
	hlt
	
int_df_msg: db 'Real panic: Double fault! Halting...',0

; ---------------------------
; Division by 0
; ---------------------------
int_div:
	cmp dword [ebp+OLD_CS],USER_CS
	jne int_die
	mov eax,SoftByteCodes_EX_DIV0
	mov ebx,[ebp+OLD_EIP]
	call int_system_exception
	ret

; ---------------------------
; Bounds check
; ---------------------------
int_bc:
	cmp dword [ebp+OLD_CS],USER_CS
	jne int_die
	mov eax,SoftByteCodes_EX_INDEXOUTOFBOUNDS
	mov ebx,[ebp+OLD_EIP]
	; Determine index register number
	movzx ebx,byte [ebx+1]		; ModR/M
	shr ebx,3					; ModR/M.reg
	and ebx,0x07
	; Determine index
	neg ebx						; -(ModR/M.reg)
	mov ebx,[ebp+OLD_EAX+ebx*4]	; Gets reg value
	; Now throw the exception
	call int_system_exception
	ret

; ---------------------------
; Coprocessor overrun
; ---------------------------
int_copro_or:
	cmp dword [ebp+OLD_CS],USER_CS
	jne int_die
	mov eax,SoftByteCodes_EX_COPRO_OR
	mov ebx,[ebp+OLD_EIP]
	call int_system_exception
	ret

; ---------------------------
; Coprocessor error
; ---------------------------
int_copro_err:
	cmp dword [ebp+OLD_CS],USER_CS
	jne int_die
	mov eax,SoftByteCodes_EX_COPRO_ERR
	mov ebx,[ebp+OLD_EIP]
	call int_system_exception
	ret

; ---------------------------
; NMI
; ---------------------------
int_nmi:
; Overflow
int_of:
; Invalid opcode
int_inv_oc:
; Invalid TSS
int_inv_tss:
; Segment not present
int_snp:
; Stack exception
int_sf:
	jmp int_die	

; ---------------------------
; Alignment error
; ---------------------------
int_alignment:
	jmp int_die
	
; ---------------------------
; Machine check exception
; ---------------------------
int_mce:
	jmp int_die
	
; ---------------------------
; SIMD floating point exception
; ---------------------------
int_xf:
	jmp int_die
	
	