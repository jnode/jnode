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
	call sys_print_char
	pop eax
%endmacro

; Print an ASCII string given as parameter
%macro PRINT_STR 1
	push AAX
	mov AAX,%1
	call sys_print_str
	pop AAX
%endmacro

; Print a word given as parameter
%macro PRINT_WORD 1
%ifdef BITS32
	push eax
	mov eax,%1
	call sys_print_eax
	pop eax
%else
	push rax
	mov rax,%1
	call sys_print_rax
	pop rax
%endif
%endmacro
