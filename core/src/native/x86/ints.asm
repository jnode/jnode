; -----------------------------------------------
; $Id$
;
; Interrupts code
;
; Author       : E.. Prangsma
; -----------------------------------------------

    global Lsetup_idt
    
kernel_panic:
	PRINT_STR kernel_panic_msg
	jmp int_die
	
kernel_irq_panic:
	PRINT_STR kernel_irq_panic_msg
	jmp int_die
	
kernel_panic_msg: db 'Kernel panic!',0xd,0xa,0
kernel_irq_panic_msg: db 'Kernel panic in IRQ!',0xd,0xa,0

; ---------------------------
; Interrupt handler stubs 
; ---------------------------

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

; ---------------------------
; IRQ handler stubs 
; ---------------------------

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

; ---------------------------
; Setup the interrupt handlers
; ---------------------------
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
	intport 0x33, timesliceHandler, 3

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
    PRINT_STR dbg_msg1
	PRINT_WORD GET_OLD_EIP
	PRINT_WORD GET_OLD_EAX
	PRINT_WORD GET_OLD_EBX
	PRINT_WORD GET_OLD_ECX
	PRINT_WORD GET_OLD_EDX
	PRINT_WORD GET_OLD_ESP
    PRINT_STR dbg_msg2
	ret

dbg_msg1: db 'debug eip,eabcdx,sp=',0
dbg_msg2: db 0xd,0xa,0

; ---------------------------
; Breakpoint
; ---------------------------
int_bp:
    mov ebp,eax
    PRINT_STR bp_msg1
	PRINT_WORD GET_OLD_EIP
	PRINT_WORD GET_OLD_EAX
    PRINT_STR bp_msg2
    call sys_print_intregs
	ret

bp_msg1: db 'breakpoint eip,eax=',0
bp_msg2: db 0xd,0xa,0

; ---------------------------
; Double fault
; ---------------------------
int_df:
	cli
	PRINT_STR int_df_msg
	hlt
	
int_df_msg: db 'Real panic: Double fault! Halting...',0

; ---------------------------
; Division by 0
; ---------------------------
int_div:
	cmp GET_OLD_CS, USER_CS
	jne int_die
	SYSTEM_EXCEPTION VmThread_EX_DIV0, GET_OLD_EIP
	ret

; ---------------------------
; Bounds check
; ---------------------------
int_bc:
	cmp GET_OLD_CS, USER_CS
	jne int_die
	SYSTEM_EXCEPTION VmThread_EX_INDEXOUTOFBOUNDS, GET_OLD_EIP
	ret

; ---------------------------
; Coprocessor overrun
; ---------------------------
int_copro_or:
	cmp GET_OLD_CS, USER_CS
	jne int_die
	SYSTEM_EXCEPTION VmThread_EX_COPRO_OR, GET_OLD_EIP
	ret

; ---------------------------
; Coprocessor error
; ---------------------------
int_copro_err:
	cmp GET_OLD_CS, USER_CS
	jne int_die
	SYSTEM_EXCEPTION VmThread_EX_COPRO_ERR, GET_OLD_EIP
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
	
	