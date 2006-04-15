; -----------------------------------------------
; $Id: ap-boot.asm,v 1.1 2004/05/31 08:51:39 epr Exp $
;
; Multiprocessor lock
;
; Author       : E. Prangsma
; -----------------------------------------------

%macro SPINLOCK 1
%1: DA 0
%endmacro

%macro SPINLOCK_ENTER 1
	push AAX
%%tryLock:	
	mov AAX,1
	lock xchg AAX,WORD [%1]
	test AAX,AAX
	jnz %%tryLock
	pop AAX
%endmacro

%macro SPINLOCK_EXIT 1
	push AAX
	xor AAX,AAX
	mov WORD [%1],AAX
	pop AAX
%endmacro
	
; Jump to the given target if the spinlock is locked
; args: lock, target
%macro SPINLOCK_JUMP_IF_LOCKED 2
	push AAX
	mov AAX,WORD [%1]
	test AAX,AAX
	pop AAX
	jnz %2
%endmacro
