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
Q43org5jnode2vm6Unsafe23getCPUID2e285bI29I:
	mov eax,[esp+4]		; Get id
	push edi
	push ebx
	push ecx
	push edx
	
	mov edi,eax			; edi = id
	mov eax,0
	cpuid				; eax contains maximum input value
	lea ecx,[eax+1]		; Store maximum+1 for later 
	lea eax,[eax*4+4]	; Calculate id.length (4 registers * (maximum input value+1))
	
	cmp edi,0			; is id null?
	je cpuid_ret
	cmp eax,[edi+VmArray_LENGTH_OFFSET*4]
	ja cpuid_ret		; id is not large enough?
	
	lea edi,[edi+VmArray_DATA_OFFSET*4]		; Load &id[0] into edi
	push eax
	pushf
	cld
	mov eax,0
cpuid_loop:
	push eax
	push ecx
	cpuid
	stosd				; store eax
	mov eax,ebx
	stosd				; store ebx
	mov eax,ecx
	stosd				; store ecx
	mov eax,edx
	stosd				; store edx
	pop ecx
	pop eax
	inc eax
	loop cpuid_loop
	popf
	pop eax
	
cpuid_ret:
	pop edx
	pop ecx
	pop ebx
	pop edi
	ret 4
