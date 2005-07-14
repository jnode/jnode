; -----------------------------------------------
; $Id: i386.h,v 1.6 2003/11/24 07:49:11 epr Exp $
;
; System call constants
;
; Author       : E.Prangsma 
; -----------------------------------------------

SYSCALL_INT		equ 0x32

%macro SYSCALL 1
	push AAX
	mov AAX,%1
	int SYSCALL_INT
	pop AAX
%endmacro

; System call commands
; Passed in register EAX
SC_DISABLE_PAGING	equ 0x00
SC_ENABLE_PAGING	equ 0x01
SC_SYNC_MSRS		equ 0x02
SC_SAVE_MSRS		equ 0x03
SC_RESTORE_MSRS		equ 0x04

SC_MAX				equ SC_RESTORE_MSRS


