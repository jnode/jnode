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
	mov AAX,GET_OLD_EAX
	cmp AAX,SC_MAX
	ja stub_syscallHandler_ret
	call WORD [AAX*SLOT_SIZE + syscalls]
stub_syscallHandler_ret:	
	int_exit

syscalls:
	DA	disable_paging
	DA	enable_paging
	DA	sc_SyncMSRs
	DA	sc_SaveMSRs
	DA	sc_RestoreMSRs

sc_SyncMSRs:
	mov ADI,CURRENTTHREAD
	SAVE_MSR_ARRAY [ADI+VmX86Thread_READWRITEMSRS_OFS]
	RESTORE_MSR_ARRAY [ADI+VmX86Thread_READWRITEMSRS_OFS]
	RESTORE_MSR_ARRAY [ADI+VmX86Thread_WRITEONLYMSRS_OFS]
	ret
	
sc_SaveMSRs:
	mov ADI,CURRENTTHREAD
	SAVE_MSR_ARRAY [ADI+VmX86Thread_READWRITEMSRS_OFS]
	ret
	
sc_RestoreMSRs:
	mov ADI,CURRENTTHREAD
	RESTORE_MSR_ARRAY [ADI+VmX86Thread_READWRITEMSRS_OFS]
	RESTORE_MSR_ARRAY [ADI+VmX86Thread_WRITEONLYMSRS_OFS]
	ret	
	