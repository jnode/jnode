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

Q43org5jnode2vm6Unsafe23addressOf2e28Ljava2flang2fObject3b29Lorg2fjnode2fvm2fVmAddress3b:
Q43org5jnode2vm6Unsafe23addressToInt2e28Lorg2fjnode2fvm2fVmAddress3b29I:
Q43org5jnode2vm6Unsafe23intToAddress2e28I29Lorg2fjnode2fvm2fVmAddress3b:
	mov eax,[esp+4] ; object
	ret 4

Q43org5jnode2vm6Unsafe23addressToLong2e28Lorg2fjnode2fvm2fVmAddress3b29J:
	mov eax,[esp+4] ; address -> lsb
	xor edx,edx		; msb
	ret 4

Q43org5jnode2vm6Unsafe23longToAddress2e28J29Lorg2fjnode2fvm2fVmAddress3b:
	mov eax,[esp+4] ; LSB long -> address
	ret 8 			; We ignore the MSB here.

Q43org5jnode2vm6Unsafe23pushInt2e28I29V:
Q43org5jnode2vm6Unsafe23pushLong2e28J29V:
Q43org5jnode2vm6Unsafe23pushObject2e28Ljava2flang2fObject3b29V:
	ret

Q43org5jnode2vm6Unsafe23objectAt2e28Lorg2fjnode2fvm2fVmAddress3b29Ljava2flang2fObject3b:
	mov eax,[esp+4] ; memPtr
	ret 4
	
Q43org5jnode2vm6Unsafe23getSuperClasses2e28Ljava2flang2fObject3b295bLorg2fjnode2fvm2fclassmgr2fVmType3b:
	mov eax,[esp+4] ; object reference
	mov eax,[eax+ObjectLayout_TIB_SLOT*4] ; TIB reference
	mov eax,[eax+(TIBLayout_SUPERCLASSES_INDEX+VmArray_DATA_OFFSET)*4] 
	ret 4

Q43org5jnode2vm6Unsafe23getBoolean2e28Lorg2fjnode2fvm2fVmAddress3b29Z:
	mov eax,[esp+4] ; memPtr
	movzx eax,byte [eax]
	ret 4

; boolean getBoolean(Object object, int offset);
Q43org5jnode2vm6Unsafe23getBoolean2e28Ljava2flang2fObject3bI29Z:
	mov eax,[esp+8] ; Object
	add eax,[esp+4] ; Offset
	movzx eax,byte [eax]
	ret 8

Q43org5jnode2vm6Unsafe23getByte2e28Lorg2fjnode2fvm2fVmAddress3b29B:
	mov eax,[esp+4] ; memPtr
	movsx eax,byte [eax]
	ret 4
	
; byte getByte(Object object, int offset);
Q43org5jnode2vm6Unsafe23getByte2e28Ljava2flang2fObject3bI29B:
	mov eax,[esp+8] ; Object
	add eax,[esp+4] ; Offset
	movsx eax,byte [eax]
	ret 8
	
Q43org5jnode2vm6Unsafe23getChar2e28Lorg2fjnode2fvm2fVmAddress3b29C:
	mov eax,[esp+4] ; memPtr
	movzx eax,word [eax]
	ret 4

Q43org5jnode2vm6Unsafe23getShort2e28Lorg2fjnode2fvm2fVmAddress3b29S:
	mov eax,[esp+4] ; memPtr
	movsx eax,word [eax]
	ret 4

; short getShort(Object object, int offset);
Q43org5jnode2vm6Unsafe23getShort2e28Ljava2flang2fObject3bI29S:
	mov eax,[esp+8] ; Object
	add eax,[esp+4] ; Offset
	movsx eax,word [eax]
	ret 8

; char getChar(Object object, int offset);
Q43org5jnode2vm6Unsafe23getChar2e28Ljava2flang2fObject3bI29C:
	mov eax,[esp+8] ; Object
	add eax,[esp+4] ; Offset
	movzx eax,word [eax]
	ret 8

; int getInt(VmAddress memPtr);
Q43org5jnode2vm6Unsafe23getInt2e28Lorg2fjnode2fvm2fVmAddress3b29I:
	mov eax,[esp+4] ; memPtr
	mov eax,dword [eax]
	ret 4
	
