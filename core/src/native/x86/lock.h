; -----------------------------------------------
; $Id: ap-boot.asm,v 1.1 2004/05/31 08:51:39 epr Exp $
;
; Multiprocessor lock
;
; Author       : E. Prangsma
; -----------------------------------------------

%macro SPINLOCK 1
%1: dd 0
%endmacro

%macro SPINLOCK_ENTER 1
	push eax
%%tryLock:	
	mov eax,1
	lock xchg eax,dword [%1]
	test eax,eax
	jnz %%tryLock
	pop eax
%endmacro

%macro SPINLOCK_EXIT 1
	push eax
	xor eax,eax
	mov dword [%1],eax
	pop eax
%endmacro
	