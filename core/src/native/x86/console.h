; -----------------------------------------------
; $Id$
;
; Kernel console macro's
;
; Author       : E.Prangsma 
; -----------------------------------------------
  
; Print an ASCII character given as parameter
%macro PRINT_CHAR 1
	push eax
	mov eax,%1
%ifdef BITS32	
	call sys_print_char
%else
	%ifdef BITS64_ON
		call sys_print_char
	%else
		call sys_print_char32
	%endif
%endif
	pop eax
%endmacro

; Print an ASCII string given as parameter
%macro PRINT_STR 1
%ifdef BITS32
	push eax
	mov eax,%1
	call sys_print_str
	pop eax
%else
	%ifdef BITS64_ON
		push rax
		mov rax,%1
		call sys_print_str
		pop rax
	%else
		push eax
		mov eax,%1
		call sys_print_str32
		pop eax
	%endif
%endif	
%endmacro

; Print a word given as parameter
%macro PRINT_WORD 1
%ifdef BITS32
	push eax
	mov eax,%1
	call sys_print_eax
	pop eax
%else
	%ifdef BITS64_ON
		push rax
		mov	rax,%1
		call sys_print_rax
		pop rax
	%else
		push eax
		mov eax,%1
		call sys_print_eax32
		pop eax
	%endif
%endif
%endmacro
