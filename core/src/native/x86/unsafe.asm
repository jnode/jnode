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

GLABEL Q43org5jnode2vm6Unsafe23pushInt2e28I29V
GLABEL Q43org5jnode2vm6Unsafe23pushLong2e28J29V
GLABEL Q43org5jnode2vm6Unsafe23pushObject2e28Ljava2flang2fObject3b29V
	ret

GLABEL Q43org5jnode2vm6Unsafe23getSuperClasses2e28Ljava2flang2fObject3b295bLorg2fjnode2fvm2fclassmgr2fVmType3b
	mov AAX,[ASP+SLOT_SIZE] ; object reference
	mov eax,[AAX+ObjectLayout_TIB_SLOT*SLOT_SIZE] ; TIB reference
	mov AAX,[AAX+(TIBLayout_SUPERCLASSES_INDEX+VmArray_DATA_OFFSET)*SLOT_SIZE] 
	ret SLOT_SIZE

; static native void clear(Address memPtr, Extent size)
GLABEL Q43org5jnode2vm6Unsafe23clear2e28Lorg2fvmmagic2funboxed2fAddress3bLorg2fvmmagic2funboxed2fExtent3b29V
	mov ADX,[ASP+(2*SLOT_SIZE)]		; memPtr
	mov AAX,[ASP+(1*SLOT_SIZE)]		; Size
	push ADI
	push ACX
	mov ADI,ADX
	mov ecx,eax
	shr ecx,2
	xor eax,eax
	cld
	rep stosd
	pop ACX
	pop ADI
	ret SLOT_SIZE*2

GLABEL Q43org5jnode2vm6Unsafe23copy2e28Lorg2fvmmagic2funboxed2fAddress3bLorg2fvmmagic2funboxed2fAddress3bI29V
	push ABX
	mov ABX,ASP
	push ADI
	push ASI
	push ACX
	
	mov ASI,[ABX+(4*SLOT_SIZE)]		; srcMemPtr
	mov ADI,[ABX+(3*SLOT_SIZE)]		; destMemPtr
	mov ecx,[ABX+(2*SLOT_SIZE)]		; size
	
	; Test the direction for copying
	cmp ASI,ADI
	jl copy_reverse
	
	; Copy from left to right
	cld
copy_bytes:
	test ACX,3			; Test for size multiple of 4-byte 
	jz copy_dwords
	dec ACX
	movsb				; Not yet multiple of 4, copy a single byte and test again
	jmp copy_bytes	
copy_dwords:
	shr ACX,2
	rep movsd
	jmp copy_done
	
copy_reverse:
	; Copy from right to left
	pushf
	cli
	std
	lea ASI,[ASI+ACX-1]
	lea ADI,[ADI+ACX-1]
	rep movsb
	popf	
	
copy_done:
	pop ACX
	pop ASI
	pop ADI
	mov ASP,ABX
	pop ABX
	ret SLOT_SIZE*3
	
GLABEL Q43org5jnode2vm6Unsafe23inPortByte2e28I29I
	mov edx,[ASP+SLOT_SIZE] ; portNr
	in al,dx
	movzx eax,al
	ret SLOT_SIZE
	
GLABEL Q43org5jnode2vm6Unsafe23inPortWord2e28I29I
	mov edx,[ASP+SLOT_SIZE] ; portNr
	in ax,dx
	movzx eax,ax
	ret SLOT_SIZE
	
GLABEL Q43org5jnode2vm6Unsafe23inPortDword2e28I29I
	mov edx,[ASP+SLOT_SIZE] ; portNr
	in eax,dx
	ret SLOT_SIZE
	
GLABEL Q43org5jnode2vm6Unsafe23outPortByte2e28II29V
	mov eax,[ASP+(1*SLOT_SIZE)] ; value
	mov edx,[ASP+(2*SLOT_SIZE)] ; portNr
	out dx,al
	ret SLOT_SIZE*2

GLABEL Q43org5jnode2vm6Unsafe23outPortWord2e28II29V
	mov eax,[ASP+(1*SLOT_SIZE)] ; value
	mov edx,[esp+(2*SLOT_SIZE)] ; portNr
	out dx,ax
	ret SLOT_SIZE*2
	
GLABEL Q43org5jnode2vm6Unsafe23outPortDword2e28II29V
	mov eax,[ASP+(1*SLOT_SIZE)] ; value
	mov edx,[esp+(2*SLOT_SIZE)] ; portNr
	out dx,eax
	ret SLOT_SIZE*2

GLABEL Q43org5jnode2vm6Unsafe23idle2e2829V
	sti
	hlt
	nop
	nop
	ret

GLABEL Q43org5jnode2vm6Unsafe23die2e2829V
	cli
	hlt
	nop
	nop
	ret

