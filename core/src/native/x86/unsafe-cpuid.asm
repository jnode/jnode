; -----------------------------------------------
; $Id$
;
; Native method implementation for org.jnode.vm.Unsafe
; of the CPU identification methods.
;
; Author       : E. Prangsma
; -----------------------------------------------

;	 * Read CPU identification data.
;	 * 
;	 * If id is null, this method will return the length of the id array
;	 * that is required to fit all data.
;	 * If id is not null and long enough, it is filled with all identification
;	 * data.
;	 * 
;	 * @param id 
;	 * @return The required length of id.
;	public static native int getCPUID(int[] id);
GLABEL Q43org5jnode2vm6Unsafe23getCPUID2e285bI29I
	mov AAX,[ASP+SLOT_SIZE]		; Get id
	push ADI
	push ABX
	push ACX
	push ADX
	
	mov ADI,AAX			; edi = id
	xor eax,eax
	cpuid				; eax contains maximum input value
	lea ecx,[eax+1]		; Store maximum+1 for later 
	lea eax,[eax*4+4]	; Calculate id.length (4 registers * (maximum input value+1))
	
	test ADI,ADI		; is id null?
	je cpuid_ret
	cmp eax,[ADI+VmArray_LENGTH_OFFSET*SLOT_SIZE]
	ja cpuid_ret		; id is not large enough?
	
	lea ADI,[ADI+VmArray_DATA_OFFSET*SLOT_SIZE]		; Load &id[0] into edi
	push AAX
	pushf
	cld
	xor eax,eax
cpuid_loop:
	push AAX
	push ACX
	cpuid
	stosd				; store eax
	mov eax,ebx
	stosd				; store ebx
	mov eax,ecx
	stosd				; store ecx
	mov eax,edx
	stosd				; store edx
	pop ACX
	pop AAX
	inc eax
	loop cpuid_loop
	popf
	pop AAX
	
cpuid_ret:
	pop ADX
	pop ACX
	pop ABX
	pop ADI
	ret SLOT_SIZE
