; -----------------------------------------------
; $Id$
;
; Native method implementation for org.jnode.vm.Unsafe
;
; Author       : E. Prangsma
; -----------------------------------------------

	extern VmThread_runThread
	extern bootHeapStart
	extern bootHeapEnd
	extern freeMemoryStart

Q43org5jnode2vm6Unsafe23pushInt2e28I29V:
Q43org5jnode2vm6Unsafe23pushLong2e28J29V:
Q43org5jnode2vm6Unsafe23pushObject2e28Ljava2flang2fObject3b29V:
	ret

Q43org5jnode2vm6Unsafe23getSuperClasses2e28Ljava2flang2fObject3b295bLorg2fjnode2fvm2fclassmgr2fVmType3b:
	mov eax,[esp+4] ; object reference
	mov eax,[eax+ObjectLayout_TIB_SLOT*4] ; TIB reference
	mov eax,[eax+(TIBLayout_SUPERCLASSES_INDEX+VmArray_DATA_OFFSET)*4] 
	ret 4

Q43org5jnode2vm6Unsafe23clear2e28Lorg2fvmmagic2funboxed2fAddress3bLorg2fvmmagic2funboxed2fExtent3b29V:
	mov edx,[esp+8] ; memPtr
	mov eax,[esp+4] ; Size
	push edi
	push ecx
	mov edi,edx
	mov ecx,eax
	shr ecx,2
	xor eax,eax
	cld
	rep stosd
	pop ecx
	pop edi
	ret 8

Q43org5jnode2vm6Unsafe23copy2e28Lorg2fvmmagic2funboxed2fAddress3bLorg2fvmmagic2funboxed2fAddress3bI29V:
	push ebx
	mov ebx,esp
	push edi
	push esi
	push ecx
	
	mov esi,[ebx+16] ; srcMemPtr
	mov edi,[ebx+12] ; destMemPtr
	mov ecx,[ebx+8]  ; size
	
	%if 0
		push eax
		mov eax,esi
		call sys_print_eax
		mov eax,edi
		call sys_print_eax
		mov eax,ecx
		call sys_print_eax
		pop eax
	%endif
	
	; Test the direction for copying
	cmp esi,edi
	jl copy_reverse
	
	; Copy from left to right
	cld
copy_bytes:
	test ecx,3			; Test for size multiple of 4-byte 
	jz copy_dwords
	dec ecx
	movsb				; Not yet multiple of 4, copy a single byte and test again
	jmp copy_bytes	
copy_dwords:
	shr ecx,2
	rep movsd
	jmp copy_done
	
copy_reverse:
	; Copy from right to left
	pushf
	cli
	std
	lea esi,[esi+ecx-1]
	lea edi,[edi+ecx-1]
	rep movsb
	popf	
	
copy_done:
	pop ecx
	pop esi
	pop edi
	mov esp,ebx
	pop ebx
	ret 12
	
Q43org5jnode2vm6Unsafe23inPortByte2e28I29I:
	mov edx,[esp+4] ; portNr
	in al,dx
	movzx eax,al
	ret 4
	
Q43org5jnode2vm6Unsafe23inPortWord2e28I29I:
	mov edx,[esp+4] ; portNr
	in ax,dx
	movzx eax,ax
	ret 4
	
Q43org5jnode2vm6Unsafe23inPortDword2e28I29I:
	mov edx,[esp+4] ; portNr
	in eax,dx
	ret 4
	
Q43org5jnode2vm6Unsafe23outPortByte2e28II29V:
	mov eax,[esp+4] ; value
	mov edx,[esp+8] ; portNr
	out dx,al
	ret 8

Q43org5jnode2vm6Unsafe23outPortWord2e28II29V:
	mov eax,[esp+4] ; value
	mov edx,[esp+8] ; portNr
	out dx,ax
	ret 8
	
Q43org5jnode2vm6Unsafe23outPortDword2e28II29V:
	mov eax,[esp+4] ; value
	mov edx,[esp+8] ; portNr
	out dx,eax
	ret 8

