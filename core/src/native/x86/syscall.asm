; -----------------------------------------------
; $Id$
;
; System call
;
; Author       : E.. Prangsma
; -----------------------------------------------

stub_syscallHandler:
	push dword 0		; Error code
	push dword 0		; INTNO (not relevant)
	push dword 0		; Handler (not relevant)
	int_entry
	mov ebp,esp
	call syscallHandler
	int_exit

syscallHandler:
	mov eax,[ebp+OLD_EAX]
	cmp eax,SC_DISABLE_PAGING
	je disable_paging
	cmp eax,SC_ENABLE_PAGING
	je enable_paging
	ret
	
