; -----------------------------------------------
; $Id$
;
; CPU initialization code
;
; Author       : E. Prangsma
; -----------------------------------------------

cpu_features:	dd 0

; Test if the cpu is at least a pentium
test_cpuid:
	pusha
	; Test for presence of cpuid instruction
	pushf			; Get current eflags
	pop eax
	mov ecx,eax
	xor eax,F_ID	; Try to toggle ID flag
	push eax
	popf 
	pushf
	pop eax			; Get new eflags back
	xor eax,ecx		; Should be different, so != 0
	jz no_cpuid
	; Now execute cpuid
	mov eax,1
	cpuid
	; Save cpu features
	mov [cpu_features],edx
	; Test for FPU, PSE
	test edx,FEAT_FPU
	jz no_fpu_feat
	test edx,FEAT_PSE
	jz no_pse_feat
	; Done
	popa
	ret

; Initialize the FPU
init_fpu:
	fninit
	; Setup rounding mode
	lea esp,[esp-4]
	fstcw [esp]
	or word [esp], 0x0C00
	fldcw [esp]
	lea esp,[esp+4]
	ret
	
; Initialize SSE (if any)
init_sse:
	test dword [cpu_features], FEAT_SSE
	jz no_sse
	mov eax,cr4
	or eax,CR4_OSFXSR		; Enable SSE instructions
	or eax,CR4_OSXMMEXCPT	; We support the XMM exception
	mov cr4,eax
	mov eax,cr0
	and eax,~CR0_EM			; Disable fpu emulation (we don't need it anyway)
	or eax,CR0_MP			; Enable monitoring FPU 
	mov eax,cr0
	ret
	
no_sse:
	ret	

no_cpuid:
	mov eax,no_cpuid_msg
    call sys_print_str
    jmp _halt
	
no_fpu_feat:	
	mov eax,no_fpu_feat_msg
    call sys_print_str
    jmp _halt
	
no_pse_feat:	
	mov eax,no_pse_feat_msg
    call sys_print_str
    jmp _halt
	
no_cpuid_msg:		db 'Processor has no CPUID. halt...',0;
no_pse_feat_msg:	db 'Processor has PSE support. halt...',0;
no_fpu_feat_msg:	db 'Processor has FPU support. halt...',0;
	