Q43org5jnode2vm6Unsafe23idle2e2829V:
	sti
	hlt
	nop
	nop
	ret

Q43org5jnode2vm6Unsafe23die2e2829V:
	cli
	hlt
	nop
	nop
	ret

; invoke(method)
Q43org5jnode2vm6Unsafe23invokeVoid2e28Lorg2fjnode2fvm2fclassmgr2fVmMethod3b29V:	
Q43org5jnode2vm6Unsafe23invokeInt2e28Lorg2fjnode2fvm2fclassmgr2fVmMethod3b29I:	
Q43org5jnode2vm6Unsafe23invokeLong2e28Lorg2fjnode2fvm2fclassmgr2fVmMethod3b29J:	
Q43org5jnode2vm6Unsafe23invokeObject2e28Lorg2fjnode2fvm2fclassmgr2fVmMethod3b29Ljava2flang2fObject3b:
	pop eax 			; Get return address
	xchg eax,[esp+0]	; EAX now contains method argument, Top of stack now contains return address
	jmp [eax+VmMethod_NATIVECODE_OFS]

; int Unsafe.initThread(VmThread thread, Object newStack, int stackSize)
; Initialize the new Thread.
Q43org5jnode2vm6Unsafe23initThread2e28Lorg2fjnode2fvm2fVmThread3bLjava2flang2fObject3bI29V:
	push ebx
	mov ecx,[esp+16]	; newThread
	mov edx,[esp+12]	; newStack
	mov ebx,[esp+8]		; stack size

	; Setup bound stackoverflow check area
	mov [edx+0], edx	; Low == stack + (sizeof bound area) + STACK_OVERFLOW_LIMIT
	add dword [edx+0],(VmThread_STACK_OVERFLOW_LIMIT+8)
	mov [edx+4], edx	; High == stack + stacksize
	add [edx+4], ebx

	pusha
	pushf	
	cli
	add ebx,edx			; stackptr -> ebx
	mov edx,esp 		; Save current esp
	mov esp,ebx
	xor ebx,ebx
	xor ebp,ebp 		; Clear the frame ptr
	push ebp 			; previous EBP
	push ebp		    ; MAGIC    (here invalid ON PURPOSE!)
	push ebp		   	; PC       (here invalid ON PURPOSE!)
	push ebp		    ; VmMethod (here invalid ON PURPOSE!)
	mov ebp,esp
	push ebx ; Dummy
	push ebx ; Dummy
	push ecx ; newThread	; Objectref for runThread
	push ebx ; Dummy		; method "return address"
	
	mov eax,VmThread_runThread
	mov [ecx+VmX86Thread_EAX_OFS],eax 		; runThread method
	mov [ecx+VmX86Thread_EBP_OFS],ebp
	mov eax,[eax+VmMethod_NATIVECODE_OFS]
	mov [ecx+VmX86Thread_EIP_OFS],eax
	mov [ecx+VmX86Thread_ESP_OFS], esp		; Save current esp
	pushf
	pop eax
	or eax,F_IF
	mov [ecx+VmX86Thread_EFLAGS_OFS],eax	; Setup EFLAGS
	
	mov esp,edx ; Restore esp
	popf		; Re-enable interrupts
	popa
	pop ebx
	ret 12
	
initThread_msg1: db 'New esp=',0

; public static native Address getMaxAddress()
Q43org5jnode2vm6Unsafe23getMaxAddress2e2829Lorg2fvmmagic2funboxed2fAddress3b:
	mov eax,0xFFC00000	; 4Gb - 4Mb
	ret
	
; public static native VmAddress getMinAddress()
Q43org5jnode2vm6Unsafe23getMinAddress2e2829Lorg2fvmmagic2funboxed2fAddress3b:
	mov eax,free_paddr
	ret
	
; public static native Address getMemoryStart()
Q43org5jnode2vm6Unsafe23getMemoryStart2e2829Lorg2fvmmagic2funboxed2fAddress3b:
	mov eax,[free_mem_start]
	;; mov eax,freeMemoryStart
	ret
	
