; -----------------------------------------------
; $Id$
;
; Native method implementation for org.jnode.vm.x86.UnsafeX86
;
; Author       : E. Prangsma
; -----------------------------------------------

; int getGDT(int[])
Q53org5jnode2vm3x869UnsafeX8623getGDT2e285bI29I:
	mov eax,[esp+4]		; Get gdt
	test eax,eax		
	jz getGDT_ret

	push esi	
	push edi
	push ecx
	cld
	
	mov ecx,[eax+VmArray_LENGTH_OFFSET*4]
	lea edi,[eax+VmArray_DATA_OFFSET*4]
	mov esi,gdtstart
	rep movsd
	
	pop ecx
	pop edi
	pop esi
	
getGDT_ret:
	; Calculate GDT length in int's
	mov eax,gdtend-gdtstart
	shr eax,2
	ret 4

; int getTSS(int[])
Q53org5jnode2vm3x869UnsafeX8623getTSS2e285bI29I:
	mov eax,[esp+4]		; Get tss
	test eax,eax		
	jz getTSS_ret

	push esi	
	push edi
	push ecx
	cld
	
	mov ecx,[eax+VmArray_LENGTH_OFFSET*4]
	lea edi,[eax+VmArray_DATA_OFFSET*4]
	mov esi,tss
	rep movsd
	
	pop ecx
	pop edi
	pop esi
	
getTSS_ret:
	; Calculate TSS length in int's
	mov eax,tss_e-tss
	shr eax,2
	ret 4
	
; int getAPBootCodeSize();
Q53org5jnode2vm3x869UnsafeX8623getAPBootCodeSize2e2829I:
	mov eax,ap_boot_end-ap_boot
	ret

; void setupBootCode(VmAddress memory, int[] gdtBase, int[] tss);
Q53org5jnode2vm3x869UnsafeX8623setupBootCode2e28Lorg2fjnode2fvm2fVmAddress3b5bI5bI29V:
	push ebx
	
	mov eax,[esp+16]		; memory
	mov edx,[esp+12]		; gdt
	mov ebx,[esp+8]			; tss

	push esi
	push edi
	push ecx
	cld

	; Copy memory
	mov ecx,ap_boot_end-ap_boot		; length
	mov edi,eax						; memory is destination
	mov esi,ap_boot					; ap_boot code is source
	rep movsb						; copy...
	
	; Patch JUMP 16 to 32 address
	lea edi,[eax+(ap_boot16_jmp-ap_boot)+2]	; Opcode JMP (66EAxxxxxxxx0800)
	lea ecx,[eax+(ap_boot32-ap_boot)]
	mov [edi],ecx
	
	; Patch ap_gdt_ptr address
	lea edi,[eax+(ap_gdt_ptr-ap_boot)]
	lea ecx,[edx+VmArray_DATA_OFFSET*4]
	mov [edi],ecx

	; Patch ap_boot32_lgdt
	lea edi,[eax+(ap_boot32_lgdt-ap_boot)+3] ; Opcode LGDT (0F0115xxxxxxxx)
	lea ecx,[eax+(ap_gdtbase-ap_boot)]
	mov [edi],ecx

	; Patch ap_boot32_ltss
	lea edi,[eax+(ap_boot32_ltss-ap_boot)+1]	; Opcode MOV ebx,v (BBxxxxxxxx)
	lea ecx,[ebx+VmArray_DATA_OFFSET*4]
	mov [edi],ecx
	
	; Set the Warm boot address in the BIOS data area
	SYSCALL SC_DISABLE_PAGING	; We need to access page 0
	mov ecx,eax					; Memory offset
	and ecx,0xf
	mov word [0x467],cx			
	mov ecx,eax					; Memory segment
	shr ecx,4
	mov word [0x469],cx			
	SYSCALL SC_ENABLE_PAGING	; Restore paging
	; Write 0xA to CMOS address 0xF: "Jump to DWORD ..." 
	CMOS_WRITE 0x0F, 0x0A

	pop ecx
	pop edi
	pop esi
	pop ebx

	ret 8


	