; -----------------------------------------------
; $Id$
;
; Memory manager in 32-bit
;
; Author       : E.Prangsma 
; -----------------------------------------------

    global Lkernel_esp
    global Lsetup_mm 

	extern vmCurProcessor

pd_paddr	equ 0x00001000	; Physical address of page dir
pg0_paddr	equ 0x00002000	; Physical address of first page table
pg1_paddr	equ 0x00003000	; Physical address of second page table
free_paddr	equ 0x00004000	; First free memory

mem_start	dd 0	; Start of physical memory
mem_size	dd 0	; The total size of memory from address 0 (in bytes)

initJar_start	dd 0	; Start address of initial jarfile
initJar_end		dd 0	; End address of initial jarfile

free_mem_start	dd 0	; Start address of free memory heap

; ---------------------------
; setup the memory manager
; ---------------------------

Lsetup_mm:
;-- heap limitation code starts here
;Limit heap to the memory region under 2G that JNode can handle
;TODO remove this limitation when large heaps (> 2G) are supported
	cmp ebx,0x7fffffff
	jna Lmem_size_ok
	mov ebx,0x7fffffff
Lmem_size_ok:
;-- heap limitation code ends here
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
; Fixed the cur Processor entry in the GDT
;

    mov eax,vmCurProcessor
    mov word [gdt_curProc+2],ax
    shr eax,16
    mov byte [gdt_curProc+4],al
    mov byte [gdt_curProc+7],ah

;
; Now reload our own GDT
;

    lgdt [gdt]
	jmp dword KERNEL_CS:mm_gdt_flush
mm_gdt_flush:

;
; Now load the TSS into TR
;

    mov eax,0x28
    ltr ax
    ; set IOPL to 3
    pushf
    pop eax
    or eax,F_IOPL1 | F_IOPL2
    push eax
    popf
    jmp mm_ltr_flush
mm_ltr_flush:

; Now setup paging mechanism for the all memory in the system
; The first 4Mb is setup using 4Kb pages all mapped such that physical address and virtual
; address is equal. The only exception is the first page (address 0). This page is not present
; in order to detect nullpointer exceptions.
; The remaining addressspace from 4Mb to 4Gb-4Mb is setup using 4Mb pages. The last 4Mb page is
; not present also to detect nullpointer exceptions.

; First setup the page dir (located at 0x00001000)

    PRINT_STR init_pd_msg

; Setup the Page Dir. First we will just fill it with 4Mb pages. 
	cld
	mov eax,PF_DEFAULT|iPF_PSE
	mov edi,pd_paddr
	mov ecx,1024
pd_lp:
	stosd				; Set pdir entry
	add eax,0x400000	; Go to next 4Mb.
	loop pd_lp

; Now setup page 0
	xor eax,eax
	mov edi,pg0_paddr
	mov ecx,1024
pg0_lp:
	and eax,iPF_ADDRMASK	; Clear all pageflags (since this is page0, eax now contains the physical address)
	; Compare with kernel limits
	cmp eax,kernel_begin
	jb pg0_rw
	cmp eax,kernel_end
	jae pg0_rw
	or eax,PF_DEFAULT_RO	; Mark kernel pages readonly
	jmp pg0_1
pg0_rw:
	or eax,PF_DEFAULT		; Mark non-kernel pages read-write
pg0_1:
	stosd					; Set pg0 entry
	add eax,0x1000			; Go to next 4Kb
	loop pg0_lp
	
; Now clear first 4Kb page
	mov edi,pg0_paddr
	and dword [edi],0
	
; Now set pg0 in pdir
	mov eax,pg0_paddr
	mov edi,pd_paddr
	or eax,PF_DEFAULT
	mov [edi],eax
	
; Now mark the last 4Mb in pdir empty
	mov edi,pd_paddr
	and dword [edi+(1023*4)],0

	; Now enable paging
    PRINT_STR enable_pg_msg
    
    call enable_paging

; OK now we're going to setup the physical page usage map.

    PRINT_STR done_pg_msg

	ret
	
; Initialize a page table
; Input:
;   EDI the address of the pagetable
;   EDX the base address of the first page in the table
init_pt:
	push eax
	push ebx
	push edx
	push edi
	
	;mov eax,edx
	;call sys_print_eax
	
	xor eax,eax
init_pt_lp:
    ; Calculate page address
	mov ebx,eax
	shl ebx,12
	add ebx,edx
	; Compare with kernel limits
	cmp ebx,kernel_begin
	jb init_pt_rw
	cmp ebx,kernel_end
	jae init_pt_rw
	or ebx,PF_DEFAULT_RO ; Mark kernel pages readonly
	jmp init_pt_1
