; -----------------------------------------------
; $Id$
;
; System call
;
; Author       : E. Prangsma
; -----------------------------------------------

%ifdef BITS32
stub_syscallHandler:
	push dword 0		; Error code
	push dword 0		; INTNO (not relevant)
	push dword 0		; Handler (not relevant)
	int_entry
	mov ebp,esp
	call syscallHandler
	int_exit
%endif

%ifdef BITS64
stub_syscallHandler:
	push qword 0		; Error code
	push qword 0		; INTNO (not relevant)
	push qword 0		; Handler (not relevant)
	int_entry
	mov rbp,rsp
	call syscallHandler
	int_exit
%endif

syscallHandler:
	cmp GET_OLD_EAX,SC_DISABLE_PAGING
	je disable_paging
	cmp GET_OLD_EAX,SC_ENABLE_PAGING
	je enable_paging
	ret
	
