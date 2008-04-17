; -----------------------------------------------
; $Id: console.h 1661 2005-05-21 07:51:46Z epr $
;
; Kernel console macro's
;
; Author       : E.Prangsma 
; -----------------------------------------------
  
; Copy a memory block
%macro COPY_MEMORY 3
	mov ASI,%1		; srcMemPtr
	mov ADI,%2		; destMemPtr
	mov ACX,%3		; size
	
	; Test the direction for copying
	cmp ASI,ADI
	jl copy_reverse
	
	; Copy from left to right
	cld
copy_bytes:
	test ACX,3			; Test for size multiple of 4-byte 
	jz copy_dwords
	dec ACX
	movsb				; Not yet multiple of 4, copy a single byte and test again
	jmp copy_bytes	
copy_dwords:
	shr ACX,2
	rep movsd
	jmp copy_done
	
copy_reverse:
	; Copy from right to left
	pushf
	cli
	std
	lea ASI,[ASI+ACX-1]
	lea ADI,[ADI+ACX-1]
	rep movsb
	popf	
	
copy_done:
%endmacro
