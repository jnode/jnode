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

; void andByte(VmAddress memPtr, byte value, int count);
Q43org5jnode2vm6Unsafe23andByte2e28Lorg2fjnode2fvm2fVmAddress3bBI29V:
	BINOP32_VALUES and, byte, 1, dl

; void andShort(VmAddress memPtr, short value, int count);
; void andChar(VmAddress memPtr, char value, int count);
Q43org5jnode2vm6Unsafe23andShort2e28Lorg2fjnode2fvm2fVmAddress3bSI29V:
Q43org5jnode2vm6Unsafe23andChar2e28Lorg2fjnode2fvm2fVmAddress3bCI29V:
	BINOP32_VALUES and, word, 2, dx

; void andInt24(VmAddress memPtr, int value, int count);
Q43org5jnode2vm6Unsafe23andInt242e28Lorg2fjnode2fvm2fVmAddress3bII29V:
	BINOP24_VALUES and, or, 0xFF000000

; void andInt(VmAddress memPtr, int value, int count);
Q43org5jnode2vm6Unsafe23andInt2e28Lorg2fjnode2fvm2fVmAddress3bII29V:
	BINOP32_VALUES and, dword, 4, edx

; void andLong(VmAddress memPtr, long value, int count);
Q43org5jnode2vm6Unsafe23andLong2e28Lorg2fjnode2fvm2fVmAddress3bJI29V:
	BINOP64_VALUES and
	
; -----------------------------------------------
; OR
; -----------------------------------------------

; void orByte(VmAddress memPtr, byte value, int count);
Q43org5jnode2vm6Unsafe23orByte2e28Lorg2fjnode2fvm2fVmAddress3bBI29V:
	BINOP32_VALUES or, byte, 1, dl

; void orShort(VmAddress memPtr, short value, int count);
; void orChar(VmAddress memPtr, char value, int count);
Q43org5jnode2vm6Unsafe23orShort2e28Lorg2fjnode2fvm2fVmAddress3bSI29V:
Q43org5jnode2vm6Unsafe23orChar2e28Lorg2fjnode2fvm2fVmAddress3bCI29V:
	BINOP32_VALUES or, word, 2, dx

; void orInt24(VmAddress memPtr, int value, int count);
Q43org5jnode2vm6Unsafe23orInt242e28Lorg2fjnode2fvm2fVmAddress3bII29V:
	BINOP24_VALUES or, and, 0x00FFFFFF

; void orInt(VmAddress memPtr, int value, int count);
Q43org5jnode2vm6Unsafe23orInt2e28Lorg2fjnode2fvm2fVmAddress3bII29V:
	BINOP32_VALUES or, dword, 4, edx

; void orLong(VmAddress memPtr, long value, int count);
Q43org5jnode2vm6Unsafe23orLong2e28Lorg2fjnode2fvm2fVmAddress3bJI29V:
	BINOP64_VALUES or
	
; -----------------------------------------------
; XOR
; -----------------------------------------------

; void xorByte(VmAddress memPtr, byte value, int count);
Q43org5jnode2vm6Unsafe23xorByte2e28Lorg2fjnode2fvm2fVmAddress3bBI29V:
	BINOP32_VALUES xor, byte, 1, dl

; void xorShort(VmAddress memPtr, short value, int count);
; void xorChar(VmAddress memPtr, char value, int count);
Q43org5jnode2vm6Unsafe23xorShort2e28Lorg2fjnode2fvm2fVmAddress3bSI29V:
Q43org5jnode2vm6Unsafe23xorChar2e28Lorg2fjnode2fvm2fVmAddress3bCI29V:
	BINOP32_VALUES xor, word, 2, dx

; void xorInt24(VmAddress memPtr, int value, int count);
Q43org5jnode2vm6Unsafe23xorInt242e28Lorg2fjnode2fvm2fVmAddress3bII29V:
	BINOP24_VALUES xor, and, 0x00FFFFFF

; void xorInt(VmAddress memPtr, int value, int count);
Q43org5jnode2vm6Unsafe23xorInt2e28Lorg2fjnode2fvm2fVmAddress3bII29V:
	BINOP32_VALUES xor, dword, 4, edx

; void xorLong(VmAddress memPtr, long value, int count);
Q43org5jnode2vm6Unsafe23xorLong2e28Lorg2fjnode2fvm2fVmAddress3bJI29V:
	BINOP64_VALUES xor
	
	