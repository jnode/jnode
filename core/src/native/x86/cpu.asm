; -----------------------------------------------
; $Id$
;
; CPU initialization code
;
; Author       : E. Prangsma
; -----------------------------------------------

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

	
