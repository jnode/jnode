; -----------------------------------------------
; $Id$
;
; Kernel console macro's
;
; Author       : E.Prangsma 
; -----------------------------------------------
  
%macro PRINT_CHAR 1
	push eax
	mov eax,%1
	call sys_print_char
	pop eax
%endmacro

%macro PRINT_STR 1
	push eax
	mov eax,%1
	call sys_print_str
	pop eax
%endmacro
