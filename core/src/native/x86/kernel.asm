; -----------------------------------------------
; $Id$
;
; Main kernel startup code
;
; Author       : E. Prangsma
; -----------------------------------------------

    global sys_start

;
; Kernel startup code
;
; Parameters
;   EAX=0x2BADB002 (Multiboot magic)
;   EBX=ref to multiboot structure
;
sys_start:
	jmp real_start

	; MULTI-BOOT HEADER
	align 4
mb_header:
	dd 0x1BADB002				; Magic
	dd 0x00010003				; Feature flags
	dd 0-0x1BADB002-0x00010003	; Checksum
	dd mb_header				; header_addr
	dd sys_start				; load_addr
	dd 0 						; load_end_addr (patched up by BootImageBuilder)
	dd 0						; bss_end_addr
	dd real_start				; entry_addr

real_start:
	mov esp,Lkernel_esp
	cld
	call sys_clrscr

	cmp eax,0x2BADB002
	je multiboot_ok
	jmp no_multiboot_loader

multiboot_ok:
	; Copy the multiboot info block
	cld
	mov esi,ebx
	mov edi,multiboot_info
	mov ecx,MBI_SIZE
	rep movsb

	; Copy command line (if any)
	mov esi,[multiboot_info+MBI_CMDLINE]
	test esi,esi
	jz skip_multiboot_cmdline
	mov edi,multiboot_cmdline
	mov ecx,MBI_CMDLINE_MAX
	rep movsb
skip_multiboot_cmdline:

	mov ebx,[multiboot_info+MBI_MEMUPPER]	; MB upper mem
	shl ebx,10			; Convert KB -> bytes
	add ebx,0x100000	; MB upper mem starts at 1Mb

	; Initialize initial jarfile
	mov esi,[multiboot_info+MBI_MODSCOUNT]
	test esi,esi
	jz no_initJar
	mov esi,[multiboot_info+MBI_MODSADDR]
	mov eax,[esi+MBMOD_START]
	mov [initJar_start],eax
	mov eax,[esi+MBMOD_END]
	mov [initJar_end],eax
	; Round to 4K
	add eax,0x1000
	and eax,~0xfff
	mov [free_mem_start],eax
	jmp initJar_done
	
no_initJar:
	; No boot module
	mov eax,freeMemoryStart
	mov [initJar_start],eax
	mov [initJar_end],eax
	mov [free_mem_start],eax

initJar_done:
	; Done initializing initial jarfile
	

    ; Check that A20 is really enabled
    xor eax,eax
check_a20:
    inc eax
    mov dword [0x0],eax
    cmp eax, dword [0x100000]
    je check_a20 ; Just loop if this is not good.

	PRINT_STR sys_version

	; Test for a valid cpu
	call test_cpuid

	; Initialize memory manager
%ifdef BITS32	
	call Lsetup_mm
%else
	jmp Lsetup_mm
	%include "mm64.asm" 
	bits 64
start64:		
%endif	
	; Initialize interrupt handling
	call Lsetup_idt

    PRINT_STR sys_version

	; Initialize the FPU
	call init_fpu
	; Initialize SSE (if any)
	call init_sse

	PRINT_STR before_start_vm_msg

	; Go into userspace
	push dword USER_DS	; old SS
	push Luser_esp		; old ESP
	pushf				; old EFLAGS
	push dword USER_CS	; old CS
	push go_user_cs		; old EIP
	pushf
	pop eax
	and eax,~F_NT
	push eax
	popf
	iret
;	db 0xea
;	dd go_user_cs
;	dw USER_CS

no_multiboot_loader:
    PRINT_STR no_multiboot_loader_msg
    jmp _halt

go_user_cs:
	mov eax,USER_DS
	mov ss,ax
	mov esp,Luser_esp
	mov ds,ax
	mov es,ax
	mov gs,ax
	mov eax,CURPROC_FS
	mov fs,ax
	sti

	; Set tracing on
	%if 0
		pushf
		pop eax
		or eax,F_TF
		push eax
		popf
	%endif

	call sys_clrscr

	; Now start the virtual machine
	xor ebp,ebp ; Clear the frame ptr
	push ebp    ; previous EBP
	push ebp    ; MAGIC    (here invalid ON PURPOSE!)
	push ebp    ; PC           (here invalid ON PURPOSE!)
	push ebp    ; VmMethod (here invalid ON PURPOSE!)
	mov ebp,esp

	mov eax,vm_start
	add eax,BootImageBuilder_JUMP_MAIN_OFFSET32
	call eax

	mov edx, eax	; Save return code in EDX
	inc dword [jnodeFinished]

	test edx,edx
	jz _halt
	
	; Reset the system
	CMOS_WRITE 0x0F, 0x00	; Shutdown code := Soft reset
	mov bl,0xfe				; Reset system
	call _kbcmd

_halt:
	cli
	hlt
	jmp _halt
	
; Send command in bl to keyboard controller
_kbcmd:
	in al, 0x64
	test al, 0x02
	jnz _kbcmd
	mov al, bl
	out 0x64, al
_kbcmd_accept:	
	in al, 0x64
	test al, 0x02
	jz _kbcmd_accept
	ret

no_multiboot_loader_msg: db 'No multiboot loader. halt...',0;
before_start_vm_msg:     db 'Before start_vm',0xd,0xa,0
after_vm_msg:  			 db 'VM returned with EAX ',0

multiboot_info:
	times MBI_SIZE db  0

multiboot_cmdline:
	times (MBI_CMDLINE_MAX+4) db 0

