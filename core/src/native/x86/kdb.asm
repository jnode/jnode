; -----------------------------------------------
; $Id$
;
; Kernel low level debugger via serial port functions
;
; Author       : E.Prangsma 
; -----------------------------------------------
  
%macro IODELAY 0
	jmp %%l1
%%l1:
	jmp %%l2
%%l2:		
%endmacro  
  
%macro KDB_INIT_FUNCTION 0
; Initialize serial port communication
kdb_init:
	push eax
	push ecx
	push edx
	push esi
	
	mov eax,0x400				; BIOS data area
	movzx edx,word [eax+0x00]	; Get port address of COM1
	mov [kdb_port],edx
	
	mov edx,[kdb_port]
	add edx,4					; Modem control
	mov eax,0x03				; enable Request to Send & Data Terminal Ready
	out dx,al
	
	; Find ' KBD' on commandline
	mov ecx,MBI_CMDLINE_MAX
	mov esi,multiboot_cmdline
	mov eax,' kdb'
%%find_loop:
	cmp [esi],eax
	je %%found_kdb
	inc esi
	loop %%find_loop
	jmp %%ret
	
%%found_kdb:
	inc dword [kdb_enabled]
	
%%ret:	
	pop esi
	pop edx
	pop ecx
	pop eax
	ret  
%endmacro
  
%macro KDB_SEND_FUNCTION 1  
; Send a character in AL to the serial port
; If serial port output is enabled.  
kdb_send_char%1:
	push ABX
	push ADX
	test byte [kdb_enabled],0x1
	jz %%ret
	
	mov ebx,eax			; Save character
	mov edx,[kdb_port]	
	add edx,5
%%wait_loop:
	inc edx				; Modem status register
	in al, dx	
	IODELAY
	test al,0x80		; Remote problem?
;	jz %%remote_error (ignore for now)
	dec edx				; Line status register
	in al,dx
	IODELAY
	test al,0x20		; xmit holding reg ready?
	jz %%wait_loop
	
	mov edx,[kdb_port]
	mov eax,ebx
	out dx,al           ; Send the character
	IODELAY
	jmp %%ret
	
%%remote_error:
	
%%ret:
	pop ADX
	pop ABX
	ret

%endmacro

%macro KDB_RECV_FUNCTION 0  
; Receive a character in AAX from the serial port
; If serial port output is enabled.  
; Return: The received character, or -1 if no data is available.
kdb_recv_char:
	push ADX
	xor AAX,AAX
	test byte [kdb_enabled],0x1
	jz %%nodata
	
	mov edx,[kdb_port]	
	add edx,5			; Line status register
	in al, dx			
	test al,1			; Any received byte?
	jz %%nodata
	IODELAY
	sub edx,5			; Data register
	in al,dx
	jmp %%ret
	
%%nodata:
	xor AAX,AAX
	dec AAX	
	
%%ret:
	pop ADX
	ret

%endmacro

%ifdef BITS32
	KDB_INIT_FUNCTION
	KDB_SEND_FUNCTION 32
	KDB_RECV_FUNCTION
%else
	%undef BITS64
	%define BITS32
	bits 32
	%include "i386_bits.h"
	KDB_INIT_FUNCTION
	KDB_SEND_FUNCTION 32
	%undef BITS32
	%define BITS64
	bits 64
	%include "i386_bits.h"
	KDB_SEND_FUNCTION 64
	KDB_RECV_FUNCTION
%endif
		