; int getInt(Object object, int offset);
Q43org5jnode2vm6Unsafe23getInt2e28Ljava2flang2fObject3bI29I:
	mov eax,[esp+8] ; Object
	add eax,[esp+4] ; Offset
	mov eax,dword [eax]
	ret 8
	
; long getLong(VmAddress memPtr);
Q43org5jnode2vm6Unsafe23getLong2e28Lorg2fjnode2fvm2fVmAddress3b29J:
	mov eax,[esp+4] ; memPtr
	mov edx,dword [eax+4] ; MSB
	mov eax,dword [eax+0] ; LSB
	ret 4

; long getLong(Object object, int offset);
Q43org5jnode2vm6Unsafe23getLong2e28Ljava2flang2fObject3bI29J:
	mov eax,[esp+8] ; Object
	add eax,[esp+4] ; Offset
	mov edx,dword [eax+4] ; MSB
	mov eax,dword [eax+0] ; LSB
	ret 8

; float getFloat(VmAddress memPtr);
Q43org5jnode2vm6Unsafe23getFloat2e28Lorg2fjnode2fvm2fVmAddress3b29F:
	mov eax,[esp+4] ; memPtr
	mov eax,dword [eax]
	ret 4

; float getFloat(Object objec, int offset);
Q43org5jnode2vm6Unsafe23getFloat2e28Ljava2flang2fObject3bI29F:
	mov eax,[esp+8] ; Object
	add eax,[esp+4] ; Offset
	mov eax,dword [eax]
	ret 8

; double getDouble(VmAddress memPtr);
Q43org5jnode2vm6Unsafe23getDouble2e28Lorg2fjnode2fvm2fVmAddress3b29D:
	mov eax,[esp+4] ; memPtr
	mov edx,dword [eax+4] ; MSB
	mov eax,dword [eax+0] ; LSB
	ret 4

; double getDouble(Object object, int offset);
Q43org5jnode2vm6Unsafe23getDouble2e28Ljava2flang2fObject3bI29D:
	mov eax,[esp+8] ; Object
	add eax,[esp+4] ; Offset
	mov edx,dword [eax+4] ; MSB
	mov eax,dword [eax+0] ; LSB
	ret 8

; Object getObject(VmAddress memPtr);
; VmAddress getAddress(VmAddress memPtr);
Q43org5jnode2vm6Unsafe23getObject2e28Lorg2fjnode2fvm2fVmAddress3b29Ljava2flang2fObject3b:
Q43org5jnode2vm6Unsafe23getAddress2e28Lorg2fjnode2fvm2fVmAddress3b29Lorg2fjnode2fvm2fVmAddress3b:
	mov eax,[esp+4] ; memPtr
	mov eax,dword [eax]
	ret 4

; Object getObject(Object object, int offset);
; VmAddress getAddress(Object object, int offset);
Q43org5jnode2vm6Unsafe23getObject2e28Ljava2flang2fObject3bI29Ljava2flang2fObject3b:
Q43org5jnode2vm6Unsafe23getAddress2e28Ljava2flang2fObject3bI29Lorg2fjnode2fvm2fVmAddress3b:
	mov eax,[esp+8] ; Object
	add eax,[esp+4] ; Offset
	mov eax,dword [eax]
	ret 8

; void setBoolean(VmAddress memPtr, boolean value);
Q43org5jnode2vm6Unsafe23setBoolean2e28Lorg2fjnode2fvm2fVmAddress3bZ29V:
	mov eax,[esp+8] ; memPtr
	mov edx,[esp+4] ; value
	mov byte [eax],dl
	ret 8

; void setBoolean(Object object, int offset, boolean value);
Q43org5jnode2vm6Unsafe23setBoolean2e28Ljava2flang2fObject3bIZ29V:
	mov eax,[esp+12] ; Object
	add eax,[esp+8]  ; Offset
	mov edx,[esp+4]  ; value
	mov byte [eax],dl
	ret 12

; void setByte(VmAddress memPtr, byte value);
Q43org5jnode2vm6Unsafe23setByte2e28Lorg2fjnode2fvm2fVmAddress3bB29V:
	mov eax,[esp+8] ; memPtr
	mov edx,[esp+4] ; value
	mov byte [eax],dl
	ret 8

