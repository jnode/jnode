; -----------------------------------------------
; $Id$
;
; Application processor boot code.
; Only used on Multi processor systems.
;
; Author       : E. Prangsma
; -----------------------------------------------

	extern VmX86Processor_applicationProcessorMain

; ===============================================
; NOTE: This code is copied to another address
;       so make sure it is relocatable!!!
; ===============================================	
	align 4
ap_boot:
	bits 16
	mov ax,cs
	mov ds,ax
	
	; Setup gdt & idt (NOTE only 24-bits are loaded since we're in 16-bit code)
	db 0x0f,0x01,0x1e	; lidt [ap_idtbase48]
	dw ap_idtbase48-ap_boot
	db 0x0f,0x01,0x16	; lgdt [ap_gdtbase48]
	dw ap_gdtbase48-ap_boot	;     so complicated because this code will be moved.
	
	; Setup protected mode
	mov ax,1
	lmsw ax
	FLUSH

	; Jump to the normal kernel code space
ap_boot16_jmp:	
	jmp dword KERNEL_CS:ap_boot32	;; This address needs to be patched

	bits 32
	; Now we're in 32-bit protected mode
ap_boot32:	
	mov eax,KERNEL_DS
	mov ds,ax
	
	; Load full 64-bit gdt
ap_boot32_lgdt:
	lgdt [ap_gdtbase]
		
ap_boot32_ltss:
	mov ebx,0					;; This value needs to be patched with actual TSS address		
		
	; Jump to kernel code (above 1Mb) 
	jmp dword KERNEL_CS:ap_boot_in_kernel_space

	align 4
ap_gdtbase48:		
	dw (gdtend-gdtstart)-1
	dd gdtstart

	align 4
ap_idtbase48:
	dw 0
	dd 0

	align 4
ap_gdtbase:
	dw (gdtend-gdtstart)-1
ap_gdt_ptr:
	dd 0
	
	bits 32
ap_boot_end:	
	
; ===============================================
; End of copied code
; ===============================================	

ap_boot_in_kernel_space:
	mov eax,KERNEL_DS
	mov ss,ax
	mov ds,ax
	mov es,ax
	mov gs,ax
	mov eax,CURPROC_FS
	mov fs,ax
	
	; Load kernel ESP (EBX contains pointer to TSS)
	mov esp,[ebx+0x04]			; Kernel ESP
	push ebx					; Save TSS
	
	; Show something
	mov eax,ap_boot_msg
	call sys_print_str
	
	; Load the idt
	lidt [idt]
	FLUSH
	
	; Enable paging
	call enable_paging

	; Now load the TSS into TR
    mov eax,TSS_DS
    ltr ax
    ; set IOPL to 3
    pushf
    pop eax
    or eax,F_IOPL1 | F_IOPL2
    push eax
    popf
    FLUSH
    
	; Go into userspace
	pop ebx						; Restore TSS
	mov ecx,[ebx+0x38]			; User ESP

	push dword USER_DS			; old SS
	push ecx					; old ESP
	pushf						; old EFLAGS
	push dword USER_CS			; old CS
	push ap_boot_go_user_cs		; old EIP
	pushf
	pop eax
	and eax,~F_NT
	push eax
	popf
	iret

ap_boot_go_user_cs:
	mov eax,USER_DS
	mov ss,ax
	mov esp,ecx
	mov ds,ax
	mov es,ax
	mov gs,ax
	mov eax,CURPROC_FS
	mov fs,ax

    ;jmp ap_test
	
	; Show that we are in user mode
	mov eax,ap_user_msg
	call sys_print_str
	
	; Enable interrupts
	sti
	
	; Jump to java code
	xor ebp,ebp ; Clear the frame ptr
	push ebp    ; previous EBP
	push ebp    ; MAGIC    (here invalid ON PURPOSE!)
	push ebp    ; PC           (here invalid ON PURPOSE!)
	push ebp    ; VmMethod (here invalid ON PURPOSE!)
	mov ebp,esp
	mov eax,VmX86Processor_applicationProcessorMain
	INVOKE_JAVA_METHOD
	
	; Return from java code
ap_halt:	
	hlt
	jmp ap_halt
	
ap_test:
	inc byte [0xb8000+0]	
	jmp ap_test
	
ap_boot_msg: 	 db 'AP-boot',0
ap_user_msg: 	 db 'AP-usermode #$#$#$#$#$#$#$#$#$#$#$#$#$#$#',0
	
	