; invoke(method)
GLABEL Q43org5jnode2vm6Unsafe23invokeVoid2e28Lorg2fjnode2fvm2fclassmgr2fVmMethod3b29V
GLABEL Q43org5jnode2vm6Unsafe23invokeInt2e28Lorg2fjnode2fvm2fclassmgr2fVmMethod3b29I
GLABEL Q43org5jnode2vm6Unsafe23invokeLong2e28Lorg2fjnode2fvm2fclassmgr2fVmMethod3b29J
GLABEL Q43org5jnode2vm6Unsafe23invokeObject2e28Lorg2fjnode2fvm2fclassmgr2fVmMethod3b29Ljava2flang2fObject3b
	pop AAX 			; Get return address
	xchg AAX,[ASP+0]	; EAX now contains method argument, Top of stack now contains return address
	jmp [AAX+VmMethod_NATIVECODE_OFS]

; int Unsafe.initThread(VmThread thread, Object newStack, int stackSize)
; Initialize the new Thread.
GLABEL Q43org5jnode2vm6Unsafe23initThread2e28Lorg2fjnode2fvm2fVmThread3bLjava2flang2fObject3bI29V
	push ABX
	mov ACX,[ASP+(4*SLOT_SIZE)]		; newThread
	mov ADX,[ASP+(3*SLOT_SIZE)]		; newStack
	mov ABX,[ASP+(2*SLOT_SIZE)]		; stack size

	; Setup bound stackoverflow check area
	mov [ADX+0], ADX	; Low == stack + (sizeof bound area) + STACK_OVERFLOW_LIMIT
	add WORD [ADX+0],((VmThread_STACK_OVERFLOW_LIMIT_SLOTS * SLOT_SIZE)+8)
	mov [ADX+SLOT_SIZE], ADX	; High == stack + stacksize
	add [ADX+SLOT_SIZE], ABX

	push ABP
	pushf	
	cli
	add ABX,ADX			; stackptr -> ebx
	mov ADX,ASP 		; Save current esp
	mov ASP,ABX
	xor ABX,ABX
	xor ABP,ABP			; Clear the frame ptr
	push ABP 			; previous EBP
	push ABP		    ; MAGIC    (here invalid ON PURPOSE!)
	push ABP		   	; PC       (here invalid ON PURPOSE!)
	push ABP		    ; VmMethod (here invalid ON PURPOSE!)
	mov ABP,ASP
	push ABX			; Dummy
	push ABX			; Dummy
	push ACX			; newThread	; Objectref for runThread
	push ABX			; Dummy		; method "return address"
	
	mov AAX,VmThread_runThread
	mov [ACX+VmX86Thread_EAX_OFS],AAX 		; runThread method
	mov [ACX+VmX86Thread_EBP_OFS],ABP
	mov AAX,[AAX+VmMethod_NATIVECODE_OFS]
	mov [ACX+VmX86Thread_EIP_OFS],AAX
	mov [ACX+VmX86Thread_ESP_OFS], ASP		; Save current esp
	pushf
	pop AAX
	or eax,F_IF
	mov [ACX+VmX86Thread_EFLAGS_OFS],AAX	; Setup EFLAGS
	
	mov ASP,ADX			; Restore esp
	popf				; Re-enable interrupts
	pop ABP
	pop ABX
	ret SLOT_SIZE*3
	
initThread_msg1: db 'New esp=',0

; public static native Address getMaxAddress()
GLABEL Q43org5jnode2vm6Unsafe23getMaxAddress2e2829Lorg2fvmmagic2funboxed2fAddress3b
	mov AAX,0xFFC00000	; 4Gb - 4Mb
	ret
	
; public static native VmAddress getMinAddress()
GLABEL Q43org5jnode2vm6Unsafe23getMinAddress2e2829Lorg2fvmmagic2funboxed2fAddress3b
	mov AAX,free_paddr
	ret
	
; public static native Address getMemoryStart()
GLABEL Q43org5jnode2vm6Unsafe23getMemoryStart2e2829Lorg2fvmmagic2funboxed2fAddress3b
	mov AAX,[free_mem_start]
	;; mov eax,freeMemoryStart
	ret
	
; public static native Address getMemoryEnd()
GLABEL Q43org5jnode2vm6Unsafe23getMemoryEnd2e2829Lorg2fvmmagic2funboxed2fAddress3b
	mov AAX,[mem_size]
	ret
	
; public static native Address getKernelStart()
GLABEL Q43org5jnode2vm6Unsafe23getKernelStart2e2829Lorg2fvmmagic2funboxed2fAddress3b
	mov AAX,kernel_begin
	ret
	
; public static native Address getKernelEnd()
GLABEL Q43org5jnode2vm6Unsafe23getKernelEnd2e2829Lorg2fvmmagic2funboxed2fAddress3b
	mov AAX,vm_start
	ret
	