; void setByte(Object object, int offset, byte value);
Q43org5jnode2vm6Unsafe23setByte2e28Ljava2flang2fObject3bIB29V:
	mov eax,[esp+12] ; Object
	add eax,[esp+8]  ; Offset
	mov edx,[esp+4] ; value
	mov byte [eax],dl
	ret 12

; void setShort(VmAddress memPtr, short value);
Q43org5jnode2vm6Unsafe23setShort2e28Lorg2fjnode2fvm2fVmAddress3bS29V:
	mov eax,[esp+8] ; memPtr
	mov edx,[esp+4] ; value
	mov word [eax],dx
	ret 8

; void setShort(Object object, int offset, short value);
Q43org5jnode2vm6Unsafe23setShort2e28Ljava2flang2fObject3bIS29V:
	mov eax,[esp+12] ; Object
	add eax,[esp+8]  ; Offset
	mov edx,[esp+4] ; value
	mov word [eax],dx
	ret 12

; void setChar(VmAddress memPtr, char value);
Q43org5jnode2vm6Unsafe23setChar2e28Lorg2fjnode2fvm2fVmAddress3bC29V:
	mov eax,[esp+8] ; memPtr
	mov edx,[esp+4] ; value
	mov word [eax],dx
	ret 8

; void setChar(Object object, int offset, char value);
Q43org5jnode2vm6Unsafe23setChar2e28Ljava2flang2fObject3bIC29V:
	mov eax,[esp+12] ; Object
	add eax,[esp+8]  ; Offset
	mov edx,[esp+4] ; value
	mov word [eax],dx
	ret 12

; void setInt(VmAddress memPtr, int value);
Q43org5jnode2vm6Unsafe23setInt2e28Lorg2fjnode2fvm2fVmAddress3bI29V:
	mov eax,[esp+8] ; memPtr
	mov edx,[esp+4] ; value
	mov dword [eax],edx
	ret 8

; void setInt(Object object, int offset, int value);
Q43org5jnode2vm6Unsafe23setInt2e28Ljava2flang2fObject3bII29V:
	mov eax,[esp+12] ; Object
	add eax,[esp+8]  ; Offset
	mov edx,[esp+4] ; value
	mov dword [eax],edx
	ret 12

; void setLong(VmAddress memPtr, long value);
Q43org5jnode2vm6Unsafe23setLong2e28Lorg2fjnode2fvm2fVmAddress3bJ29V:
	mov eax,[esp+12] ; memPtr
	mov edx,[esp+8] ; value MSB
	mov dword [eax+4],edx
	mov edx,[esp+4] ; value LSB
	mov dword [eax+0],edx
	ret 12

; void setLong(Object object, int offset, long value);
Q43org5jnode2vm6Unsafe23setLong2e28Ljava2flang2fObject3bIJ29V:
	mov eax,[esp+16] ; Object
	add eax,[esp+12]  ; Offset
	mov edx,[esp+8] ; value MSB
	mov dword [eax+4],edx
	mov edx,[esp+4] ; value LSB
	mov dword [eax+0],edx
	ret 16

; void setFloat(VmAddress memPtr, float value);
Q43org5jnode2vm6Unsafe23setFloat2e28Lorg2fjnode2fvm2fVmAddress3bF29V:
	mov eax,[esp+8] ; memPtr
	mov edx,[esp+4] ; value
	mov dword [eax],edx
	ret 8

; void setFloat(Object object, int offset, float value);
Q43org5jnode2vm6Unsafe23setFloat2e28Ljava2flang2fObject3bIF29V:
	mov eax,[esp+12] ; Object
	add eax,[esp+8]  ; Offset
	mov edx,[esp+4] ; value
	mov dword [eax],edx
	ret 12

; void setDouble(VmAddress memPtr, double value);
Q43org5jnode2vm6Unsafe23setDouble2e28Lorg2fjnode2fvm2fVmAddress3bD29V:
	mov eax,[esp+12] ; memPtr
	mov edx,[esp+8] ; value MSB
	mov dword [eax+4],edx
	mov edx,[esp+4] ; value LSB
	mov dword [eax+0],edx
	ret 12

