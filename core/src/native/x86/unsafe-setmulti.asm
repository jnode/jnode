; -----------------------------------------------
; $Id$
;
; Native method implementation for org.jnode.vm.Unsafe
; of the set multiple values operations.
;
; Author       : E. Prangsma
; -----------------------------------------------

; void setBytes(Address memPtr, byte value, int count);
GLABEL Q43org5jnode2vm6Unsafe23setBytes2e28Lorg2fvmmagic2funboxed2fAddress3bBI29V
	push ADI
	mov ADI,[ASP+(4*SLOT_SIZE)]		; memPtr
	mov eax,[ASP+(3*SLOT_SIZE)] 	; value
	mov ecx,[ASP+(2*SLOT_SIZE)]		; count
	rep stosb
	pop ADI
	ret SLOT_SIZE*3

; void setShorts(Address memPtr, short value, int count);
; void setChars(Address memPtr, char value, int count);
GLABEL Q43org5jnode2vm6Unsafe23setShorts2e28Lorg2fvmmagic2funboxed2fAddress3bSI29V
GLABEL Q43org5jnode2vm6Unsafe23setChars2e28Lorg2fvmmagic2funboxed2fAddress3bCI29V
	push ADI
	mov ADI,[ASP+(4*SLOT_SIZE)]		; memPtr
	mov eax,[ASP+(3*SLOT_SIZE)] 	; value
	mov ecx,[ASP+(2*SLOT_SIZE)]		; count
	rep stosw
	pop ADI
	ret SLOT_SIZE*3

; void setInts(Address memPtr, int value, int count);
; void setFloats(Address memPtr, float value, int count);
GLABEL Q43org5jnode2vm6Unsafe23setInts2e28Lorg2fvmmagic2funboxed2fAddress3bII29V
GLABEL Q43org5jnode2vm6Unsafe23setFloats2e28Lorg2fvmmagic2funboxed2fAddress3bFI29V
	push ADI
	mov ADI,[ASP+(4*SLOT_SIZE)]		; memPtr
	mov eax,[ASP+(3*SLOT_SIZE)] 	; value
	mov ecx,[ASP+(2*SLOT_SIZE)]		; count
	rep stosd
	pop ADI
	ret SLOT_SIZE*3

; void setObjects(Address memPtr, Object value, int count);
GLABEL Q43org5jnode2vm6Unsafe23setObjects2e28Lorg2fvmmagic2funboxed2fAddress3bLjava2flang2fObject3bI29V
	push ADI
	mov ADI,[ASP+(4*SLOT_SIZE)]		; memPtr
	mov AAX,[ASP+(3*SLOT_SIZE)]	 	; value
	mov ecx,[ASP+(2*SLOT_SIZE)]		; count
%ifdef BITS32	
	rep stosd
%else
	rep stosq
%endif	
	pop ADI
	ret SLOT_SIZE*3

; void setInts24(Address memPtr, int value, int count);
GLABEL Q43org5jnode2vm6Unsafe23setInts242e28Lorg2fvmmagic2funboxed2fAddress3bII29V
	push ADI
	mov ADI,[ASP+(4*SLOT_SIZE)]		; memPtr
	mov eax,[ASP+(3*SLOT_SIZE)] 	; value
	mov ecx,[ASP+(2*SLOT_SIZE)]		; count
	and eax,0xFFFFFF				; Mask of high 8-bits
set24_loop:
	mov edx,[ADI]
	and edx,0xFF000000
	or edx,eax
	mov [ADI],edx
	lea ADI,[ADI+3]
	loop set24_loop
	pop ADI
	ret SLOT_SIZE*3

; void setLongs(Address memPtr, long value, int count);
; void setDoubles(Address memPtr, double value, int count);
GLABEL Q43org5jnode2vm6Unsafe23setLongs2e28Lorg2fvmmagic2funboxed2fAddress3bJI29V
GLABEL Q43org5jnode2vm6Unsafe23setDoubles2e28Lorg2fvmmagic2funboxed2fAddress3bDI29V
%ifdef BITS32
	push edi
	mov edi,[esp+20]	; memPtr
	mov edx,[esp+16] 	; value MSB
	mov eax,[esp+12] 	; value LSB
	mov ecx,[esp+8]		; count
	test ecx,0xFFFFFFFF	; (count == 0) ??
	jz set64_end
set64_loop:
	stosd				; LSB
	xchg eax,edx		; Swap LSB,MSB
	stosd				; MSB
	xchg eax,edx		; Swap LSB,MSB
	loop set64_loop
set64_end:	
	pop edi
	ret 16
%else
	push ADI
	mov ADI,[ASP+(5*SLOT_SIZE)]		; memPtr
	mov AAX,[ASP+(3*SLOT_SIZE)] 	; value (+garbage)
	mov ecx,[ASP+(2*SLOT_SIZE)]		; count
	rep stosq
	pop ADI
	ret SLOT_SIZE*4
%endif	
		align 4096

	