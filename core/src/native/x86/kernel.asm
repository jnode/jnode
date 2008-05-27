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
	dd 0x00010007				; Feature flags
	dd 0-0x1BADB002-0x00010007	; Checksum
	dd mb_header				; header_addr
	dd sys_start				; load_addr
	dd 0 						; load_end_addr (patched up by BootImageBuilder)
	dd 0						; bss_end_addr
	dd real_start				; entry_addr
	dd 0						; mode_type 
	dd 0						; width
	dd 0						; height
	dd 0						; depth

real_start:
	mov esp,Lkernel_esp
	cld
	CLEAR_SCREEN

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

	; Copy memmap (if any)
	test dword [multiboot_info+MBI_FLAGS],MBF_MMAP
	jz multiboot_mmap_done
	; Get start address
	mov esi,[multiboot_info+MBI_MMAPADDR]
	; Get destination address
	mov edi,multiboot_mmap+4
	; Get end address
	mov edx,esi
	add edx,[multiboot_info+MBI_MMAPLENGTH]
	; Skip size of first entry
	lea esi,[esi+4]	
multiboot_mmap_copy:
	; Get entry size
	mov eax,[esi+MBMMAP_SIZE]
	; Copy the 20 bytes
	mov ecx,MBMMAP_ESIZE
	push esi
	rep movsb
	pop esi
	; Increment entry count
	inc dword [multiboot_mmap]
	; Move to next entry
	lea esi,[esi+eax+4]		; Source address += entrysize + 4
	; edi is already incremented by rep movsb
	; #Entries > MBI_MMAP_MAX
	cmp dword [multiboot_mmap],MBI_MMAP_MAX
	jge multiboot_mmap_done
	cmp esi,edx				; source >= end?
	jb multiboot_mmap_copy
multiboot_mmap_done:

%ifdef SETUP_VBE
	; Are vbe informations available ? 
	test dword [multiboot_info+MBI_FLAGS],MBF_VBE
	jz vbe_info_done ; no vbe info, jump to end
	
	; Copy vbe infos
	; Get start address
	mov esi,[multiboot_info+MBI_VBECTRLINFO]
	; Get destination address
	mov edi,multiboot_vbe
	; Copy the VBE_ESIZE bytes
	mov ecx,VBE_ESIZE
	rep movsb

	; Get start address
	mov esi,[multiboot_info+MBI_VBECTRLINFO]
	; Get destination address
	mov edi,vbe_control_info
	; Copy the VBECTRLINFO_SIZE bytes
	mov ecx,VBECTRLINFO_SIZE
	rep movsb
	
	; Get start address
	mov esi,[multiboot_info+MBI_VBEMODEINFO]
	; Get destination address
	mov edi,vbe_mode_info
	; Copy the VBEMODEINFO_SIZE bytes
	mov ecx,VBEMODEINFO_SIZE
	rep movsb	
vbe_info_done:
%endif

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

	; Initialize kernel debugger communication
	call kdb_init

	; Print version
	PRINT_STR sys_version
	
	; Print Multiboot flags
	PRINT_STR mbflags_msg
	PRINT_INT [multiboot_info+MBI_FLAGS]
	NEWLINE

	; Test for a valid cpu
	call test_cpuid
	PRINT_STR cpu_ok_msg

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
	mov ABX,Luser_esp
	mov ACX,go_user_cs
	mov ASI,USER_DS
	mov ADI,USER_CS
	push ASI			; old SS
	push ABX			; old ESP
	pushf				; old EFLAGS
	push ADI			; old CS
	push ACX			; old EIP
	pushf
	pop AAX
	and eax,~F_NT
	push AAX
	popf
%ifdef BITS32	
	iret
%else
	iretq
%endif	

no_multiboot_loader:
    PRINT_STR no_multiboot_loader_msg
    jmp _halt

go_user_cs:
%ifdef BITS32
	mov eax,USER_DS
	mov ss,ax
	mov ds,ax
	mov es,ax
	mov gs,ax
	mov eax,CURPROC_FS
	mov fs,ax
%endif	
	mov ASP,Luser_esp
%ifdef BITS64
	; Setup current processor
	mov r12, vmCurProcessor
%endif	
	mov KERNELSTACKEND, BOOT_KERNEL_STACKEND
	sti

	; Set tracing on
	%if 0
		pushf
		pop eax
		or eax,F_TF
		push eax
		popf
	%endif

	%if 0
		CLEAR_SCREEN
	%endif

	; Now start the virtual machine
	xor ABP,ABP	; Clear the frame ptr
	push ABP    ; previous EBP
	push ABP    ; MAGIC    (here invalid ON PURPOSE!)
	push ABP    ; PC           (here invalid ON PURPOSE!)
	push ABP    ; VmMethod (here invalid ON PURPOSE!)
	mov ABP,ASP

	mov AAX,vm_start
%ifdef BITS32	
	add AAX,BootImageBuilder_JUMP_MAIN_OFFSET32
%else
	add AAX,BootImageBuilder_JUMP_MAIN_OFFSET64
%endif	
	call AAX

	mov ADX, AAX	; Save return code in EDX
	inc WORD [jnodeFinished]

	test ADX,ADX
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
cpu_ok_msg:			     db 'CPU tested ok',0xd,0xa,0
mbflags_msg:		     db 'Multiboot flags ',0

multiboot_info:
	times MBI_SIZE db  0

multiboot_cmdline:
	times (MBI_CMDLINE_MAX+4) db 0

multiboot_mmap:
	dd 0				; Entries
	times (MBI_MMAP_MAX * MBMMAP_ESIZE) db 0

%ifdef SETUP_VBE
multiboot_vbe:
	times (VBE_ESIZE) db 0
	
vbe_control_info:	
	times (VBECTRLINFO_SIZE) db 0

vbe_mode_info:	
	times (VBEMODEINFO_SIZE) db 0
%endif