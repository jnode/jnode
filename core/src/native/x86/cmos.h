; -----------------------------------------------
; $Id: i386.h,v 1.6 2003/11/24 07:49:11 epr Exp $
;
; CMOS macros
;
; Author       : E.Prangsma 
; -----------------------------------------------

; CMOS_WRITE address, value
%macro CMOS_WRITE 2
	push AAX
	mov eax,%1
	out 0x70,al
	mov eax,%2
	out 0x71,al
	pop AAX
%endmacro

