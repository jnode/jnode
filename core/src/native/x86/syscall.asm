; -----------------------------------------------
; $Id$
;
; System call
;
; Author       : E. Prangsma
; -----------------------------------------------

stub_syscallHandler:
	push dword 0		; Error code
	push dword 0		; INTNO (not relevant)
	push dword 0		; Handler (not relevant)
	int_entry
	mov ABP,ASP
	call syscallHandler
	int_exit

syscallHandler:
	cmp GET_OLD_EAX,SC_DISABLE_PAGING
	je disable_paging
	cmp GET_OLD_EAX,SC_ENABLE_PAGING
	je enable_paging
	ret
	