init_pt_rw:
	or ebx,PF_DEFAULT    ; Mark non-kernel pages read-write
init_pt_1:
	mov [edi+eax*4],ebx
	inc eax
	cmp eax,1024
	jne init_pt_lp
	
	; Now make the table known in th page directory table
	mov ebx,pd_paddr
	shr edx,22 
	or edi,PF_DEFAULT
	mov [ebx+edx*4],edi
	
	pop edi
	pop edx
	pop ebx
	pop eax
	ret

; Enable paging on the current cpu.
enable_paging:
	; Set the pdir address
	mov eax,pd_paddr
	mov cr3,eax
	; Set PSE in CR4 to enable 4Mb extensions
	mov eax,cr4
	or eax,CR4_PSE
	mov cr4,eax
	; Set PG in CR0 to enable paging
	mov eax,cr0
	or eax,CR0_PG
	mov cr0,eax
	; Jump to flush any caches
	jmp enable_pg_flush
enable_pg_flush:
	ret

; Disable paging on the current cpu.
disable_paging:
	; Reset PG in CR0 to disable paging
	mov eax,cr0
	and eax,~CR0_PG
	mov cr0,eax
	; Jump to flush any caches
	jmp disable_pg_flush
disable_pg_flush:
	ret


mem_start_str:	db 'mem-start ',0
mem_size_str:	db 0xd,0xa,'mem-size ',0
init_pd_msg:    db 'init page-dir table',0xd,0xa,0
init_pg0_msg:   db 'init page 0 table',0xd,0xa,0
enable_pg_msg:  db 'enable paging',0xd,0xa,0
done_pg_msg:    db 'paging setup finished',0xd,0xa,0

; -----------------------------------------------
; GDT
; -----------------------------------------------

gdt:
	dw 56-1
	dd gdtstart

gdtstart:
    ; Entry 0, should be NULL
	dd 0 ;null
	dd 0

    ; Entry 1 (selector 0x08)
    ; Kernel CS
	dw 0ffffh ;limit
	dw 0 ;base
	db 0 ;more base
	db 9ah ;dpl=0,code
	db 0cfh ;other stuff (i forget), more limit
	db 0 ;base

    ; Entry 2 (selector 0x10)
    ; Kernel DS
	dw 0ffffh
	dw 0
	db 0
	db 92h ;dpl=0,data
	db 0cfh
	db 0

    ; Entry 3 (selector 0x1B)
    ; User CS
	dw 0ffffh ;limit
	dw 0 ;base
	db 0 ;more base
	db 0xFA ;dpl=3,code
	db 0xCF ;other stuff (i forget), more limit
	db 0 ;base

    ; Entry 4 (selector 0x23)
    ; User DS
	dw 0ffffh
	dw 0
	db 0
	db 0xF2 ;dpl=3,data
	db 0xCF
	db 0

    ; Entry 5 (select 0x28)
    ; TSS
gdt_tss:
    dw tss_e-tss
    dw 0 ; Fixed later
    db 0 ; Fixed later
    db 0x89
    db 0
    db 0 ; Fixed later

    ; Entry 6 (select 0x33)
    ; Current processor
gdt_curProc:
    dw VmX86Processor_SIZE
    dw 0 ; Fixed later
    db 0 ; Fixed later
	db 0xF2 ;dpl=3,data
    db 0
    db 0 ; Fixed later
gdtend:

; -----------------------------------------------
; TSS
; -----------------------------------------------

tss:
    ; 0x00
    dd 0           ; Terugkoppeling
    dd Lkernel_esp ; ESP0
    dw KERNEL_DS   ; SS0
    dw 0           ; R
    dd 0           ; ESP1
    ; 0x10
    dd 0           ; R - SS1
    dd 0           ; ESP2
    dd 0           ; R - SS2
    dd pd_paddr    ; CR3
    ; 0x20
    dd 0           ; EIP
    dd 0           ; EFLAGS
    dd 0           ; EAX
    dd 0           ; ECX
    ; 0x30
    dd 0           ; EDX
    dd 0           ; EBX
    dd Luser_esp   ; ESP
    dd 0           ; EBP
    ; 0x40
    dd 0           ; ESI
    dd 0           ; EDI
    dd USER_DS     ; ES
    dd USER_CS     ; CS
    ; 0x50
    dd USER_DS     ; SS
    dd USER_DS     ; DS
    dd USER_DS     ; FS
    dd USER_DS     ; GS
    ; 0x60
    dd 0           ; LDT
    dw 0           ; R
    dw 0xFFFF      ; I/O bitmap basis (0xFFFF means NULL)
tss_e:

kernel_stack:
    ; Reserve 16K kernel stack space
    times 8*1024 dd 0
Lkernel_esp:
    dd 0