; public static native Address getMemoryEnd()
Q43org5jnode2vm6Unsafe23getMemoryEnd2e2829Lorg2fvmmagic2funboxed2fAddress3b:
	mov eax,[mem_size]
	ret
	
; public static native Address getKernelStart()
Q43org5jnode2vm6Unsafe23getKernelStart2e2829Lorg2fvmmagic2funboxed2fAddress3b:
	mov eax,kernel_begin
	ret
	
; public static native Address getKernelEnd()
Q43org5jnode2vm6Unsafe23getKernelEnd2e2829Lorg2fvmmagic2funboxed2fAddress3b:
	mov eax,vm_start
	ret
	
; public static native Address getInitJarStart()
Q43org5jnode2vm6Unsafe23getInitJarStart2e2829Lorg2fvmmagic2funboxed2fAddress3b:
	mov eax,[initJar_start]
	ret
	
; public static native Address getInitJarEnd()
Q43org5jnode2vm6Unsafe23getInitJarEnd2e2829Lorg2fvmmagic2funboxed2fAddress3b:
	mov eax,[initJar_end]
	ret
	
; public static native Address getBootHeapStart()
Q43org5jnode2vm6Unsafe23getBootHeapStart2e2829Lorg2fvmmagic2funboxed2fAddress3b:
	mov eax,bootHeapStart
	ret
	
; public static native Address getBootHeapEnd()
Q43org5jnode2vm6Unsafe23getBootHeapEnd2e2829Lorg2fvmmagic2funboxed2fAddress3b:
	mov eax,bootHeapEnd
	ret
	
; Gets information of the JNode kernel command line.
; @param destination If non-null, the commandline is copied into this array.
; @return The maximum length of the command line
; protected static native int getCmdLine(byte[] destination);
Q43org5jnode2vm6Unsafe23getCmdLine2e285bB29I:	
	mov eax,[esp+4]		; destination
	test eax,eax		; Is null?
	jz after_copyCmdLine
	mov ecx,[eax+(VmArray_LENGTH_OFFSET*4)]
	push esi
	push edi
	mov esi,multiboot_cmdline
	lea edi,[eax+(VmArray_DATA_OFFSET*4)]
	cld
	rep	movsb
	pop edi
	pop esi
after_copyCmdLine:
	mov eax,MBI_CMDLINE_MAX
	ret 4
	
; Gets the current processor
Q43org5jnode2vm6Unsafe23getCurrentProcessor2e2829Lorg2fjnode2fvm2fVmProcessor3b:
	;mov eax,vmCurProcessor
	mov eax,CURRENTPROCESSOR
	ret	

; Force a yieldpoint
Q43org5jnode2vm6Unsafe23yieldPoint2e2829V:
	; Is a switch required?
	cmp THREADSWITCHINDICATOR,VmProcessor_TSI_SWITCH_REQUESTED
	jne noYieldPoint
	int 0x30
noYieldPoint:
	ret	

; Address getJumpTable0()	
Q43org5jnode2vm6Unsafe23getJumpTable02e2829Lorg2fvmmagic2funboxed2fAddress3b:
	mov eax,vm_jumpTable
	ret
	
; public static native void debug(String str);
Q43org5jnode2vm6Unsafe23debug2e28Ljava2flang2fString3b29V:
	mov eax,[esp+4]
	call vm_print_string
	ret 4
	
; public static native void debug(char value);
Q43org5jnode2vm6Unsafe23debug2e28C29V:
	mov eax,[esp+4]
	call sys_print_char
	ret 4

; public static native void debug(int value);
Q43org5jnode2vm6Unsafe23debug2e28I29V:
	mov eax,[esp+4]
	call sys_print_eax
	ret 4

; public static native void debug(long value);
Q43org5jnode2vm6Unsafe23debug2e28J29V:
	mov eax,[esp+8]		; MSB
	call sys_print_eax
	mov eax,[esp+4]		; LSB
	call sys_print_eax
	ret 8
		
	
	