; -----------------------------------------------
; $Id$
;
; Native method implementation for org.jnode.vm.Unsafe
; of the set multiple values operations.
;
; Author       : E. Prangsma
; -----------------------------------------------

; void setBytes(Address memPtr, byte value, int count);
Q43org5jnode2vm6Unsafe23setBytes2e28Lorg2fvmmagic2funboxed2fAddress3bBI29V:
	push edi
	mov edi,[esp+16]	; memPtr
	mov eax,[esp+12] 	; value
	mov ecx,[esp+8]		; count
	rep stosb
	pop edi
	ret 12

; void setShorts(Address memPtr, short value, int count);
; void setChars(Address memPtr, char value, int count);
Q43org5jnode2vm6Unsafe23setShorts2e28Lorg2fvmmagic2funboxed2fAddress3bSI29V:
Q43org5jnode2vm6Unsafe23setChars2e28Lorg2fvmmagic2funboxed2fAddress3bCI29V:
	push edi
	mov edi,[esp+16]	; memPtr
	mov eax,[esp+12] 	; value
	mov ecx,[esp+8]		; count
	rep stosw
	pop edi
	ret 12

; void setInts(Address memPtr, int value, int count);
; void setFloats(Address memPtr, float value, int count);
; void setObjects(Address memPtr, Object value, int count);
Q43org5jnode2vm6Unsafe23setInts2e28Lorg2fvmmagic2funboxed2fAddress3bII29V:
Q43org5jnode2vm6Unsafe23setFloats2e28Lorg2fvmmagic2funboxed2fAddress3bFI29V:
Q43org5jnode2vm6Unsafe23setObjects2e28Lorg2fvmmagic2funboxed2fAddress3bLjava2flang2fObject3bI29V:
	push edi
	mov edi,[esp+16]	; memPtr
	mov eax,[esp+12] 	; value
	mov ecx,[esp+8]		; count
	rep stosd
	pop edi
	ret 12

; void setInts24(Address memPtr, int value, int count);
Q43org5jnode2vm6Unsafe23setInts242e28Lorg2fvmmagic2funboxed2fAddress3bII29V:
	push edi
	mov edi,[esp+16]	; memPtr
	mov eax,[esp+12] 	; value
	mov ecx,[esp+8]		; count
	and eax,0xFFFFFF	; Mask of high 8-bits
set24_loop:
	mov edx,[edi]
	and edx,0xFF000000
	or edx,eax
	mov [edi],edx
	lea edi,[edi+3]
	loop set24_loop
	pop edi
	ret 12

; void setLongs(Address memPtr, long value, int count);
; void setDoubles(Address memPtr, double value, int count);
Q43org5jnode2vm6Unsafe23setLongs2e28Lorg2fvmmagic2funboxed2fAddress3bJI29V:
Q43org5jnode2vm6Unsafe23setDoubles2e28Lorg2fvmmagic2funboxed2fAddress3bDI29V:
	push edi
	mov edi,[esp+20]	; memPtr
	mov edx,[esp+16] 	; value MSB
	mov eax,[esp+12] 	; value LSB
	mov ecx,[esp+8]		; count
	test ecx,0xFFFFFFFF	; (count == 0) ??
	jz set64_end
set64_loop;
	stosd				; LSB
	xchg eax,edx		; Swap LSB,MSB
	stosd				; MSB
	xchg eax,edx		; Swap LSB,MSB
	loop set64_loop
set64_end:	
	pop edi
	ret 16
	
		align 4096

	