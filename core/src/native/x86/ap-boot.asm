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
							; bits 16
	db 0x8C,0xC8			; mov ax,cs
	db 0x8E,0xD8			; mov ds,ax
	
	; Setup gdt & idt (NOTE only 24-bits are loaded since we're in 16-bit code)
	db 0x0f,0x01,0x1e	; lidt [ap_idtbase48]
	dw ap_idtbase48-ap_boot
	db 0x0f,0x01,0x16	; lgdt [ap_gdtbase48]
	dw ap_gdtbase48-ap_boot	;     so complicated because this code will be moved.
	
	; Setup protected mode
	db 0xB8,0x01,0x00		; mov ax,1
	db 0x0F,0x01,0xF0		; lmsw ax
	db 0xE9,0x00,0x00		; FLUSH

	; Jump to the normal kernel code space
ap_boot16_jmp:	
	db 0x66,0xEA			; jmp dword KERNEL_CS:ap_boot32	;; This address needs to be patched
	dd ap_boot32
	dw KERNEL_CS

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
	DA gdtstart

	align 4
ap_idtbase48:
	dw 0
	DA 0

	align 4
ap_gdtbase:
	dw (gdtend-gdtstart)-1
ap_gdt_ptr:
	DA 0
	
	bits 32
ap_boot_end:	
	
; ===============================================
; End of copied code
; ===============================================	

%undef BITS64_ON
ap_boot_in_kernel_space:
	mov eax,KERNEL_DS
	mov ss,ax
	mov ds,ax
	mov es,ax
	mov gs,ax
%ifdef BITS32	
	mov eax,CURPROC_FS
	mov fs,ax
%endif	
	
	; Load kernel ESP (EBX contains pointer to TSS)
	mov esp,[ebx+0x04]			; Kernel ESP
	push ebx					; Save TSS
	
	; Show something
	PRINT_STR ap_boot_msg
	
	; Load the idt
	lidt [idt]
	FLUSH
	
%ifdef BITS32	
	; Enable paging
	call enable_paging
	
	; Restore TSS
	pop ebx						
%else

	; Switch to long-mode
	
	; TODO implement me
	bits 64
	%define BITS64_ON

%endif

	; Now load the TSS into TR
    mov eax,TSS_DS
    ltr ax
    ; set IOPL to 3
    pushf
    pop AAX
    or AAX,F_IOPL1 | F_IOPL2
    push AAX
    popf
    FLUSH
    
	; Go into userspace
	mov ACX,[ebx+0x38]			; User ESP

	mov AAX,ap_boot_go_user_cs
	push dword USER_DS			; old SS
	push ACX					; old ESP
	pushf						; old EFLAGS
	push dword USER_CS			; old CS
	push AAX					; old EIP
	pushf
	pop AAX
	and AAX,~F_NT
	push AAX
	popf
%ifdef BITS32	
	iret
%else
	iretq
%endif		

ap_boot_go_user_cs:
%ifdef BITS32
	mov eax,USER_DS
	mov ss,ax
	mov esp,ecx
	mov ds,ax
	mov es,ax
	mov gs,ax
	mov eax,CURPROC_FS
	mov fs,ax
%endif	

    ;jmp ap_test
	
	; Show that we are in user mode
	PRINT_STR ap_user_msg
	
	; Enable interrupts
	sti
	
	; Jump to java code
	xor ABP,ABP		; Clear the frame ptr
	push ABP		; previous EBP
	push ABP		; MAGIC    (here invalid ON PURPOSE!)
	push ABP		; PC           (here invalid ON PURPOSE!)
	push ABP		; VmMethod (here invalid ON PURPOSE!)
	mov ABP,ASP
	mov AAX,VmX86Processor_applicationProcessorMain
	INVOKE_JAVA_METHOD
	
	; Return from java code
ap_halt:	
	hlt
	jmp ap_halt
	
ap_test:
	inc byte [0xb8000+0]	
	jmp ap_test
	
ap_boot_msg: 	 db 'AP-boot',0xd,0xa,0
ap_user_msg: 	 db 'AP-usermode',0xd,0xa,0
	
	