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
	push eax
	mov eax,%1
	call sys_print_str
	pop eax
%endmacro

; Print a word given as parameter
%macro PRINT_WORD 1
	push eax
	mov eax,%1
	call sys_print_eax
	pop eax
%endmacro
