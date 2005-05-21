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
%ifdef BITS64_ON
	call sys_print_char64
%else
	call sys_print_char32
%endif
	pop eax
%endmacro

; Print an ASCII string given as parameter
%macro PRINT_STR 1
%ifdef BITS64_ON
	push rax
	mov rax,%1
	call sys_print_str64
	pop rax
%else		
	push eax
	mov eax,%1
	call sys_print_str32
	pop eax
%endif	
%endmacro

; Print a word given as parameter
%macro PRINT_WORD 1
%ifdef BITS64_ON
	push rax
	mov	rax,%1
	call sys_print_rax64
	pop rax
%else		
	push eax
	mov eax,%1
	call sys_print_eax32
	pop eax
%endif
%endmacro

; Print a 32-bit int given as parameter
%macro PRINT_INT 1
%ifdef BITS64_ON
	push rax
	mov	eax,%1
	call sys_print_eax64
	pop rax
%else		
	push eax
	mov eax,%1
	call sys_print_eax32
	pop eax
%endif
%endmacro

; Clear the screen
%macro CLEAR_SCREEN 0
%ifdef BITS64_ON
	call sys_clrscr64
%else
	call sys_clrscr32
%endif		
%endmacro

%macro NEWLINE 0
	PRINT_STR newline_msg	
%endmacro
