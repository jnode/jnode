; -----------------------------------------------
; $Id$
;
; Memory manager in 64-bit
;
; Author       : E.Prangsma 
; -----------------------------------------------

    global Lkernel_esp
    global Lsetup_mm 

	extern vmCurProcessor

pml4_addr		equ 0x00001000	; Physical address of page-map level-4 table
pdp0_addr		equ 0x00002000	; Physical address of first page directory pointer table
pd0_addr		equ 0x00003000	; Physical address of first page directory table
pd1_addr		equ 0x00004000	; Physical address of second page directory table
pd2_addr		equ 0x00005000	; Physical address of third page directory table
pd3_addr		equ 0x00006000	; Physical address of fourth page directory table
pt0_addr		equ 0x00007000	; Physical address of first page table (for 0-2Mb)

free_paddr		equ 0x00008000	; First free memory

mem_start		DA 0	; Start of physical memory
mem_size		DA 0	; The total size of memory from address 0 (in bytes)

initJar_start	DA 0	; Start address of initial jarfile
initJar_end		DA 0	; End address of initial jarfile

free_mem_start	DA 0	; Start address of free memory heap

CONST_2MB		equ 2*1024*1024
CONST_4KB		equ 4*1024

; Clear a page table of 4Kb
; Give the physical address of the table as parameter
%macro CLEAR_PAGE_TABLE 1
	push edi
	push ecx
	push eax
	
	mov edi,%1
	mov ecx,1024
	xor eax,eax
	rep stosd
		
	pop eax
	pop ecx	
	pop edi
%endmacro

; Set a page table entry
; Parameters
;	1)	address lsb (that will be written in the entry)
;	2)	address msb
;	3)	options
;	edi	address of entry
%macro SET_PT_ENTRY 3
	push ebx
	mov ebx,%1						; Get address lsb
	and ebx, iPF_ADDRMASK		
	or ebx, (%3 & iPF_FLAGSMASK)	; Set flags
	mov dword [edi+0], ebx
	mov ebx,%2						; Get address msb
	mov dword [edi+4], ebx
	pop ebx
%endmacro

; Setup a page directory
; Parameters
;	eax	address lsb (that will be written in the entry)
;	edx	address msb
;   edi	Address of page directory table
;   1)	options
%macro SETUP_PDIR 1
	mov ecx,512
%%lp:
	SET_PT_ENTRY eax, edx, %1
	add eax,CONST_2MB
	adc edx,0
	add edi,8
	loop %%lp
%endmacro

; Setup a page table
; Parameters
;	eax	address lsb (that will be written in the entry)
;	edx	address msb
;   edi	Address of page table
;   1)	options
%macro SETUP_PTABLE 1
	mov ecx,512
%%loop:
	SET_PT_ENTRY eax, edx, %1
	add eax,CONST_4KB
	adc edx,0
	add edi,8
	loop %%loop	
%endmacro

; ---------------------------
; setup the memory manager
; This code initially runs in 32-bit!
; ---------------------------

Lsetup_mm:
	mov [mem_size],ebx
	mov eax,rmc_memstart
	; Page align
	add eax,0x1000
	and eax,~0xfff
	mov [mem_start],eax

	
	; Print config
	PRINT_STR mem_start_str
	PRINT_WORD [mem_start]
	PRINT_STR mem_size_str
	PRINT_WORD [mem_size]