; public static native Address getInitJarStart()
GLABEL Q43org5jnode2vm6Unsafe23getInitJarStart2e2829Lorg2fvmmagic2funboxed2fAddress3b
	mov AAX,[initJar_start]
	ret
	
; public static native Address getInitJarEnd()
GLABEL Q43org5jnode2vm6Unsafe23getInitJarEnd2e2829Lorg2fvmmagic2funboxed2fAddress3b
	mov AAX,[initJar_end]
	ret
	
; public static native Address getBootHeapStart()
GLABEL Q43org5jnode2vm6Unsafe23getBootHeapStart2e2829Lorg2fvmmagic2funboxed2fAddress3b
	mov AAX,bootHeapStart
	ret
	
; public static native Address getBootHeapEnd()
GLABEL Q43org5jnode2vm6Unsafe23getBootHeapEnd2e2829Lorg2fvmmagic2funboxed2fAddress3b
	mov AAX,bootHeapEnd
	ret
	
; Gets information of the JNode kernel command line.
; @param destination If non-null, the commandline is copied into this array.
; @return The maximum length of the command line
; protected static native int getCmdLine(byte[] destination);
GLABEL Q43org5jnode2vm6Unsafe23getCmdLine2e285bB29I
	mov AAX,[ASP+SLOT_SIZE]		; destination
	test AAX,AAX				; Is null?
	jz after_copyCmdLine
	mov ecx,[AAX+(VmArray_LENGTH_OFFSET*SLOT_SIZE)]
	push ASI
	push ADI
	mov ASI,multiboot_cmdline
	lea ADI,[AAX+(VmArray_DATA_OFFSET*SLOT_SIZE)]
	cld
	rep	movsb
	pop ADI
	pop ASI
after_copyCmdLine:
	mov eax,MBI_CMDLINE_MAX
	ret SLOT_SIZE
	
; Gets the current processor
GLABEL Q43org5jnode2vm6Unsafe23getCurrentProcessor2e2829Lorg2fjnode2fvm2fVmProcessor3b
	mov AAX,CURRENTPROCESSOR
	ret	

; Force a yieldpoint
GLABEL Q43org5jnode2vm6Unsafe23yieldPoint2e2829V
	; Is a switch required?
	cmp THREADSWITCHINDICATOR,VmProcessor_TSI_SWITCH_REQUESTED
	jne noYieldPoint
	int 0x30
noYieldPoint:
	ret	

; Address getJumpTable0()	
GLABEL Q43org5jnode2vm6Unsafe23getJumpTable02e2829Lorg2fvmmagic2funboxed2fAddress3b
	mov AAX,vm_jumpTable
	ret
	
; public static native void debug(String str);
GLABEL Q43org5jnode2vm6Unsafe23debug2e28Ljava2flang2fString3b29V
	mov AAX,[ASP+SLOT_SIZE]
	call vm_print_string
	ret SLOT_SIZE
	
; public static native void debug(char value);
GLABEL Q43org5jnode2vm6Unsafe23debug2e28C29V
	mov eax,[ASP+SLOT_SIZE]
%ifdef BITS32	
	call sys_print_char32
%else
	call sys_print_char64
%endif	
	ret SLOT_SIZE

; public static native void debug(int value);
GLABEL Q43org5jnode2vm6Unsafe23debug2e28I29V
	mov eax,[ASP+SLOT_SIZE]
%ifdef BITS32	
	call sys_print_eax32
%else
	call sys_print_eax64
%endif	
	ret SLOT_SIZE

; public static native void debug(long value);
GLABEL Q43org5jnode2vm6Unsafe23debug2e28J29V
%ifdef BITS32
	mov eax,[esp+8]		; MSB
	call sys_print_eax32
	mov eax,[esp+4]		; LSB
	call sys_print_eax32
%else
	mov rax,[rsp+SLOT_SIZE]
	call sys_print_rax64
%endif	
	ret SLOT_SIZE*2
		
; public static native void debug(Address value);
; public static native void debug(Word value);
; public static native void debug(Extent value);
; public static native void debug(Offset value);
GLABEL Q43org5jnode2vm6Unsafe23debug2e28Lorg2fvmmagic2funboxed2fAddress3b29V
GLABEL Q43org5jnode2vm6Unsafe23debug2e28Lorg2fvmmagic2funboxed2fWord3b29V
GLABEL Q43org5jnode2vm6Unsafe23debug2e28Lorg2fvmmagic2funboxed2fExtent3b29V
GLABEL Q43org5jnode2vm6Unsafe23debug2e28Lorg2fvmmagic2funboxed2fOffset3b29V
	mov AAX,[ASP+SLOT_SIZE]
%ifdef BITS32	
	call sys_print_eax32
%else
	call sys_print_rax64
%endif	
	ret SLOT_SIZE
		
	
	