; void setDouble(Object object, int offset, double value);
Q43org5jnode2vm6Unsafe23setDouble2e28Ljava2flang2fObject3bID29V:
	mov eax,[esp+16] ; Object
	add eax,[esp+12]  ; Offset
	mov edx,[esp+8] ; value MSB
	mov dword [eax+4],edx
	mov edx,[esp+4] ; value LSB
	mov dword [eax+0],edx
	ret 16

; void setObject(VmAddress memPtr, Object value);
Q43org5jnode2vm6Unsafe23setObject2e28Lorg2fjnode2fvm2fVmAddress3bLjava2flang2fObject3b29V:
	mov eax,[esp+8] ; memPtr
	mov edx,[esp+4] ; value
	mov dword [eax],edx
	ret 8

; void setObject(Object object, int offset, Object value);
Q43org5jnode2vm6Unsafe23setObject2e28Ljava2flang2fObject3bILjava2flang2fObject3b29V:
	mov eax,[esp+12] ; Object
	add eax,[esp+8]  ; Offset
	mov edx,[esp+4] ; value
	mov dword [eax],edx
	ret 12

Q43org5jnode2vm6Unsafe23intBitsToFloat2e28I29F:
	mov eax,[esp+4]
	ret 4

Q43org5jnode2vm6Unsafe23floatToRawIntBits2e28F29I:
	mov eax,[esp+4]
	ret 4

Q43org5jnode2vm6Unsafe23longBitsToDouble2e28J29D:
	mov edx,[esp+8] ; MSB
	mov eax,[esp+4] ; LSB
	ret 8

Q43org5jnode2vm6Unsafe23doubleToRawLongBits2e28D29J:
	mov edx,[esp+8] ; MSB
	mov eax,[esp+4] ; LSB
	ret 8

Q43org5jnode2vm6Unsafe23clear2e28Lorg2fjnode2fvm2fVmAddress3bI29V:
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

Q43org5jnode2vm6Unsafe23copy2e28Lorg2fjnode2fvm2fVmAddress3bLorg2fjnode2fvm2fVmAddress3bI29V:
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

; VmAddress getCurrentFrame()	
Q43org5jnode2vm6Unsafe23getCurrentFrame2e2829Lorg2fjnode2fvm2fVmAddress3b:
	mov eax,ebp
	ret
	
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
	jmp [eax+VmMethod_NATIVECODE_OFFSET*4]

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
	mov [ecx+VmX86Thread_EAX_OFFSET*4],eax ; runThread method
	mov [ecx+VmX86Thread_EBP_OFFSET*4],ebp
	mov eax,[eax+VmMethod_NATIVECODE_OFFSET*4]
	mov [ecx+VmX86Thread_EIP_OFFSET*4],eax
	mov [ecx+VmX86Thread_ESP_OFFSET*4], esp		; Save current esp
	pushf
	pop eax
	or eax,F_IF
	mov [ecx+VmX86Thread_EFLAGS_OFFSET*4],eax	; Setup EFLAGS
	
	mov esp,edx ; Restore esp
	popf		; Re-enable interrupts
	popa
	pop ebx
	ret 12
	
initThread_msg1: db 'New esp=',0

; public static native int compare(VmAddress a1, VmAddress a2);
Q43org5jnode2vm6Unsafe23compare2e28Lorg2fjnode2fvm2fVmAddress3bLorg2fjnode2fvm2fVmAddress3b29I:
	mov eax,[esp+8]
	cmp eax,[esp+4]
	jl compare_lt
	jg compare_gt
	xor eax,eax ; equal
	ret 8
compare_lt:
	mov eax,-1
	ret 8
compare_gt:
	mov eax,1
	ret 8

; public static native VmAddress add(VmAddress addr, int incValue);
; public static native VmAddress add(VmAddress a1, VmAddress a2);
Q43org5jnode2vm6Unsafe23add2e28Lorg2fjnode2fvm2fVmAddress3bI29Lorg2fjnode2fvm2fVmAddress3b:
Q43org5jnode2vm6Unsafe23add2e28Lorg2fjnode2fvm2fVmAddress3bLorg2fjnode2fvm2fVmAddress3b29Lorg2fjnode2fvm2fVmAddress3b:
	mov eax,[esp+4] ; incValue
	add eax,[esp+8] ; addr
	ret 8