;
; Setup the paging structures
;	

	; Clear everything first
	CLEAR_PAGE_TABLE pml4_addr		; Page-map level-4 table
	CLEAR_PAGE_TABLE pdp0_addr		; Page-directory pointer table
	CLEAR_PAGE_TABLE pd0_addr		; Page-directory 0..3
	CLEAR_PAGE_TABLE pd1_addr
	CLEAR_PAGE_TABLE pd2_addr
	CLEAR_PAGE_TABLE pd3_addr
	CLEAR_PAGE_TABLE pt0_addr		; Page-table 0
	
	; Initialize page-map level-4 table
	mov edi,pml4_addr
	SET_PT_ENTRY pdp0_addr, 0, PF_DEFAULT
	
	; Initialize page-directory pointer table 0
	mov edi,pdp0_addr
	SET_PT_ENTRY pd0_addr, 0, PF_DEFAULT
	add edi,8
	SET_PT_ENTRY pd1_addr, 0, PF_DEFAULT
	add edi,8
	SET_PT_ENTRY pd2_addr, 0, PF_DEFAULT
	add edi,8
	SET_PT_ENTRY pd3_addr, 0, PF_DEFAULT

	; Initialize page directory tables	
	xor eax,eax						; We start at address 0
	xor edx,edx
	mov edi,pd0_addr	
	SETUP_PDIR (PF_DEFAULT | iPF_PSE)
	mov edi,pd1_addr 
	SETUP_PDIR (PF_DEFAULT | iPF_PSE)
	mov edi,pd2_addr
	SETUP_PDIR (PF_DEFAULT | iPF_PSE)
	mov edi,pd3_addr
	SETUP_PDIR (PF_DEFAULT | iPF_PSE)
	
	; Setup the first entry of pd0 to a 4Kb page table
	mov edi,pd0_addr
	SET_PT_ENTRY pt0_addr, 0, PF_DEFAULT
	
	; Setup the low 2Mb page table
	xor eax,eax						; We start at address 0
	xor edx,edx
	mov edi,pt0_addr
	SETUP_PTABLE PF_DEFAULT
	
	; Clear the first page
	mov edi,pt0_addr
	SET_PT_ENTRY 0, 0, 0
	
;
; Fixup the TSS entry in the GDT
;

    mov eax,tss
    pushf
    pop dword [eax+0x24]
    mov word [gdt_tss+2],ax
    shr eax,16
    mov byte [gdt_tss+4],al
    mov byte [gdt_tss+7],ah

; 
; Now load our own (temporary) 32-bits GDT
;

	lgdt [gdt64_32]
	jmp dword KERNEL32_CS:mm_gdt32_flush
mm_gdt32_flush:	
	mov eax,KERNEL32_DS
	mov ds,ax
; 
; Switch to long-mode (64-bit)
;

	; Disable paging
	mov eax,cr0
	and eax,~CR0_PG
	mov cr0,eax
	
	; Enable physical address extensions
	mov eax,cr4
	or eax,CR4_PSE		; Enable page size extensions
	or eax,CR4_PAE		; Enable physical address extensions
	mov cr4,eax
	
	; Load the page-map level-4 address
	mov eax,pml4_addr
	mov cr3,eax
	
	; Enable long mode
	mov ecx, 0c0000080h ; EFER MSR number.
	rdmsr				; Read EFER.
	bts eax, 8			; Set LME=1.
	wrmsr				; Write EFER.
	
	; Enable paging
	mov eax,cr0
	or eax,CR0_PG
	mov cr0,eax
	
	; Jump to 64-bit code
	jmp dword KERNEL_CS:mm_long_mode
	
	bits 64
	%define BITS64_ON
mm_long_mode:
	; Reload the stack pointer
	mov rsp,Lkernel_esp
	
	; Reload the GDT
	lgdt [gdt64_64]
	
	; Load TSS
	mov eax, TSS_DS
	ltr ax
	
	; Reload CR3
	mov rax,pml4_addr
	mov cr3,rax
		
    ; set IOPL to 3
    pushf
    pop rax
    or eax,F_IOPL1 | F_IOPL2
    push rax
    popf
    
    ; Flush to make sure.
    jmp mm_long_mode_flush
mm_long_mode_flush:

	PRINT_STR in_long_mode_msg
	
	; We're done	
	jmp start64

; Enable paging on the current cpu.
enable_paging:
	; TODO implement me
	
	; Jump to flush any caches
	jmp enable_pg_flush
enable_pg_flush:
	ret

; Disable paging on the current cpu.
disable_paging:
	; Reset PG in CR0 to disable paging
	mov rax,cr0
	and eax,~CR0_PG
	mov cr0,rax
	; Jump to flush any caches
	jmp disable_pg_flush
