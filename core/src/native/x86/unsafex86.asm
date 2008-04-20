; -----------------------------------------------
; $Id$
;
; Native method implementation for org.jnode.vm.x86.UnsafeX86
;
; Author       : E. Prangsma
; -----------------------------------------------

; int getGDT(int[])
GLABEL Q53org5jnode2vm3x869UnsafeX8623getGDT2e285bI29I
	mov AAX,[ASP+SLOT_SIZE]		; Get gdt
	test AAX,AAX
	jz getGDT_ret

	push ASI
	push ADI
	push ACX
	cld
	
	mov ecx,[AAX+VmArray_LENGTH_OFFSET*SLOT_SIZE]
	lea ADI,[AAX+VmArray_DATA_OFFSET*SLOT_SIZE]
	mov ASI,gdtstart
	rep movsd
	
	pop ACX
	pop ADI
	pop ASI
	
getGDT_ret:
	; Calculate GDT length in int's
	mov eax,gdtend-gdtstart
	shr eax,2
	ret SLOT_SIZE

; int getTSS(int[])
GLABEL Q53org5jnode2vm3x869UnsafeX8623getTSS2e285bI29I
	mov AAX,[ASP+SLOT_SIZE]		; Get tss
	test AAX,AAX		
	jz getTSS_ret

	push ASI	
	push ADI
	push ACX
	cld
	
	mov ecx,[AAX+VmArray_LENGTH_OFFSET*SLOT_SIZE]
	lea ADI,[AAX+VmArray_DATA_OFFSET*SLOT_SIZE]
	mov ASI,tss
	rep movsd
	
	pop ACX
	pop ADI
	pop ASI
	
getTSS_ret:
	; Calculate TSS length in int's
	mov eax,tss_e-tss
	shr eax,2
	ret SLOT_SIZE
	
; int getAPBootCodeSize();
GLABEL Q53org5jnode2vm3x869UnsafeX8623getAPBootCodeSize2e2829I
	mov eax,ap_boot_end-ap_boot
	ret
	
; Address getCR3();
GLABEL Q53org5jnode2vm3x869UnsafeX8623getCR32e2829Lorg2fvmmagic2funboxed2fAddress3b
%ifdef BITS32
	mov eax,pd_paddr
%else
	mov rax,pml4_addr
%endif
	ret

; Address getMultibootMMap();
GLABEL Q53org5jnode2vm3x869UnsafeX8623getMultibootMMap2e2829Lorg2fvmmagic2funboxed2fAddress3b
	mov AAX,multiboot_mmap+4
	ret

; int getMultibootMMapLength();
GLABEL Q53org5jnode2vm3x869UnsafeX8623getMultibootMMapLength2e2829I
	mov eax,[multiboot_mmap]
	ret

; Address getVbeInfos();
GLABEL Q53org5jnode2vm3x869UnsafeX8623getVbeInfos2e2829Lorg2fvmmagic2funboxed2fAddress3b
%ifdef SETUP_VBE
	mov AAX,multiboot_vbe
%endif	
%ifndef SETUP_VBE
	mov AAX,0
%endif	
	ret

; Address getVbeControlInfos();
GLABEL Q53org5jnode2vm3x869UnsafeX8623getVbeControlInfos2e2829Lorg2fvmmagic2funboxed2fAddress3b
%ifdef SETUP_VBE
	mov AAX,vbe_control_info
%endif	
%ifndef SETUP_VBE
	mov AAX,0
%endif	
	ret

; Address getVbeModeInfos();
GLABEL Q53org5jnode2vm3x869UnsafeX8623getVbeModeInfos2e2829Lorg2fvmmagic2funboxed2fAddress3b
%ifdef SETUP_VBE
	mov AAX,vbe_mode_info
%endif	
%ifndef SETUP_VBE
	mov AAX,0
%endif	
	ret

; Address getMultibootInfos();
GLABEL Q53org5jnode2vm3x869UnsafeX8623getMultibootInfos2e2829Lorg2fvmmagic2funboxed2fAddress3b
%ifdef SETUP_VBE
	mov AAX,multiboot_infos
%endif	
%ifndef SETUP_VBE
	mov AAX,0
%endif	
	ret

; void setupBootCode(Address memory, int[] gdtBase, int[] tss);
GLABEL Q53org5jnode2vm3x869UnsafeX8623setupBootCode2e28Lorg2fvmmagic2funboxed2fAddress3b5bI5bI29V
	push ABX
	
	mov AAX,[ASP+(4*SLOT_SIZE)]		; memory
	mov ADX,[ASP+(3*SLOT_SIZE)]		; gdt
	mov ABX,[ASP+(2*SLOT_SIZE)]		; tss

	push ASI
	push ADI
	push ACX
	cld

	; Copy memory
	mov ACX,ap_boot_end-ap_boot		; length
	mov ADI,AAX						; memory is destination
	mov ASI,ap_boot					; ap_boot code is source
	rep movsb						; copy...
	
	; Patch JUMP 16 to 32 address
	lea ADI,[AAX+(ap_boot16_jmp-ap_boot)+2]	; Opcode JMP (66EAxxxxxxxx0800)
	lea ACX,[AAX+(ap_boot32-ap_boot)]
	mov [ADI],ecx
	
	; Patch ap_gdt_ptr address
	lea ADI,[AAX+(ap_gdt_ptr-ap_boot)]
	lea ACX,[ADX+VmArray_DATA_OFFSET*SLOT_SIZE]
	mov [ADI],ACX

	; Patch ap_boot32_lgdt
	lea ADI,[AAX+(ap_boot32_lgdt-ap_boot)+3] ; Opcode LGDT (0F0115xxxxxxxx)
	lea ACX,[AAX+(ap_gdtbase-ap_boot)]
	mov [ADI],ecx

	; Patch ap_boot32_ltss
	lea ADI,[AAX+(ap_boot32_ltss-ap_boot)+1]	; Opcode MOV ebx,v (BBxxxxxxxx)
	lea ACX,[ABX+VmArray_DATA_OFFSET*SLOT_SIZE]
	mov [ADI],ecx
	
	; Set the Warm boot address in the BIOS data area
	SYSCALL SC_DISABLE_PAGING	; We need to access page 0
	mov ACX,AAX					; Memory offset
	and ACX,0xf
	mov word [0x467],cx			
	mov ACX,AAX					; Memory segment
	shr ecx,4
	mov word [0x469],cx			
	SYSCALL SC_ENABLE_PAGING	; Restore paging
	; Write 0xA to CMOS address 0xF: "Jump to DWORD ..." 
	CMOS_WRITE 0x0F, 0x0A

	pop ACX
	pop ADI
	pop ASI
	pop ABX

	ret SLOT_SIZE*3

; static final native void syncMSRs();
GLABEL Q53org5jnode2vm3x869UnsafeX8623syncMSRs2e2829V
	SYSCALL SC_SYNC_MSRS
	ret

; static final native void saveMSRs();
GLABEL Q53org5jnode2vm3x869UnsafeX8623saveMSRs2e2829V	
	SYSCALL SC_SAVE_MSRS
	ret

; static final native void restoreMSRs();
GLABEL Q53org5jnode2vm3x869UnsafeX8623restoreMSRs2e2829V	
	SYSCALL SC_RESTORE_MSRS
	ret
	