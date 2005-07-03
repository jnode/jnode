; -----------------------------------------------
; $Id$
;
; Native multi media methods implementation for org.jnode.vm.x86.UnsafeX86
;
; Author       : E. Prangsma
; -----------------------------------------------

V256_T8:	dd 0x01000100
			dd 0x01000100

; static void setARGB32bppMMX(Address src, Address dst, int length);
GLABEL Q53org5jnode2vm3x869UnsafeX8623setARGB32bppMMX2e28Lorg2fvmmagic2funboxed2fAddress3bLorg2fvmmagic2funboxed2fAddress3bI29V	
	push ACX

	; Setup
	mov AAX,[ASP+(4*SLOT_SIZE)]		; src (A+C)
	mov ADX,[ASP+(3*SLOT_SIZE)]		; dst (D)
	mov ecx,[ASP+(2*SLOT_SIZE)]		; length	
	pxor mm1,mm1					; mm1 = 00 00 00 00 00 00 00 00 
	movq mm5,qword [V256_T8]		; mm5 = 01 00 01 00 01 00 01 00
	
setARGB32bppMMX_Loop:	
	movd mm2,dword [AAX]			; mm2 = 0 0 0 0 A R G B (C)
	movd mm3,dword [ADX]			; mm3 = 0 0 0 0 0 R G B (D)
	
	punpcklbw mm2,mm1		 		; mm2 = 0 A 0 R 0 G 0 B (C)
	punpcklbw mm3,mm1				; mm3 = 0 0 0 R 0 G 0 B (D)
	
	pshufw mm4,mm2,0xFF				; mm4 = 0 A 0 A 0 A 0 A	
	movq mm6,mm3					; mm6 = mm3 = D

	pcmpgtw mm6,mm2					; mm6 = all 1's if D > C, 0 otherwise
	movq mm7,mm5					; mm7 = mm5 = V256_T8
	
	pand mm7,mm6					; mm7 = V256_T8 if D > C, 0 otherwise
	paddw mm2,mm7					; C + mm7
	
	psubw mm2,mm3					; (C + mm7) - D
	pmullw mm2,mm4					; A * ((C + mm7) - D)
	
	psrlw mm2,8						; (A * ((C + mm7) - D)) / 256
	paddw mm2,mm3					; ((A * ((C + mm7) - D)) / 256) + D
	
	pand mm4,mm6					; mm4 = A if D > C, 0 otherwise

	psubw mm2,mm4					; ((A * ((C + mm7) - D)) / 256) + D - A
	packuswb mm2,mm2				; mm2 = 0 0 0 0 0 R G B
	
	movd dword [ADX],mm2
	lea ADX,[ADX+4]
	lea AAX,[AAX+4]
	dec ecx
	jnz setARGB32bppMMX_Loop
	
	; Cleanup
	emms

	pop ACX
	ret SLOT_SIZE*3

