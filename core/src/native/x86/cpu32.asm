; -----------------------------------------------
; $Id$
;
; CPU 32-bit test code
;
; Author       : E. Prangsma
; -----------------------------------------------

	bits 32
%undef BITS64_ON
%include "i386_bits.h"

cpu_features:	DA 0

; Test if the cpu is at least a pentium
test_cpuid:
	push eax
	push ebx
	push ecx
	push edx
	
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
	
	pop edx
	pop ecx
	pop ebx
	pop eax
	ret

no_cpuid:
	PRINT_STR no_cpuid_msg
	LOOPDIE
	
no_fpu_feat:	
	PRINT_STR no_fpu_feat_msg
	LOOPDIE
	
no_pse_feat:	
	PRINT_STR no_pse_feat_msg
	LOOPDIE
	
no_cpuid_msg:		db 'Processor has no CPUID. halt...',0;
no_pse_feat_msg:	db 'Processor has no PSE support. halt...',0;
no_fpu_feat_msg:	db 'Processor has no FPU support. halt...',0;
	
%ifdef BITS64
	bits 64
	%define BITS64_ON
	%include "i386_bits.h"
%endif	