; protected static native boolean atomicCompareAndSwap(VmAddress address, int oldValue, int newValue)
Q43org5jnode2vm6Unsafe23atomicCompareAndSwap2e28Lorg2fjnode2fvm2fVmAddress3bII29Z:
	mov eax,[esp+8] ; old value
	mov ecx,[esp+4] ; new value
	mov edx,[esp+12] ; address
	lock cmpxchg dword [edx], ecx
	jnz cas_not_ok
	mov eax,1
	ret 12
cas_not_ok:
	xor eax,eax
	ret 12

; protected static native boolean atomicAnd(VmAddress address, int value)
Q43org5jnode2vm6Unsafe23atomicAnd2e28Lorg2fjnode2fvm2fVmAddress3bI29Z:
	mov ecx,[esp+4] ; value
	mov edx,[esp+8] ; address
	lock and dword [edx], ecx
	ret 8

; protected static native boolean atomicOr(VmAddress address, int value)
Q43org5jnode2vm6Unsafe23atomicOr2e28Lorg2fjnode2fvm2fVmAddress3bI29Z:
	mov ecx,[esp+4] ; value
	mov edx,[esp+8] ; address
	lock or dword [edx], ecx
	ret 8

; protected static native boolean atomicSub(VmAddress address, int value)
Q43org5jnode2vm6Unsafe23atomicSub2e28Lorg2fjnode2fvm2fVmAddress3bI29Z:
	mov ecx,[esp+4] ; value
	mov edx,[esp+8] ; address
	lock sub dword [edx], ecx
	ret 8

; public static native VmAddress getMaxAddress()
Q43org5jnode2vm6Unsafe23getMaxAddress2e2829Lorg2fjnode2fvm2fVmAddress3b:
	mov eax,0xFFC00000	; 4Gb - 4Mb
	ret
	
; public static native VmAddress getMinAddress()
Q43org5jnode2vm6Unsafe23getMinAddress2e2829Lorg2fjnode2fvm2fVmAddress3b:
	mov eax,free_paddr
	ret
	
; public static native VmAddress getMemoryStart()
Q43org5jnode2vm6Unsafe23getMemoryStart2e2829Lorg2fjnode2fvm2fVmAddress3b:
	mov eax,[free_mem_start]
	;; mov eax,freeMemoryStart
	ret
	
; public static native VmAddress getMemoryEnd()
Q43org5jnode2vm6Unsafe23getMemoryEnd2e2829Lorg2fjnode2fvm2fVmAddress3b:
	mov eax,[mem_size]
	ret
	
; public static native VmAddress getKernelStart()
Q43org5jnode2vm6Unsafe23getKernelStart2e2829Lorg2fjnode2fvm2fVmAddress3b:
	mov eax,kernel_begin
	ret
	
; public static native VmAddress getKernelEnd()
Q43org5jnode2vm6Unsafe23getKernelEnd2e2829Lorg2fjnode2fvm2fVmAddress3b:
	mov eax,vm_start
	ret
	
; public static native VmAddress getInitJarStart()
Q43org5jnode2vm6Unsafe23getInitJarStart2e2829Lorg2fjnode2fvm2fVmAddress3b:
	mov eax,[initJar_start]
	ret
	
; public static native VmAddress getInitJarEnd()
Q43org5jnode2vm6Unsafe23getInitJarEnd2e2829Lorg2fjnode2fvm2fVmAddress3b:
	mov eax,[initJar_end]
	ret
	
; public static native VmAddress getBootHeapStart()
Q43org5jnode2vm6Unsafe23getBootHeapStart2e2829Lorg2fjnode2fvm2fVmAddress3b:
	mov eax,bootHeapStart
	ret
	
; public static native VmAddress getBootHeapEnd()
Q43org5jnode2vm6Unsafe23getBootHeapEnd2e2829Lorg2fjnode2fvm2fVmAddress3b:
	mov eax,bootHeapEnd
	ret
	
; public static native long getTimeStampCounter()
Q43org5jnode2vm6Unsafe23getTimeStampCounter2e2829J:
	rdtsc
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
	UNCOND_YIELDPOINT
	ret	

; VmAddress getJumpTable0()	
Q43org5jnode2vm6Unsafe23getJumpTable02e2829Lorg2fjnode2fvm2fVmAddress3b:
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
		
; public static native void breakPoint();
Q43org5jnode2vm6Unsafe23breakPoint2e2829V:
	int3
	ret
	
	