disable_pg_flush:
	ret

mem_start_str:		db 'mem-start ',0
mem_size_str:		db 0xd,0xa,'mem-size  ',0
enable_pg_msg:  	db 'enable paging',0xd,0xa,0
done_pg_msg:	    db 'paging setup finished',0xd,0xa,0
in_long_mode_msg:   db 'Long mode enabled',0xd,0xa,0

; -----------------------------------------------
; 64-bit GDT
; -----------------------------------------------

gdt64_32:
	dw (gdtend-gdtstart)-1
	dd gdtstart

gdt64_64:
	dw (gdtend-gdtstart)-1
	dq gdtstart

gdtstart:
    ; Entry 0, should be NULL
	dd 0 ;null
	dd 0

    ; Entry 1 (selector 0x08)
    ; Kernel CS
	dw 0ffffh 			; limit
	dw 0 				; base
	db 0				; more base
	db 9ah				; dpl=0,code
	db 02fh 			; L=1, D=0, more limit
	db 0 				; base

    ; Entry 2 (selector 0x10)
    ; Kernel DS
	dw 0ffffh
	dw 0
	db 0
	db 92h 				; dpl=0,data
	db 0cfh
	db 0

    ; Entry 3 (selector 0x1B)
    ; User CS
	dw 0ffffh 			; limit
	dw 0				; base
	db 0				; more base
	db 0xFA				; dpl=3,code
	db 0x2F				; L=1, D=0, more limit
	db 0				; base

    ; Entry 4 (selector 0x23)
    ; User DS
	dw 0ffffh
	dw 0
	db 0
	db 0xF2				; dpl=3,data
	db 0xCF
	db 0

    ; Entry 5 (selector 0x28)
    ; Kernel-32 CS
	dw 0ffffh 			; limit
	dw 0 				; base
	db 0				; more base
	db 9ah				; dpl=0,code
	db 0cfh 			; more limit
	db 0 				; base

    ; Entry 6 (selector 0x30)
    ; Kernel-32 DS
	dw 0ffffh
	dw 0
	db 0
	db 92h ;dpl=0,data
	db 0cfh
	db 0

    ; Entry 5 (select 0x38)
    ; TSS
gdt_tss:
    dw tss_e-tss
    dw 0				; Fixed later
    db 0				; Fixed later
    db 0x89
    db 0
    db 0				; Fixed later
    dd 0				; base 63-32
    dd 0				; Reserved
gdtend:

; -----------------------------------------------
; TSS (64-bit)
; -----------------------------------------------

tss:
    ; 0x00
    dd 0          	; Reserved
    dd Lkernel_esp	; RSP0 (lsb)
    dd 0			; RSP0 (msb)
    dd 0			; RSP1 (lsb)
    dd 0			; RSP1 (msb)
    dd 0			; RSP2 (lsb)
    dd 0			; RSP2 (msb)
    dd 0          	; Reserved
    dd 0          	; Reserved
    dd 0          	; IST1 (lsb)
    dd 0          	; IST1 (msb)
    dd 0          	; IST2 (lsb)
    dd 0          	; IST2 (msb)
    dd 0          	; IST3 (lsb)
    dd 0          	; IST3 (msb)
    dd 0          	; IST4 (lsb)
    dd 0          	; IST4 (msb)
    dd 0          	; IST5 (lsb)
    dd 0          	; IST5 (msb)
    dd 0          	; IST6 (lsb)
    dd 0          	; IST6 (msb)
    dd 0          	; IST7 (lsb)
    dd 0          	; IST7 (msb)
    dd 0          	; Reserved
    dd 0          	; Reserved
    dw 0          	; Reserved
    dw 0xFFFF      ; I/O bitmap basis (0xFFFF means NULL)
tss_e:

kernel_stack:
    ; Reserve 32K kernel stack space
    times 8*1024 dq 0
Lkernel_esp:
    dd 0

