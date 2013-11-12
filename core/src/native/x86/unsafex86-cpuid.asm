; -----------------------------------------------
; $Id: unsafe-cpuid.asm 1036 2005-01-30 10:41:32Z epr $
;
; Native method implementation for org.jnode.vm.x86.UnsafeX86
; of the CPU identification methods.
;
; Author       : E. Prangsma
; -----------------------------------------------

;	 * Read CPU identification data.
;	 * 
;	 * @param input  The number to put in EAX
;    * @param result An array of length 4 (or longer) where eax, ebx, ecx, edx is stored into.
;	 * @return 1 on success, 0 otherwise (result == null or result.length less than 4).
;	public static native int getCPUID(Word input, int[] result);
GLABEL Q53org5jnode2vm3x869UnsafeX8623getCPUID2e28Lorg2fvmmagic2funboxed2fWord3b5bI29I 
	push ADI
	mov ADI,[ASP+(2*SLOT_SIZE)]		; Get result
	mov AAX,[ASP+(3*SLOT_SIZE)]		; Get input
	push ABX
	push ACX
	push ADX
	
	test ADI,ADI		; is id null?
	je cpuid_invalid_arg
	mov ebx,4 			; We need an array of length 4 (or more)
	cmp ebx,[ADI+VmArray_LENGTH_OFFSET*SLOT_SIZE]
	ja cpuid_invalid_arg; id is not large enough?
	
	lea ADI,[ADI+VmArray_DATA_OFFSET*SLOT_SIZE]		; Load &id[0] into edi
	; Execute CPUID
	cpuid
	mov [ADI+0],eax		; store eax
	mov [ADI+4],ebx		; store ebx
	mov [ADI+8],ecx		; store ecx
	mov [ADI+12],edx	; store edx
	; Signal valid return
	mov eax,1			; Return 1
	jmp cpuid_ret
	
cpuid_invalid_arg:	
	xor eax,eax 		; Return 0
	
cpuid_ret:
	pop ADX
	pop ACX
	pop ABX
	pop ADI
	ret SLOT_SIZE

	