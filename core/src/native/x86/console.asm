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

scr_width	equ 80
scr_height	equ 25
scr_addr	equ 0xb8000
hexchars: 	db '0123456789ABCDEF' 
CR		equ 0x0D
LF		equ 0x0A
video_prt_reg	equ 0x3d4  
video_prt_val	equ 0x3d5
video_mode_reg	equ 0x3d8

; Set the cursor at offset [scr_ofs]
set_cursor:
	push eax
	push edx
	mov eax,14
	mov edx,video_prt_reg
	out dx,al
	mov eax,[scr_ofs]
	shr eax,8
	mov edx,video_prt_val
	out dx,al
	mov eax,15
	mov edx,video_prt_reg
	out dx,al
	mov eax,[scr_ofs]
	mov edx,video_prt_val
	out dx,al
	pop edx
	pop eax
	ret

; Hide the cursor 
hide_cursor:
	push eax
	push edx
	mov eax,10
	mov edx,video_prt_reg
	out dx,al
	mov eax,0xFF
	mov edx,video_prt_val
	out dx,al
	pop edx
	pop eax
	ret

; Clear the screen
sys_clrscr:
	push eax
	push ecx
	push edi
	mov ecx,scr_width*scr_height
	mov edi,scr_addr
	mov eax,0x0720
	rep stosw
	mov dword [scr_ofs],0
	;call set_cursor
	call hide_cursor
	pop edi
	pop ecx
	pop eax
	ret

; Print a character in al
sys_print_char:
	cmp al,LF
	je pc_nextline
	cmp al,CR
	jne pc_normal
	jmp pc_ok
pc_nextline
	; next line
	push eax
	push ebx
	push edx
	mov eax,[scr_ofs]
	xor edx,edx
	mov ebx,scr_width
	div ebx
	sub [scr_ofs],edx
	add dword [scr_ofs],scr_width
	pop edx
	pop ebx
	pop eax
	jmp pc_check_eos
pc_normal:
	push edi
	mov edi,[scr_ofs]
	shl edi,1
	add edi,scr_addr
	mov ah,0x70 ; Color: black on white background
	mov [edi],ax
	inc dword [scr_ofs]
	pop edi
pc_check_eos:
	cmp dword [scr_ofs],(scr_width*scr_height)
	jne pc_ok
	; Scroll up
	push edi
	push esi
	push ecx
	mov edi,scr_addr
	mov esi,edi
	add esi,(scr_width*2)
	mov ecx,(scr_width*(scr_height-1))
	rep movsw
	; Now clear the last row
	push eax
	mov edi,scr_addr+((scr_width*(scr_height-1))*2)
	mov ecx,scr_width
	mov eax,0x0720
	rep stosw
	pop eax
	pop ecx
	pop esi
	pop edi
	mov dword [scr_ofs],(scr_width*(scr_height-1))
pc_ok:
	call set_cursor
	ret

%macro digit 1
	mov eax,ebx
	shr eax,%1
	and eax,0x0f
	mov al,[hexchars+eax]
	call sys_print_char
%endmacro

; Print a value in EAX (in hex format)
sys_print_eax:
	push eax
	push ebx
	mov ebx,eax
	digit 28
	digit 24
	digit 20
	digit 16
	digit 12
	digit 8
	digit 4
	digit 0
	mov al,' '
	call sys_print_char
	pop ebx
	pop eax
	ret

; Print a value in AL (in hex format)
sys_print_al:
	push eax
	push ebx
	mov ebx,eax
	digit 4
	digit 0
	mov al,' '
	call sys_print_char
	pop ebx
	pop eax
	ret

; Print a null terminated string pointed to by EAX
sys_print_str:
	push esi
	mov esi,eax
ps_lp:
	mov al,[esi]
	cmp al,0
	je ps_ready
	inc esi
	call sys_print_char
	jmp ps_lp
ps_ready:
	pop esi
	ret
