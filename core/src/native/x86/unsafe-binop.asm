; -----------------------------------------------
; $Id$
;
; Native method implementation for org.jnode.vm.Unsafe
; of the binary operations.
;
; Author       : E. Prangsma
; -----------------------------------------------

; BINOP24_VALUES opcode, mask-opcode, mask
%macro BINOP24_VALUES 3
    push edi
	mov edi,[esp+12]	; memPtr
	mov eax,[esp+8] 	; value
	mov ecx,[esp+4]		; count
	jecxz %%end			; (Count == 0) ?
	%2 eax,%3	     	; Apply Mask 
%%loop:
    %1 dword [edi],eax
    lea edi,[edi+3]
	loop %%loop
%%end:
	pop edi
	ret 12
%endmacro

; BINOP32_VALUES opcode, type, size, reg
%macro BINOP32_VALUES 4
	mov eax,[esp+12]	; memPtr
	mov edx,[esp+8] 	; value
	mov ecx,[esp+4]		; count
	jecxz %%end			; (Count == 0) ?
%%loop:
	%1 %2 [eax],%4
	add eax,%3
	loop %%loop
%%end:
	ret 12
%endmacro

; BINOP64_VALUES opcode
%macro BINOP64_VALUES 1
	push edi
	mov eax,[esp+16]	; memPtr
	mov edx,[esp+12] 	; value MSB
	mov eax,[esp+8] 	; value LSB
	mov ecx,[esp+4]		; count
	jecxz %%end			; (Count == 0) ?
%%loop:
	%1 dword [edi],eax	; LSB
	%1 dword [edi+4],edx; MSB
	add edi,8
	loop %%loop
%%end:
	pop edi
	ret 16
%endmacro

; -----------------------------------------------
; AND
; -----------------------------------------------

; void andByte(Address memPtr, byte value, int count);
Q43org5jnode2vm6Unsafe23andByte2e28Lorg2fvmmagic2funboxed2fAddress3bBI29V:
	BINOP32_VALUES and, byte, 1, dl

; void andShort(Address memPtr, short value, int count);
; void andChar(Address memPtr, char value, int count);
Q43org5jnode2vm6Unsafe23andShort2e28Lorg2fvmmagic2funboxed2fAddress3bSI29V:
Q43org5jnode2vm6Unsafe23andChar2e28Lorg2fvmmagic2funboxed2fAddress3bCI29V:
	BINOP32_VALUES and, word, 2, dx

; void andInt24(Address memPtr, int value, int count);
Q43org5jnode2vm6Unsafe23andInt242e28Lorg2fvmmagic2funboxed2fAddress3bII29V:
	BINOP24_VALUES and, or, 0xFF000000

; void andInt(Address memPtr, int value, int count);
Q43org5jnode2vm6Unsafe23andInt2e28Lorg2fvmmagic2funboxed2fAddress3bII29V:
	BINOP32_VALUES and, dword, 4, edx

; void andLong(Address memPtr, long value, int count);
Q43org5jnode2vm6Unsafe23andLong2e28Lorg2fvmmagic2funboxed2fAddress3bJI29V:
	BINOP64_VALUES and
	
; -----------------------------------------------
; OR
; -----------------------------------------------

; void orByte(Address memPtr, byte value, int count);
Q43org5jnode2vm6Unsafe23orByte2e28Lorg2fvmmagic2funboxed2fAddress3bBI29V:
	BINOP32_VALUES or, byte, 1, dl

; void orShort(Address memPtr, short value, int count);
; void orChar(Address memPtr, char value, int count);
Q43org5jnode2vm6Unsafe23orShort2e28Lorg2fvmmagic2funboxed2fAddress3bSI29V:
Q43org5jnode2vm6Unsafe23orChar2e28Lorg2fvmmagic2funboxed2fAddress3bCI29V:
	BINOP32_VALUES or, word, 2, dx

; void orInt24(Address memPtr, int value, int count);
Q43org5jnode2vm6Unsafe23orInt242e28Lorg2fvmmagic2funboxed2fAddress3bII29V:
	BINOP24_VALUES or, and, 0x00FFFFFF

; void orInt(Address memPtr, int value, int count);
Q43org5jnode2vm6Unsafe23orInt2e28Lorg2fvmmagic2funboxed2fAddress3bII29V:
	BINOP32_VALUES or, dword, 4, edx

; void orLong(Address memPtr, long value, int count);
Q43org5jnode2vm6Unsafe23orLong2e28Lorg2fvmmagic2funboxed2fAddress3bJI29V:
	BINOP64_VALUES or
	
; -----------------------------------------------
; XOR
; -----------------------------------------------

; void xorByte(Address memPtr, byte value, int count);
Q43org5jnode2vm6Unsafe23xorByte2e28Lorg2fvmmagic2funboxed2fAddress3bBI29V:
	BINOP32_VALUES xor, byte, 1, dl

; void xorShort(Address memPtr, short value, int count);
; void xorChar(Address memPtr, char value, int count);
Q43org5jnode2vm6Unsafe23xorShort2e28Lorg2fvmmagic2funboxed2fAddress3bSI29V:
Q43org5jnode2vm6Unsafe23xorChar2e28Lorg2fvmmagic2funboxed2fAddress3bCI29V:
	BINOP32_VALUES xor, word, 2, dx

; void xorInt24(Address memPtr, int value, int count);
Q43org5jnode2vm6Unsafe23xorInt242e28Lorg2fvmmagic2funboxed2fAddress3bII29V:
	BINOP24_VALUES xor, and, 0x00FFFFFF

; void xorInt(Address memPtr, int value, int count);
Q43org5jnode2vm6Unsafe23xorInt2e28Lorg2fvmmagic2funboxed2fAddress3bII29V:
	BINOP32_VALUES xor, dword, 4, edx

; void xorLong(Address memPtr, long value, int count);
Q43org5jnode2vm6Unsafe23xorLong2e28Lorg2fvmmagic2funboxed2fAddress3bJI29V:
	BINOP64_VALUES xor
	
	