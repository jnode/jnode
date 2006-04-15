; -----------------------------------------------
; $Id$
;
; Kernel console functions
;
; Author       : E.Prangsma 
; -----------------------------------------------
  
    global sys_clrscr       ; Clear the screen
    global sys_print_eax    ; Print the contents of EAX in hexadecimal format
    global sys_print_char   ; Print a single character in AL
    global sys_print_str    ; Print a null-terminated (byte) character array point by EAX

scr_width		equ 80
scr_height		equ 25
scr_addr		equ 0xb8000
CR				equ 0x0D
LF				equ 0x0A
video_prt_reg	equ 0x3d4  
video_prt_val	equ 0x3d5
video_mode_reg	equ 0x3d8

%macro digit 2
	mov AAX,ABX
	shr AAX,%1
	and AAX,0x0f
	mov al,[eax+hexchars]
	call sys_do_print_char%2
%endmacro

%macro CONSOLE_FUNCTIONS 0-1
; Set the cursor at offset [scr_ofs]
set_cursor%1:
	push AAX
	push ADX
	mov eax,14
	mov edx,video_prt_reg
	out dx,al
	mov AAX,[scr_ofs]
	shr eax,8
	mov edx,video_prt_val
	out dx,al
	mov eax,15
	mov edx,video_prt_reg
	out dx,al
	mov AAX,[scr_ofs]
	mov edx,video_prt_val
	out dx,al
	pop ADX
	pop AAX
	ret

; Hide the cursor 
hide_cursor%1:
	push AAX
	push ADX
	mov eax,10
	mov edx,video_prt_reg
	out dx,al
	mov eax,0xFF
	mov edx,video_prt_val
	out dx,al
	pop ADX
	pop AAX
	ret

; Clear the screen
sys_clrscr%1:
	SPINLOCK_ENTER console_lock
	push AAX
	push ACX
	push ADI
	mov ecx,scr_width*scr_height
	mov ADI,scr_addr
	mov eax,0x0720
	rep stosw
	mov WORD [scr_ofs],0
	call hide_cursor%1
	pop ADI
	pop ACX
	pop AAX
	SPINLOCK_EXIT console_lock
	ret

; Print a character in al
sys_print_char%1:
	SPINLOCK_ENTER console_lock
	call sys_do_print_char%1
	SPINLOCK_EXIT console_lock
	ret

; Print a character in al
; This subroutine does NOT claim locks, so you must do that first.
sys_do_print_char%1:
	cmp al,LF
	je %%pc_nextline
	cmp al,CR
	jne %%pc_normal
	jmp %%pc_ok
%%pc_nextline:
	; next line
	push AAX
	push ABX
	push ADX
	mov AAX,[scr_ofs]
	xor ADX,ADX
	mov ABX,scr_width
	div ABX
	sub [scr_ofs],ADX
	add WORD [scr_ofs],scr_width
	pop ADX
	pop ABX
	pop AAX
	jmp %%pc_check_eos
%%pc_normal:
	push ADI
	mov ADI,[scr_ofs]
	shl ADI,1
	add ADI,scr_addr
	mov ah,0x08 ; Color: gray on black background
	mov [ADI],ax
	inc WORD [scr_ofs]
	pop ADI
%%pc_check_eos:
	cmp WORD [scr_ofs],(scr_width*scr_height)
	jne %%pc_ok
	; Scroll up
	push ADI
	push ASI
	push ACX
	mov ADI,scr_addr
	mov ASI,ADI
	add ASI,(scr_width*2)
	mov ACX,(scr_width*(scr_height-1))
	rep movsw
	; Now clear the last row
	push AAX
	mov ADI,scr_addr+((scr_width*(scr_height-1))*2)
	mov ACX,scr_width
	mov eax,0x0720
	rep stosw
	pop AAX
	pop ACX
	pop ASI
	pop ADI
	mov WORD [scr_ofs],(scr_width*(scr_height-1))
%%pc_ok:
	call set_cursor%1
	call kdb_send_char%1
	ret

; Print a value in EAX (in hex format)
sys_print_eax%1:
	SPINLOCK_ENTER console_lock
	push AAX
	push ABX
	mov ebx,eax
	digit 28, %1
	digit 24, %1
	digit 20, %1
	digit 16, %1
	digit 12, %1
	digit 8, %1
	digit 4, %1
	digit 0, %1
	mov al,' '
	call sys_do_print_char%1
	pop ABX
	pop AAX
	SPINLOCK_EXIT console_lock
	ret

%ifdef BITS64
; Print a value in RAX (in hex format)
sys_print_rax%1:
	SPINLOCK_ENTER console_lock
	push AAX
	push ABX
	mov ABX,AAX
	digit 60, 64
	digit 56, 64
	digit 52, 64
	digit 48, 64
	digit 44, 64
	digit 40, 64
	digit 36, 64
	digit 32, 64
	digit 28, 64
	digit 24, 64
	digit 20, 64
	digit 16, 64
	digit 12, 64
	digit 8, 64
	digit 4, 64
	digit 0, 64
	mov al,' '
	call sys_do_print_char%1
	pop ABX
	pop AAX
	SPINLOCK_EXIT console_lock
	ret
%endif

; Print a value in AL (in hex format)
sys_print_al%1:
	SPINLOCK_ENTER console_lock
	push AAX
	push ABX
	mov ebx,eax
	digit 4, %1
	digit 0, %1
	mov al,' '
	call sys_do_print_char%1
	pop ABX
	pop AAX
	SPINLOCK_EXIT console_lock
	ret

; Print a null terminated string pointed to by EAX/RAX
sys_print_str%1:
	SPINLOCK_ENTER console_lock
	push ASI
	mov ASI,AAX
%%ps_lp:
	mov al,[ASI]
	cmp al,0
	je %%ps_ready
	inc ASI
	call sys_do_print_char%1
	jmp %%ps_lp
%%ps_ready:
	pop ASI
	SPINLOCK_EXIT console_lock
	ret
%endmacro

%ifdef BITS32
	CONSOLE_FUNCTIONS 32
%else
	%undef BITS64
	%define BITS32
	bits 32
	%include "i386_bits.h"
	CONSOLE_FUNCTIONS 32
	%undef BITS32
	%define BITS64
	bits 64
	%include "i386_bits.h"
	CONSOLE_FUNCTIONS 64
%endif
	