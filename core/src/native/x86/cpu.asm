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
	push AAX
	push ABX
	push ACX
	push ADX
	
	; Test for presence of cpuid instruction
	pushf			; Get current eflags
	pop AAX
	mov ecx,eax
	xor eax,F_ID	; Try to toggle ID flag
	push AAX
	popf 
	pushf
	pop AAX			; Get new eflags back
	xor eax,ecx		; Should be different, so != 0
	jz near no_cpuid
	; Now execute cpuid
	mov AAX,1
	cpuid
	; Save cpu features
	mov [cpu_features],edx
	; Test for FPU, PSE
	test edx,FEAT_FPU
	jz near no_fpu_feat
	test edx,FEAT_PSE
	jz near no_pse_feat
	; Done
	
	pop ADX
	pop ACX
	pop ABX
	pop AAX
	ret

; Initialize the FPU
init_fpu:
	fninit
	; Setup rounding mode
	lea ASP,[ASP-SLOT_SIZE]
	fstcw [ASP]
	or word [ASP], 0x0C00
	fldcw [ASP]
	lea ASP,[ASP+SLOT_SIZE]
	mov AAX,cr0
	or eax,CR0_MP			; Enable monitoring FPU 
	mov AAX,cr0
	ret
	
; Initialize SSE (if any)
init_sse:
	; Test for SSE feature
	test dword [cpu_features], FEAT_SSE
	jz no_sse
	mov AAX,cr4
	or eax,CR4_OSFXSR		; Enable SSE instructions
	or eax,CR4_OSXMMEXCPT	; We support the XMM exception
	mov cr4,AAX
	mov AAX,cr0
	and AAX,~CR0_EM			; Disable fpu emulation (we don't need it anyway)
	or eax,CR0_MP			; Enable monitoring FPU 
	mov AAX,cr0
	; Setup MXCSR	
	lea ASP,[ASP-SLOT_SIZE]
	stmxcsr [ASP]
	pop AAX
	or eax,MXCSR_IM			; Disable invalid operation exception
	or eax,MXCSR_DM			; Disable denormalized operand exception
	and AAX,~MXCSR_ZM		; Enable zero-divide exception
	or eax,MXCSR_OM			; Disable overflow exception
	or eax,MXCSR_UM			; Disable underflow exception
	or eax,MXCSR_PM			; Disable precision exception
	push AAX
	ldmxcsr [ASP]					
	lea ASP,[ASP+SLOT_SIZE]
	ret
	
no_sse:
	ret	

no_cpuid:
	PRINT_STR no_cpuid_msg
    jmp _halt
	
no_fpu_feat:	
	PRINT_STR no_fpu_feat_msg
    jmp _halt
	
no_pse_feat:	
	PRINT_STR no_pse_feat_msg
    jmp _halt
	
no_cpuid_msg:		db 'Processor has no CPUID. halt...',0;
no_pse_feat_msg:	db 'Processor has PSE support. halt...',0;
no_fpu_feat_msg:	db 'Processor has FPU support. halt...',0;
	
