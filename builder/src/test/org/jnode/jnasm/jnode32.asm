







               
                 
                 
 	
 			
    
 			

MAX_STACK_TRACE_LENGTH		equ 30	

bits 32
 









configbase equ 0c000h

      

     
      
    
    
        
     


  
	section .text

kernel_begin:
    













F_CF        equ 0x00000001
F_1         equ 0x00000002
F_PF        equ 0x00000004
F_01        equ 0x00000008
F_AF        equ 0x00000010
F_02        equ 0x00000020
F_ZF        equ 0x00000040
F_SF        equ 0x00000080
F_TF        equ 0x00000100
F_IF        equ 0x00000200
F_DF        equ 0x00000400
F_OF        equ 0x00000800
F_IOPL1     equ 0x00001000
F_IOPL2     equ 0x00002000
F_NT        equ 0x00004000	
F_03        equ 0x00008000
F_RF        equ 0x00010000	
F_VM        equ 0x00020000	
F_AC		equ 0x00040000	
F_VIF		equ	0x00080000	
F_VIP		equ	0x00100000	
F_ID		equ	0x00200000	





CR0_PE		equ 0x00000001	
CR0_MP		equ 0x00000002	
CR0_EM		equ 0x00000004	
CR0_TS		equ 0x00000008	
CR0_ET		equ 0x00000010	
CR0_PG		equ 0x80000000	





CR4_VME			equ 1 << 0	
CR4_PVI			equ 1 << 1	
CR4_TSD			equ 1 << 2	
CR4_DE			equ 1 << 3	
CR4_PSE			equ 1 << 4	
CR4_PAE			equ 1 << 5	
CR4_MCE			equ 1 << 6	
CR4_PGE			equ 1 << 7	
CR4_PCE			equ 1 << 8	
CR4_OSFXSR		equ 1 << 9	
CR4_OSXMMEXCPT	equ 1 << 10	






MXCSR_IE		equ 1 << 0	
MXCSR_DE		equ 1 << 1	
MXCSR_ZE		equ 1 << 2	
MXCSR_OE		equ 1 << 3	
MXCSR_UE		equ 1 << 4	
MXCSR_PE		equ 1 << 5	
MXCSR_DAZ		equ 1 << 6	
MXCSR_IM		equ 1 << 7	
MXCSR_DM		equ 1 << 8	
MXCSR_ZM		equ 1 << 9	
MXCSR_OM		equ 1 << 10	
MXCSR_UM		equ 1 << 11	
MXCSR_PM		equ 1 << 12	
MXCSR_RC1		equ 1 << 13	
MXCSR_RC2		equ 1 << 14	
MXCSR_FZ		equ 1 << 15	





iPF_PRESENT		equ 0x00000001
iPF_WRITE		equ 0x00000002
iPF_USER		equ 0x00000004
iPF_PWT			equ 0x00000008
iPF_PCD			equ 0x00000010
iPF_ACCESSED	equ 0x00000020
iPF_DIRTY		equ 0x00000040
iPF_PSE			equ 0x00000080
iPF_AVAIL0		equ 0x00000200
iPF_AVAIL1		equ 0x00000400
iPF_AVAIL2		equ 0x00000800

iPF_ADDRMASK	equ 0xFFFFF000
iPF_FLAGSMASK	equ 0x00000FFF





FEAT_FPU		equ 1 << 0
FEAT_VME		equ 1 << 1
FEAT_DE			equ 1 << 2
FEAT_PSE		equ 1 << 3
FEAT_TSC		equ 1 << 4
FEAT_MSR		equ 1 << 5
FEAT_PAE		equ 1 << 6
FEAT_MCE		equ 1 << 7
FEAT_CX8		equ 1 << 8
FEAT_APIC		equ 1 << 9
FEAT_SEP		equ 1 << 11
FEAT_MTRR		equ 1 << 12
FEAT_PGE		equ 1 << 13
FEAT_MCA		equ 1 << 14
FEAT_CMOV		equ 1 << 15
FEAT_PAT		equ 1 << 16
FEAT_PSE36		equ 1 << 17
FEAT_PSN		equ 1 << 18
FEAT_CLFSH		equ 1 << 19
FEAT_DS			equ 1 << 21
FEAT_ACPI		equ 1 << 22
FEAT_MMX		equ 1 << 23
FEAT_FXSR		equ 1 << 24
FEAT_SSE		equ 1 << 25
FEAT_SSE2		equ 1 << 26
FEAT_SS			equ 1 << 27
FEAT_HTT		equ 1 << 28
FEAT_TM			equ 1 << 29
FEAT_PBE		equ 1 << 31





MBI_FLAGS		equ 0x00
MBI_MEMLOWER	equ 0x04
MBI_MEMUPPER	equ 0x08
MBI_BOOTDEVICE	equ 0x0C
MBI_CMDLINE		equ 0x10
MBI_MODSCOUNT	equ 0x14
MBI_MODSADDR	equ 0x18
MBI_MMAPLENGTH	equ 0x2C
MBI_MMAPADDR	equ 0x30
MBI_VBECTRLINFO	equ 72
MBI_VBEMODEINFO	equ 76
MBI_VBE_MODE	equ 80
MBI_VBE_INT_SEG	equ 82
MBI_VBE_INT_OFF	equ 84
MBI_VBE_INT_LEN	equ 86

MBI_SIZE		equ 0x58
MBI_CMDLINE_MAX	equ 0x400
MBI_MMAP_MAX	equ 64

MBMOD_START		equ 0x00
MBMOD_END		equ 0x04
MBMOD_CMDLINE	equ 0x08
MBMOD_PAD		equ 0x10

MBMMAP_SIZE		equ -4
MBMMAP_BASEADDR	equ 0		
MBMMAP_LENGTH	equ 8		
MBMMAP_TYPE		equ 16		
MBMMAP_ESIZE	equ 20


VBECTRLINFO	equ 0
VBEMODEINFO	equ 4
VBE_MODE	equ 8
VBE_INT_SEG	equ 10
VBE_INT_OFF	equ 12
VBE_INT_LEN	equ 14


VBECTRLINFO_SIZE	equ 512
VBEMODEINFO_SIZE	equ 256


MBF_MEM			equ (1 << 0)
MBF_BOOTDEVICE	equ (1 << 1)
MBF_CMDLINE		equ (1 << 2)
MBF_MODS		equ (1 << 3)
MBF_MMAP		equ (1 << 6)
MBF_VBE			equ (1 << 11)














 
 
 
 
 
 
 
 
 
 
 

	 			
	 	
	 		
	
	 			
	 			
	 			
	 			
	 			
	 			
	 			
	 			

 
	 			
	 	
	 		
	 			
	 			
	 			
	 			
	 			
	 			
	 			
	 			

	


 
 









  
	
	
	
	
	
	



 








SYSCALL_INT		equ 0x32

  
	
	
	
	




SC_DISABLE_PAGING	equ 0x00
SC_ENABLE_PAGING	equ 0x01
SC_SYNC_MSRS		equ 0x02
SC_SAVE_MSRS		equ 0x03
SC_RESTORE_MSRS		equ 0x04
SC_WRITE_MSR		equ 0x05

SC_MAX				equ SC_WRITE_MSR



 








  
  


  
	
	
	
	
	
	
	


  
	
	
	
	

	


  
	
	
	
	
	


 





Throwable_BACKTRACE_OFS equ 0
Throwable_DETAILMESSAGE_OFS equ 4
Throwable_CAUSE_OFS equ 8
Throwable_STACKTRACE_OFS equ 12
Throwable_SIZE equ 16


BootImageBuilder_LOAD_ADDR equ 1048576
BootImageBuilder_INITIAL_OBJREFS_CAPACITY equ 750000
BootImageBuilder_INITIAL_SIZE equ 67108864
BootImageBuilder_JUMP_MAIN_OFFSET32 equ 16
BootImageBuilder_JUMP_MAIN_OFFSET64 equ 24
BootImageBuilder_INITIALIZE_METHOD_OFFSET equ 8





VmProcessor_THREADSWITCHINDICATOR_OFS equ 0
VmProcessor_ME_OFS equ 4
VmProcessor_CURRENTTHREAD_OFS equ 8
VmProcessor_ISOLATEDSTATICSTABLE_OFS equ 12
VmProcessor_ISOLATEDSTATICS_OFS equ 16
VmProcessor_NEXTTHREAD_OFS equ 20
VmProcessor_STACKEND_OFS equ 24
VmProcessor_KERNELSTACKEND_OFS equ 28
VmProcessor_ID_OFS equ 32
VmProcessor_IDSTRING_OFS equ 36
VmProcessor_IRQMGR_OFS equ 40
VmProcessor_TSIADDRESS_OFS equ 44
VmProcessor_SCHEDULER_OFS equ 48
VmProcessor_KERNELDEBUGGER_OFS equ 52
VmProcessor_ARCHITECTURE_OFS equ 56
VmProcessor_IDLETHREAD_OFS equ 60
VmProcessor_LOCKCOUNT_OFS equ 64
VmProcessor_CPUID_OFS equ 68
VmProcessor_STATICSTABLE_OFS equ 72
VmProcessor_JNODEMIPS_OFS equ 76
VmProcessor_LASTTHREADPRIORITY_OFS equ 80
VmProcessor_SAMETHREADPRIORITYCOUNT_OFS equ 84
VmProcessor_HEAPDATA_OFS equ 88
VmProcessor_MATHSUPPORT_OFS equ 92
VmProcessor_GCMAPITERATORS_OFS equ 96
VmProcessor_COMPILERIDS_OFS equ 100
VmProcessor_SIZE equ 104


VmProcessor_TSI_SWITCH_NEEDED equ 1
VmProcessor_TSI_SYSTEM_READY equ 2
VmProcessor_TSI_SWITCH_ACTIVE equ 4
VmProcessor_TSI_BLOCK_SWITCH equ 8
VmProcessor_TSI_SWITCH_REQUESTED equ 3



VmThread_JAVATHREAD_OFS equ 8
VmThread_QUEUEENTRY_OFS equ 12
VmThread_SLEEPQUEUEENTRY_OFS equ 16
VmThread_ALLTHREADSENTRY_OFS equ 20
VmThread_STACKSIZE_OFS equ 24
VmThread_STACK_OFS equ 28
VmThread_STACKEND_OFS equ 32
VmThread_STACKOVERFLOW_S1_OFS equ 0
VmThread_THREADSTATE_OFS equ 36
VmThread_WAKEUPTIME_OFS equ 76
VmThread_WAITFORMONITOR_OFS equ 40
VmThread_LASTOWNEDMONITOR_OFS equ 44
VmThread_PRIORITY_OFS equ 48
VmThread_ID_OFS equ 52
VmThread_INTERRUPTED_S1_OFS equ 1
VmThread_INEXCEPTION_S1_OFS equ 2
VmThread_STOPPING_S1_OFS equ 3
VmThread_NAME_OFS equ 56
VmThread_CONTEXT_OFS equ 60
VmThread_INSYSTEMEXCEPTION_S1_OFS equ 4
VmThread_ISOLATEDSTATICS_OFS equ 64
VmThread_REQUIREDPROCESSOR_OFS equ 68
VmThread_CURRENTPROCESSOR_OFS equ 72
VmThread_SIZE equ 84


VmThread_STACK_OVERFLOW_LIMIT_SLOTS equ 256
VmThread_DEFAULT_STACK_SLOTS equ 16384
VmThread_STACKTRACE_LIMIT equ 256
VmThread_EX_NULLPOINTER equ 0
VmThread_EX_PAGEFAULT equ 1
VmThread_EX_INDEXOUTOFBOUNDS equ 2
VmThread_EX_DIV0 equ 3
VmThread_EX_ABSTRACTMETHOD equ 4
VmThread_EX_STACKOVERFLOW equ 5
VmThread_EX_COPRO_OR equ 7
VmThread_EX_COPRO_ERR equ 8


ObjectLayout_FLAGS_SLOT equ -2
ObjectLayout_TIB_SLOT equ -1
ObjectLayout_HEADER_SLOTS equ 2
ObjectLayout_OBJECT_ALIGN equ 8
ObjectLayout_IMT_LENGTH equ 64


TIBLayout_VMTYPE_INDEX equ 0
TIBLayout_IMT_INDEX equ 1
TIBLayout_IMTCOLLISIONS_INDEX equ 2
TIBLayout_COMPILED_IMT_INDEX equ 3
TIBLayout_SUPERCLASSES_INDEX equ 4
TIBLayout_MIN_TIB_LENGTH equ 5
TIBLayout_FIRST_METHOD_INDEX equ 5


VmArray_LENGTH_OFFSET equ 0
VmArray_DATA_OFFSET equ 1



VmMethod_NATIVECODE_OFS equ 36
VmMethod_ARGSLOTCOUNT_S2_OFS equ 28
VmMethod_PARAMTYPES_OFS equ 40
VmMethod_RETURNTYPE_OFS equ 44
VmMethod_JAVAMEMBERHOLDER_OFS equ 48
VmMethod_BYTECODE_OFS equ 52
VmMethod_COMPILEDCODE_OFS equ 56
VmMethod_EXCEPTIONS_OFS equ 60
VmMethod_SELECTOR_OFS equ 64
VmMethod_NATIVECODEOPTLEVEL_S2_OFS equ 30
VmMethod_STATICSINDEX_OFS equ 68
VmMethod_MANGLEDNAME_OFS equ 72
VmMethod_PRAGMAFLAGS_S2_OFS equ 32
VmMethod_ANNOTATIONDEFAULT_OFS equ 76
VmMethod_RAWANNOTATIONDEFAULT_OFS equ 80
VmMethod_RAWPARAMETERANNOTATIONS_OFS equ 84
VmMethod_SIZE equ 88



VmStatics_STATICS_OFS equ 4
VmStatics_OBJECTS_OFS equ 8
VmStatics_SLOTLENGTH_OFS equ 12
VmStatics_LSBFIRST_S1_OFS equ 0
VmStatics_RESOLVER_OFS equ 16
VmStatics_LOCKED_S1_OFS equ 1
VmStatics_ALLOCATOR_OFS equ 20
VmStatics_SIZE equ 24



VmType_SUPERCLASS_OFS equ 16
VmType_SUPERCLASSNAME_OFS equ 20
VmType_SUPERCLASSDEPTH_OFS equ 24
VmType_NAME_OFS equ 28
VmType_SOURCEFILE_OFS equ 32
VmType_SIGNATURE_OFS equ 36
VmType_METHODTABLE_OFS equ 40
VmType_FIELDTABLE_OFS equ 44
VmType_MODIFIERS_OFS equ 48
VmType_PRAGMAFLAGS_S2_OFS equ 10
VmType_STATE_S2_OFS equ 12
VmType_CP_OFS equ 52
VmType_INTERFACETABLE_OFS equ 56
VmType_ALLINTERFACETABLE_OFS equ 60
VmType_LOADER_OFS equ 64
VmType_JAVACLASSHOLDER_OFS equ 68
VmType_RESOLVEDCPREFS_S1_OFS equ 8
VmType_TYPESIZE_S1_OFS equ 9
VmType_ARRAYCLASSNAME_OFS equ 72
VmType_ARRAYCLASS_OFS equ 76
VmType_SUPERCLASSESARRAY_OFS equ 80
VmType_FINALIZEMETHOD_OFS equ 84
VmType_MANGLEDNAME_OFS equ 88
VmType_ERRORMSG_OFS equ 92
VmType_STATICSINDEX_OFS equ 96
VmType_ISOLATEDSTATICSINDEX_OFS equ 100
VmType_PROTECTIONDOMAIN_OFS equ 104
VmType_MMTYPE_OFS equ 108
VmType_SIZE equ 112



MSR_ID_OFS equ 0
MSR_VALUE_OFS equ 4
MSR_SIZE equ 12



VmX86Processor_IRQCOUNT_OFS equ 104
VmX86Processor_LOCALAPIC_OFS equ 108
VmX86Processor_LOCALAPICEOIADDRESS_OFS equ 112
VmX86Processor_GDT_OFS equ 116
VmX86Processor_LOGICAL_S1_OFS equ 120
VmX86Processor_BOOTPROCESSOR_S1_OFS equ 124
VmX86Processor_SENDTIMESLICEINTERRUPT_OFS equ 128
VmX86Processor_RM_OFS equ 132
VmX86Processor_RESUME_INT_OFS equ 136
VmX86Processor_RESUME_INTNO_OFS equ 140
VmX86Processor_RESUME_ERROR_OFS equ 144
VmX86Processor_RESUME_HANDLER_OFS equ 148
VmX86Processor_DEADLOCKCOUNTER_OFS equ 152
VmX86Processor_FXSAVECOUNTER_OFS equ 156
VmX86Processor_FXRESTORECOUNTER_OFS equ 160
VmX86Processor_DEVICENACOUNTER_OFS equ 164
VmX86Processor_PERFCOUNTERS_OFS equ 168
VmX86Processor_SIZE equ 172


VmX86StackReader_METHOD_ID_OFFSET equ 0
VmX86StackReader_PREVIOUS_OFFSET equ 1
VmX86StackReader_RETURNADDRESS_OFFSET equ 2



VmX86Thread_EAX_OFS equ 84
VmX86Thread_EBX_OFS equ 88
VmX86Thread_ECX_OFS equ 92
VmX86Thread_EDX_OFS equ 96
VmX86Thread_ESI_OFS equ 100
VmX86Thread_EDI_OFS equ 104
VmX86Thread_EFLAGS_OFS equ 108
VmX86Thread_EIP_OFS equ 112
VmX86Thread_ESP_OFS equ 116
VmX86Thread_EBP_OFS equ 120
VmX86Thread_FXSTATE_OFS equ 124
VmX86Thread_FXSTATEPTR_OFS equ 128
VmX86Thread_FXFLAGS_OFS equ 132
VmX86Thread_EXEAX_OFS equ 136
VmX86Thread_EXEBX_OFS equ 140
VmX86Thread_EXECX_OFS equ 144
VmX86Thread_EXEDX_OFS equ 148
VmX86Thread_EXESI_OFS equ 152
VmX86Thread_EXEDI_OFS equ 156
VmX86Thread_EXEFLAGS_OFS equ 160
VmX86Thread_EXEIP_OFS equ 164
VmX86Thread_EXESP_OFS equ 168
VmX86Thread_EXEBP_OFS equ 172
VmX86Thread_EXCR2_OFS equ 176
VmX86Thread_READWRITEMSRS_OFS equ 180
VmX86Thread_WRITEONLYMSRS_OFS equ 184
VmX86Thread_SIZE equ 188


VmX86Thread_FXF_USED equ 1



VmX86Thread64_R8_OFS equ 188
VmX86Thread64_R9_OFS equ 192
VmX86Thread64_R10_OFS equ 196
VmX86Thread64_R11_OFS equ 200
VmX86Thread64_R13_OFS equ 204
VmX86Thread64_R14_OFS equ 208
VmX86Thread64_R15_OFS equ 212
VmX86Thread64_SIZE equ 216


 







  

  
	
	
 
	

	



  
 
	
	
	
	
	



  
 
	
	
	
	




  
 
	
	
	
	




  
 
	
		


  
	


 





PF_DEFAULT		equ iPF_PRESENT|iPF_WRITE|iPF_USER
PF_DEFAULT_RO	equ iPF_PRESENT|iPF_USER


KERNEL_CS   equ 0x08
KERNEL_DS   equ 0x10
USER_CS     equ 0x1B
USER_DS     equ 0x23
TSS_DS      equ 0x28
CURPROC_FS  equ 0x33






  

	


  
	



  
	



 		
	  			

	  			










    global sys_start








sys_start:
	jmp real_start

	
	align 4
mb_header:
	dd 0x1BADB002				
	dd 0x00010007				
	dd 0-0x1BADB002-0x00010007	
	dd mb_header				
	dd sys_start				
	dd 0 						
	dd 0						
	dd real_start				
	dd 0						
	dd 0						
	dd 0						
	dd 0						

real_start:
	mov esp,Lkernel_esp
	cld
	 
	

	call sys_clrscr32
		


	cmp eax,0x2BADB002
	je multiboot_ok
	jmp no_multiboot_loader

multiboot_ok:
	
	cld
	mov esi,ebx
	mov edi,multiboot_info
	mov ecx,MBI_SIZE
	rep movsb

	
	mov esi,[multiboot_info+MBI_CMDLINE]
	test esi,esi
	jz skip_multiboot_cmdline
	mov edi,multiboot_cmdline
	mov ecx,MBI_CMDLINE_MAX
	rep movsb
skip_multiboot_cmdline:

	mov ebx,[multiboot_info+MBI_MEMUPPER]	
	shl ebx,10			
	add ebx,0x100000	

	
	test dword [multiboot_info+MBI_FLAGS],MBF_MMAP
	jz multiboot_mmap_done
	
	mov esi,[multiboot_info+MBI_MMAPADDR]
	
	mov edi,multiboot_mmap+4
	
	mov edx,esi
	add edx,[multiboot_info+MBI_MMAPLENGTH]
	
	lea esi,[esi+4]	
multiboot_mmap_copy:
	
	mov eax,[esi+MBMMAP_SIZE]
	
	mov ecx,MBMMAP_ESIZE
	push esi
	rep movsb
	pop esi
	
	inc dword [multiboot_mmap]
	
	lea esi,[esi+eax+4]		
	
	
	cmp dword [multiboot_mmap],MBI_MMAP_MAX
	jge multiboot_mmap_done
	cmp esi,edx				
	jb multiboot_mmap_copy
multiboot_mmap_done:


	
	test dword [multiboot_info+MBI_FLAGS],MBF_VBE
	jz vbe_info_done 
	
    
	mov esi,[multiboot_info+MBI_VBECTRLINFO] 
	mov edi,vbe_control_info                 
	mov ecx,VBECTRLINFO_SIZE                 
	rep movsb
	
	
	mov esi,[multiboot_info+MBI_VBEMODEINFO]  
	mov edi,vbe_mode_info                     
	mov ecx,VBEMODEINFO_SIZE                  
	rep movsb	
vbe_info_done:


	
	mov esi,[multiboot_info+MBI_MODSCOUNT]
	test esi,esi
	jz no_initJar
	mov esi,[multiboot_info+MBI_MODSADDR]
	mov eax,[esi+MBMOD_START]
	mov [initJar_start],eax
	mov eax,[esi+MBMOD_END]
	mov [initJar_end],eax
	
	add eax,0x1000
	and eax,~0xfff
	mov [free_mem_start],eax
	jmp initJar_done
	
no_initJar:
	
	mov eax,freeMemoryStart
	mov [initJar_start],eax
	mov [initJar_end],eax
	mov [free_mem_start],eax

initJar_done:
	
	

    
    xor eax,eax
check_a20:
    inc eax
    mov dword [0x0],eax
    cmp eax, dword [0x100000]
    je check_a20 

	
	call kdb_init

	
	 
	
	
	
	
		
	push eax
	mov eax,sys_version
	call sys_print_str32
	pop eax
	

	
	
	 
	
	
	
	
		
	push eax
	mov eax,mbflags_msg
	call sys_print_str32
	pop eax
	

	 
	
	
	
	
		
	push eax
	mov eax,[multiboot_info+MBI_FLAGS]
	call sys_print_eax32
	pop eax


		 
	
	
	
	
		
	push eax
	mov eax,newline_msg
	call sys_print_str32
	pop eax
	



	
	call test_cpuid
	 
	
	
	
	
		
	push eax
	mov eax,cpu_ok_msg
	call sys_print_str32
	pop eax
	


	
	call Lsetup_mm

	
	  
	
		
	
	
	call Lsetup_idt

     
	
	
	
	
		
	push eax
	mov eax,sys_version
	call sys_print_str32
	pop eax
	


	
	call init_fpu
	
	call init_sse

	 
	
	
	
	
		
	push eax
	mov eax,before_start_vm_msg
	call sys_print_str32
	pop eax
	


	
	mov ebx,Luser_esp
	mov ecx,go_user_cs
	mov esi,USER_DS
	mov edi,USER_CS
	push esi			
	push ebx			
	pushf				
	push edi			
	push ecx			
	pushf
	pop eax
	and eax,~F_NT
	push eax
	popf
	iret

	
	

no_multiboot_loader:
     
	
	
	
	
		
	push eax
	mov eax,no_multiboot_loader_msg
	call sys_print_str32
	pop eax
	

    jmp _halt

go_user_cs:
	mov eax,USER_DS
	mov ss,ax
	mov ds,ax
	mov es,ax
	mov gs,ax
	mov eax,CURPROC_FS
	mov fs,ax
	
	mov esp,Luser_esp
 
	
	
	
	mov dword[fs:VmProcessor_KERNELSTACKEND_OFS], (kernel_stack + (VmThread_STACK_OVERFLOW_LIMIT_SLOTS * 4))
	sti

	
	 
		
		
		
		
		
	

	 
		
	

	
	xor ebp,ebp	
	push ebp    
	push ebp    
	push ebp    
	push ebp    
	mov ebp,esp

	mov eax,vm_start
	add eax,BootImageBuilder_JUMP_MAIN_OFFSET32

	
	
	call eax

	mov edx, eax	
	inc dword [jnodeFinished]

	test edx,edx
	jz _halt
	
	
		push eax
	mov eax,0x0F
	out 0x70,al
	mov eax,0x00
	out 0x71,al
	pop eax

	mov bl,0xfe				
	call _kbcmd

_halt:
	cli
	hlt
	jmp _halt
	

_kbcmd:
	in al, 0x64
	test al, 0x02
	jnz _kbcmd
	mov al, bl
	out 0x64, al
_kbcmd_accept:	
	in al, 0x64
	test al, 0x02
	jz _kbcmd_accept
	ret

no_multiboot_loader_msg: db 'No multiboot loader. halt...',0
before_start_vm_msg:     db 'Before start_vm',0xd,0xa,0
after_vm_msg:  			 db 'VM returned with EAX ',0
cpu_ok_msg:			     db 'CPU tested ok',0xd,0xa,0
mbflags_msg:		     db 'Multiboot flags ',0

multiboot_info:
	times MBI_SIZE db  0

multiboot_cmdline:
	times (MBI_CMDLINE_MAX+4) db 0

multiboot_mmap:
	dd 0				
	times (MBI_MMAP_MAX * MBMMAP_ESIZE) db 0


vbe_control_info:	
	times (VBECTRLINFO_SIZE) db 0

vbe_mode_info:	
	times (VBEMODEINFO_SIZE) db 0

 









init_fpu:
	fninit
	
	lea esp,[esp-4]
	fstcw [esp]
	or word [esp], 0x0C00
	fldcw [esp]
	lea esp,[esp+4]
	mov eax,cr0
	or eax,CR0_MP			
	mov eax,cr0
	ret
	

init_sse:
	
	test dword [cpu_features], FEAT_SSE
	jz no_sse
	mov eax,cr4
	or eax,CR4_OSFXSR		
	or eax,CR4_OSXMMEXCPT	
	mov cr4,eax
	mov eax,cr0
	and eax,~CR0_EM			
	or eax,CR0_MP			
	mov eax,cr0
	
	lea esp,[esp-4]
	stmxcsr [esp]
	pop eax
	or eax,MXCSR_IM			
	or eax,MXCSR_DM			
	and eax,~MXCSR_ZM		
	or eax,MXCSR_OM			
	or eax,MXCSR_UM			
	or eax,MXCSR_PM			
	push eax
	ldmxcsr [esp]					
	lea esp,[esp+4]
	ret
	
no_sse:
	ret	

	

 








	bits 32
 













 
 
 
 
 
 
 
 
 
 
 

	 			
	 	
	 		
	
	 			
	 			
	 			
	 			
	 			
	 			
	 			
	 			

 
	 			
	 	
	 		
	 			
	 			
	 			
	 			
	 			
	 			
	 			
	 			

	


 
cpu_features:	dd 0


test_cpuid:
	push eax
	push ebx
	push ecx
	push edx
	
	
	pushf			
	pop eax
	mov ecx,eax
	xor eax,F_ID	
	push eax
	popf 
	pushf
	pop eax			
	xor eax,ecx		
	jz no_cpuid
	
	mov eax,1
	cpuid
	
	mov [cpu_features],edx
	
	test edx,FEAT_FPU
	jz no_fpu_feat
	test edx,FEAT_PSE
	jz no_pse_feat
	
	
	pop edx
	pop ecx
	pop ebx
	pop eax
	ret

no_cpuid:
	 
	
	
	
	
		
	push eax
	mov eax,no_cpuid_msg
	call sys_print_str32
	pop eax
	

	__jnasm_macro_local_label_0:
	jmp __jnasm_macro_local_label_0

	
no_fpu_feat:	
	 
	
	
	
	
		
	push eax
	mov eax,no_fpu_feat_msg
	call sys_print_str32
	pop eax
	

	__jnasm_macro_local_label_1:
	jmp __jnasm_macro_local_label_1

	
no_pse_feat:	
	 
	
	
	
	
		
	push eax
	mov eax,no_pse_feat_msg
	call sys_print_str32
	pop eax
	

	__jnasm_macro_local_label_2:
	jmp __jnasm_macro_local_label_2

	
no_cpuid_msg:		db 'Processor has no CPUID. halt...',0
no_pse_feat_msg:	db 'Processor has no PSE support. halt...',0
no_fpu_feat_msg:	db 'Processor has no FPU support. halt...',0
	
 
	
	 
	 
	

 	 		
  








 		
 	
 	
 	
    
int_die_halted:	dd 0    
    




OLD_SS      equ 72
OLD_ESP     equ 68
OLD_EFLAGS  equ 64
OLD_CS      equ 60
OLD_EIP     equ 56
ERROR       equ 52
INTNO   	equ 48
HANDLER     equ 44 
OLD_EAX     equ 40
OLD_ECX     equ 36
OLD_EDX     equ 32
OLD_EBX     equ 28
OLD_EBP     equ 24
OLD_ESI     equ 20
OLD_EDI     equ 16
OLD_DS      equ 12
OLD_ES      equ 8
OLD_FS      equ 4
OLD_GS      equ 0

 		
 		
 		
 	
 		
 		
 		
 		

  
	
	
	
	
	
	
	
    
    
    
    
    
    
    
    
    
     
		
	
    
	
	


  
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    





inthandler:
    	push eax
	push ecx
	push edx
	push ebx
	push ebp
	push esi
	push edi
    push ds
    push es
    push fs
    push gs
    mov eax,KERNEL_DS
    mov ds,ax
    mov es,ax
    mov gs,ax
    
     
		
	
    cld
	test dword [int_die_halted],0xffffffff
	jnz near int_die_halt

	mov eax,esp
	mov ebp,esp
	cmp dword [ebp+OLD_CS],USER_CS
	jne near kernel_panic
	mov ebx,[esp+HANDLER]
	call ebx
	test dword[fs:VmX86Processor_RESUME_INT_OFS],0xFFFFFFFF
	jz inthandler_ret
	
	
	mov ebp,esp
	mov eax,dword[fs:VmX86Processor_RESUME_INTNO_OFS]
	mov [ebp+INTNO],eax
	mov eax,dword[fs:VmX86Processor_RESUME_ERROR_OFS]
	mov [ebp+ERROR],eax
	mov eax,dword[fs:VmX86Processor_RESUME_HANDLER_OFS]
	mov [ebp+HANDLER],eax
	mov dword[fs:VmX86Processor_RESUME_INT_OFS],0
	jmp irqhandler_resume
inthandler_ret:
        mov eax,esp
    and dword [esp+OLD_EFLAGS],~F_NT 
    and byte [gdt_tss+5],~0x02       
    pop gs
    pop fs
    pop es
    pop ds
    pop edi
    pop esi
    pop ebp
    pop ebx
    pop edx
    pop ecx
    pop eax
    add esp,12 
    iret

    





int_die:
	cli
	mov ebx,ebp
		push eax
	mov eax,dword [die_lock]
	test eax,eax
	pop eax
	jnz int_die_halt

		push eax
__jnasm_macro_local_label_3:	
	mov eax,1
	lock xchg eax,dword [die_lock]
	test eax,eax
	jnz __jnasm_macro_local_label_3
	pop eax

	call sys_print_intregs
	
int_die_halt:
	cli
	mov dword [int_die_halted],1
	 
	
	
	
	
		
	push eax
	mov eax,int_die_halt_msg
	call sys_print_str32
	pop eax
	

	 
	
	
	
	
		
	push eax
	mov eax,dword[fs:VmProcessor_ID_OFS]
	call sys_print_eax32
	pop eax


	hlt

int_die_halt_msg: db 'Real panic: int_die_halt! ',0

  
	
	


  
	
	
	


sys_print_intregs:
		 
	
	
	
	
		
	push eax
	mov eax,idm_procid
	call sys_print_str32
	pop eax
	

	 
	
	
	
	
		
	push eax
	mov eax,dword[fs:VmProcessor_ID_OFS]
	call sys_print_eax32
	pop eax



		 
	
	
	
	
		
	push eax
	mov eax,idm_intno
	call sys_print_str32
	pop eax
	

	 
	
	
	
	
		
	push eax
	mov eax,[ebp+INTNO]
	call sys_print_eax32
	pop eax



		 
	
	
	
	
		
	push eax
	mov eax,idm_error
	call sys_print_str32
	pop eax
	

	 
	
	
	
	
		
	push eax
	mov eax,[ebp+ERROR]
	call sys_print_eax32
	pop eax



		 
	
	
	
	
		
	push eax
	mov eax,idm_cr2
	call sys_print_str32
	pop eax
	

	 
	
	
	
	
		
	push eax
	mov eax,cr2
	call sys_print_eax32
	pop eax



		 
	
	
	
	
		
	push eax
	mov eax,idm_cr3
	call sys_print_str32
	pop eax
	

	 
	
	
	
	
		
	push eax
	mov eax,cr3
	call sys_print_eax32
	pop eax



		 
	
	
	
	
		
	push eax
	mov eax,idm_eip
	call sys_print_str32
	pop eax
	

	 
	
	
	
	
		
	push eax
	mov eax,[ebp+OLD_EIP]
	call sys_print_eax32
	pop eax



		 
	
	
	
	
		
	push eax
	mov eax,idm_cs
	call sys_print_str32
	pop eax
	

	 
	
	
	
	
		
	push eax
	mov eax,[ebp+OLD_CS]
	call sys_print_eax32
	pop eax



		 
	
	
	
	
		
	push eax
	mov eax,idm_eflags
	call sys_print_str32
	pop eax
	

	 
	
	
	
	
		
	push eax
	mov eax,[ebp+OLD_EFLAGS]
	call sys_print_eax32
	pop eax



		 
	
	
	
	
		
	push eax
	mov eax,idm_cr0
	call sys_print_str32
	pop eax
	

	 
	
	
	
	
		
	push eax
	mov eax,cr0
	call sys_print_eax32
	pop eax



		 
	
	
	
	
		
	push eax
	mov eax,idm_eax
	call sys_print_str32
	pop eax
	

	 
	
	
	
	
		
	push eax
	mov eax,[ebp+OLD_EAX]
	call sys_print_eax32
	pop eax



		 
	
	
	
	
		
	push eax
	mov eax,idm_ebx
	call sys_print_str32
	pop eax
	

	 
	
	
	
	
		
	push eax
	mov eax,[ebp+OLD_EBX]
	call sys_print_eax32
	pop eax



		 
	
	
	
	
		
	push eax
	mov eax,idm_ecx
	call sys_print_str32
	pop eax
	

	 
	
	
	
	
		
	push eax
	mov eax,[ebp+OLD_ECX]
	call sys_print_eax32
	pop eax



		 
	
	
	
	
		
	push eax
	mov eax,idm_edx
	call sys_print_str32
	pop eax
	

	 
	
	
	
	
		
	push eax
	mov eax,[ebp+OLD_EDX]
	call sys_print_eax32
	pop eax



		 
	
	
	
	
		
	push eax
	mov eax,idm_ebp
	call sys_print_str32
	pop eax
	

	 
	
	
	
	
		
	push eax
	mov eax,[ebp+OLD_EBP]
	call sys_print_eax32
	pop eax



		 
	
	
	
	
		
	push eax
	mov eax,idm_esp
	call sys_print_str32
	pop eax
	

	 
	
	
	
	
		
	push eax
	mov eax,[ebp+OLD_ESP]
	call sys_print_eax32
	pop eax



		 
	
	
	
	
		
	push eax
	mov eax,idm_edi
	call sys_print_str32
	pop eax
	

	 
	
	
	
	
		
	push eax
	mov eax,[ebp+OLD_EDI]
	call sys_print_eax32
	pop eax



		 
	
	
	
	
		
	push eax
	mov eax,idm_esi
	call sys_print_str32
	pop eax
	

	 
	
	
	
	
		
	push eax
	mov eax,[ebp+OLD_ESI]
	call sys_print_eax32
	pop eax



		 
	
	
	
	
		
	push eax
	mov eax,idm_ds
	call sys_print_str32
	pop eax
	

	 
	
	
	
	
		
	push eax
	mov eax,[ebp+OLD_DS]
	call sys_print_eax32
	pop eax



		 
	
	
	
	
		
	push eax
	mov eax,idm_es
	call sys_print_str32
	pop eax
	

	 
	
	
	
	
		
	push eax
	mov eax,[ebp+OLD_ES]
	call sys_print_eax32
	pop eax



		 
	
	
	
	
		
	push eax
	mov eax,idm_fs
	call sys_print_str32
	pop eax
	

	 
	
	
	
	
		
	push eax
	mov eax,[ebp+OLD_FS]
	call sys_print_eax32
	pop eax



		 
	
	
	
	
		
	push eax
	mov eax,idm_gs
	call sys_print_str32
	pop eax
	

	 
	
	
	
	
		
	push eax
	mov eax,[ebp+OLD_GS]
	call sys_print_eax32
	pop eax



	mov ebx,[ebp+OLD_ESP]
		 
	
	
	
	
		
	push eax
	mov eax,idm_stack0
	call sys_print_str32
	pop eax
	

	 
	
	
	
	
		
	push eax
	mov eax,[ebx+0]
	call sys_print_eax32
	pop eax



		 
	
	
	
	
		
	push eax
	mov eax,idm_stack1
	call sys_print_str32
	pop eax
	

	 
	
	
	
	
		
	push eax
	mov eax,[ebx+4]
	call sys_print_eax32
	pop eax



		 
	
	
	
	
		
	push eax
	mov eax,idm_stack1
	call sys_print_str32
	pop eax
	

	 
	
	
	
	
		
	push eax
	mov eax,[ebx+8]
	call sys_print_eax32
	pop eax



		 
	
	
	
	
		
	push eax
	mov eax,idm_stack1
	call sys_print_str32
	pop eax
	

	 
	
	
	
	
		
	push eax
	mov eax,[ebx+12]
	call sys_print_eax32
	pop eax



		 
	
	
	
	
		
	push eax
	mov eax,idm_stack1
	call sys_print_str32
	pop eax
	

	 
	
	
	
	
		
	push eax
	mov eax,[ebx+16]
	call sys_print_eax32
	pop eax



		 
	
	
	
	
		
	push eax
	mov eax,idm_stack1
	call sys_print_str32
	pop eax
	

	 
	
	
	
	
		
	push eax
	mov eax,[ebx+20]
	call sys_print_eax32
	pop eax



	mov ebx,[ebp+OLD_EIP]
	
	
	push eax
	mov eax,cr2
	cmp ebx,eax
	pop eax
	jne sys_print_intregs_code
	mov ebx,[ebp+OLD_ESP]
	mov ebx,[ebx+0]
sys_print_intregs_code:
	sub ebx,16
		 
	
	
	
	
		
	push eax
	mov eax,idm_ipaddr
	call sys_print_str32
	pop eax
	

	 
	
	
	
	
		
	push eax
	mov eax,ebx
	call sys_print_eax32
	pop eax



		 
	
	
	
	
		
	push eax
	mov eax,idm_ip0
	call sys_print_str32
	pop eax
	

	movzx eax,byte [ebx+0]
	call sys_print_al32

	push ecx
	mov ecx,15
sys_print_intregs_loop1:
	inc ebx
		 
	
	
	
	
		
	push eax
	mov eax,idm_ip1
	call sys_print_str32
	pop eax
	

	movzx eax,byte [ebx]
	call sys_print_al32

	loop sys_print_intregs_loop1
	
	inc ebx
		 
	
	
	
	
		
	push eax
	mov eax,idm_ipaddr
	call sys_print_str32
	pop eax
	

	 
	
	
	
	
		
	push eax
	mov eax,ebx
	call sys_print_eax32
	pop eax



		 
	
	
	
	
		
	push eax
	mov eax,idm_ip0
	call sys_print_str32
	pop eax
	

	movzx eax,byte [ebx+0]
	call sys_print_al32

	mov ecx,15
sys_print_intregs_loop2:
	inc ebx
		 
	
	
	
	
		
	push eax
	mov eax,idm_ip1
	call sys_print_str32
	pop eax
	

	movzx eax,byte [ebx]
	call sys_print_al32

	loop sys_print_intregs_loop2
	pop ecx
	ret

idm_procid: db 0xd,0xa,'proid: ',0
idm_intno:  db 0xd,0xa,'int  : ',0
idm_error:  db        ' Error: ',0
idm_cr2:    db        ' CR2  : ',0
idm_cr3:    db        ' CR3  : ',0
idm_eip:    db 0xd,0xa,'EIP  : ',0
idm_cs:     db        ' CS   : ',0
idm_eflags: db        ' FLAGS: ',0
idm_cr0:	db        ' CR0  : ',0
idm_eax:    db 0xd,0xa,'EAX  : ',0
idm_ebx:    db        ' EBX  : ',0
idm_ecx:    db        ' ECX  : ',0
idm_edx:    db        ' EDX  : ',0
idm_ebp:    db 0xd,0xa,'EBP  : ',0
idm_esp:    db        ' ESP  : ',0
idm_edi:    db        ' EDI  : ',0
idm_esi:    db        ' ESI  : ',0
idm_ds:     db 0xd,0xa,'DS   : ',0
idm_es:     db        ' ES   : ',0
idm_fs:     db        ' FS   : ',0
idm_gs:     db        ' GS   : ',0
idm_stack0: db 0xd,0xa,'STACK: ',0
idm_stack1: db        0
idm_ipaddr: db 0xd,0xa,'CODE(',0
idm_ip0:    db        '): ',0
idm_ip1:    db        0





irqhandler:
    	push eax
	push ecx
	push edx
	push ebx
	push ebp
	push esi
	push edi
    push ds
    push es
    push fs
    push gs
    mov eax,KERNEL_DS
    mov ds,ax
    mov es,ax
    mov gs,ax
    
     
		
	
    cld
	test dword [int_die_halted],0xffffffff
	jnz near int_die_halt

	mov eax,esp
	mov ebp,esp
	cmp dword [esp+OLD_CS],USER_CS
	jne irqhandler_suspend
irqhandler_resume:
	call dword [esp+HANDLER]
irqhandler_ret:
	 
		
	
	    mov eax,esp
    and dword [esp+OLD_EFLAGS],~F_NT 
    and byte [gdt_tss+5],~0x02       
    pop gs
    pop fs
    pop es
    pop ds
    pop edi
    pop esi
    pop ebp
    pop ebx
    pop edx
    pop ecx
    pop eax
    add esp,12 
    iret

irqhandler_suspend:
	
	and dword [esp+OLD_EFLAGS],~F_IF
	mov eax,[esp+INTNO]
	mov dword[fs:VmX86Processor_RESUME_INTNO_OFS],eax
	mov eax,[esp+ERROR]
	mov dword[fs:VmX86Processor_RESUME_ERROR_OFS],eax
	mov eax,[esp+HANDLER]
	mov dword[fs:VmX86Processor_RESUME_HANDLER_OFS],eax
	mov eax,1
	xchg dword[fs:VmX86Processor_RESUME_INT_OFS],eax
	
	test eax,eax
	jz inthandler_ret 
	 
	
	
	
	
		
	push eax
	mov eax,irq_resume_overrun_msg
	call sys_print_str32
	pop eax
	

	jmp inthandler_ret

irq_suspend_msg: db 'IRQ suspend!',0
irq_resume_overrun_msg: db 'IRQ resume overrun!',0



  

	
	
	
	




  

	
	
	




  

	
	
	
	



  
	
	
	
	



  
	
	
	
	











setup_idtentry:
	mov word [edi+0],ax
	shr eax,16
	mov word [edi+2],KERNEL_CS
	mov word [edi+4],bx
	mov word [edi+6],ax
	ret




idt:
	dw idtend-idtstart
	dd idtstart

idtstart:
	times (0x40)*8 db 0
idtend:







  
	
	
	









int_gpf:
	mov eax,dword [ebp+OLD_EIP]
	cmp byte [eax],0xf4 
	jne int_gpf_2
	
int_gpf_hlt:
	cmp eax,_halt
	je int_gpf_halt
	inc dword [ebp+OLD_EIP] 			
	test dword [ebp+OLD_EFLAGS],F_IF
	jz int_gpf_1
	sti
int_gpf_1:
	hlt
	ret
int_gpf_2:
	jmp int_die

int_gpf_halt:
	cli
	hlt
	jmp int_die
	



int_pf:
	cmp dword [ebp+OLD_CS],USER_CS
	jne int_pf_kernel
	mov eax,cr2
	test eax,0xFFFFF000 
	jz int_pf_npe
	neg eax
	test eax,0xFFFFF000 
	jz int_pf_npe
		mov eax,VmThread_EX_PAGEFAULT
	mov ebx,cr2
	call int_system_exception

	ret
int_pf_npe:
	
		mov eax,VmThread_EX_NULLPOINTER
	mov ebx,dword [ebp+OLD_EIP]
	call int_system_exception

	ret
	
int_pf_kernel:
	jmp int_die
	

   








    global Lkernel_esp
    global Lsetup_mm 


pd_paddr	equ 0x00001000	
pg0_paddr	equ 0x00002000	
pg1_paddr	equ 0x00003000	
free_paddr	equ 0x00004000	

mem_start	dd 0	
mem_size	dd 0	

initJar_start	dd 0	
initJar_end		dd 0	

free_mem_start	dd 0	





Lsetup_mm:



	cmp ebx,0x7fffffff
	jna Lmem_size_ok
	mov ebx,0x7fffffff
Lmem_size_ok:

	mov [mem_size],ebx
	mov eax,dword[configbase+20]	
	
	add eax,0x1000
	and eax,~0xfff
	mov [mem_start],eax

	
	 
	
	
	
	
		
	push eax
	mov eax,mem_start_str
	call sys_print_str32
	pop eax
	

	 
	
	
	
	
		
	push eax
	mov eax,[mem_start]
	call sys_print_eax32
	pop eax


	 
	
	
	
	
		
	push eax
	mov eax,mem_size_str
	call sys_print_str32
	pop eax
	

	 
	
	
	
	
		
	push eax
	mov eax,[mem_size]
	call sys_print_eax32
	pop eax







    mov eax,tss
    pushf
    pop dword [eax+0x24]
    mov word [gdt_tss+2],ax
    shr eax,16
    mov byte [gdt_tss+4],al
    mov byte [gdt_tss+7],ah





    mov eax,vmCurProcessor
    mov word [gdt_curProc+2],ax
    shr eax,16
    mov byte [gdt_curProc+4],al
    mov byte [gdt_curProc+7],ah





    lgdt [gdt]
	jmp dword KERNEL_CS:mm_gdt_flush
mm_gdt_flush:





    mov eax,0x28
    ltr ax
    
    pushf
    pop eax
    or eax,F_IOPL1 | F_IOPL2
    push eax
    popf
    jmp mm_ltr_flush
mm_ltr_flush:










     
	
	
	
	
		
	push eax
	mov eax,init_pd_msg
	call sys_print_str32
	pop eax
	



	cld
	mov eax,PF_DEFAULT|iPF_PSE
	mov edi,pd_paddr
	mov ecx,1024
pd_lp:
	stosd				
	add eax,0x400000	
	loop pd_lp


	xor eax,eax
	mov edi,pg0_paddr
	mov ecx,1024
pg0_lp:
	and eax,iPF_ADDRMASK	
	
	cmp eax,kernel_begin
	jb pg0_rw
	cmp eax,kernel_end
	jae pg0_rw
	or eax,PF_DEFAULT_RO	
	jmp pg0_1
pg0_rw:
	or eax,PF_DEFAULT		
pg0_1:
	stosd					
	add eax,0x1000			
	loop pg0_lp
	

	mov edi,pg0_paddr
	and dword [edi],0
	

	mov eax,pg0_paddr
	mov edi,pd_paddr
	or eax,PF_DEFAULT
	mov [edi],eax
	

	mov edi,pd_paddr
	and dword [edi+(1023*4)],0

	
     
	
	
	
	
		
	push eax
	mov eax,enable_pg_msg
	call sys_print_str32
	pop eax
	

    
    call enable_paging



     
	
	
	
	
		
	push eax
	mov eax,done_pg_msg
	call sys_print_str32
	pop eax
	


	ret
	




init_pt:
	push eax
	push ebx
	push edx
	push edi
	
	
	
	
	xor eax,eax
init_pt_lp:
    
	mov ebx,eax
	shl ebx,12
	add ebx,edx
	
	cmp ebx,kernel_begin
	jb init_pt_rw
	cmp ebx,kernel_end
	jae init_pt_rw
	or ebx,PF_DEFAULT_RO 
	jmp init_pt_1
init_pt_rw:
	or ebx,PF_DEFAULT    
init_pt_1:
	mov [edi+eax*4],ebx
	inc eax
	cmp eax,1024
	jne init_pt_lp
	
	
	mov ebx,pd_paddr
	shr edx,22 
	or edi,PF_DEFAULT
	mov [ebx+edx*4],edi
	
	pop edi
	pop edx
	pop ebx
	pop eax
	ret


enable_paging:
	
	mov eax,pd_paddr
	mov cr3,eax
	
	mov eax,cr4
	or eax,CR4_PSE
	mov cr4,eax
	
	mov eax,cr0
	or eax,CR0_PG
	mov cr0,eax
	
	jmp enable_pg_flush
enable_pg_flush:
	ret


disable_paging:
	
	mov eax,cr0
	and eax,~CR0_PG
	mov cr0,eax
	
	jmp disable_pg_flush
disable_pg_flush:
	ret


mem_start_str:	db 'mem-start ',0
mem_size_str:	db 0xd,0xa,'mem-size ',0
init_pd_msg:    db 'init page-dir table',0xd,0xa,0
init_pg0_msg:   db 'init page 0 table',0xd,0xa,0
enable_pg_msg:  db 'enable paging',0xd,0xa,0
done_pg_msg:    db 'paging setup finished',0xd,0xa,0





gdt:
	dw 56-1
	dd gdtstart

gdtstart:
    
	dd 0 
	dd 0

    
    
	dw 0ffffh 
	dw 0 
	db 0 
	db 9ah 
	db 0cfh 
	db 0 

    
    
	dw 0ffffh
	dw 0
	db 0
	db 92h 
	db 0cfh
	db 0

    
    
	dw 0ffffh 
	dw 0 
	db 0 
	db 0xFA 
	db 0xCF 
	db 0 

    
    
	dw 0ffffh
	dw 0
	db 0
	db 0xF2 
	db 0xCF
	db 0

    
    
gdt_tss:
    dw tss_e-tss
    dw 0 
    db 0 
    db 0x89
    db 0
    db 0 

    
    
gdt_curProc:
    dw VmX86Processor_SIZE
    dw 0 
    db 0 
	db 0xF2 
    db 0
    db 0 
gdtend:





tss:
    
    dd 0           
    dd Lkernel_esp 
    dw KERNEL_DS   
    dw 0           
    dd 0           
    
    dd 0           
    dd 0           
    dd 0           
    dd pd_paddr    
    
    dd 0           
    dd 0           
    dd 0           
    dd 0           
    
    dd 0           
    dd 0           
    dd Luser_esp   
    dd 0           
    
    dd 0           
    dd 0           
    dd USER_DS     
    dd USER_CS     
    
    dd USER_DS     
    dd USER_DS     
    dd USER_DS     
    dd USER_DS     
    
    dd 0           
    dw 0           
    dw 0xFFFF      
tss_e:

kernel_stack:
    
    times 8*1024 dd 0
Lkernel_esp:
    dd 0


   
	 		
   









  
    global sys_clrscr       
    global sys_print_eax    
    global sys_print_char   
    global sys_print_str    

scr_width		equ 80
scr_height		equ 25
scr_addr		equ 0xb8000
CR				equ 0x0D
LF				equ 0x0A
video_prt_reg	equ 0x3d4  
video_prt_val	equ 0x3d5
video_mode_reg	equ 0x3d8

  
	
	
	
	
	


  


	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	



	
	
	
	
	
	
	
	
	
	
	



	
	
	
	
	
	
	
	
	
	
	
	
	
	
	



	
	
	
	




	
	
	
	
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	

	
	
	
	
	
	
	
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	
	
	



	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

 


	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	




	
	
	
	
	
	
	
	
	
	
	
	



	
	
	

	
	
	
	
	
	

	
	
	


	
set_cursor32:
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


hide_cursor32:
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


sys_clrscr32:
		push eax
__jnasm_macro_local_label_12:	
	mov eax,1
	lock xchg eax,dword [console_lock]
	test eax,eax
	jnz __jnasm_macro_local_label_12
	pop eax

	push eax
	push ecx
	push edi
	mov ecx,scr_width*scr_height
	mov edi,scr_addr
	mov eax,0x0720
	rep stosw
	mov dword [scr_ofs],0
	call hide_cursor32
	pop edi
	pop ecx
	pop eax
		push eax
	xor eax,eax
	mov dword [console_lock],eax
	pop eax

	ret


sys_print_char32:
		push eax
__jnasm_macro_local_label_13:	
	mov eax,1
	lock xchg eax,dword [console_lock]
	test eax,eax
	jnz __jnasm_macro_local_label_13
	pop eax

	call sys_do_print_char32
		push eax
	xor eax,eax
	mov dword [console_lock],eax
	pop eax

	ret



sys_do_print_char32:
	cmp al,LF
	je __jnasm_macro_local_label_9
	cmp al,CR
	jne __jnasm_macro_local_label_11
	jmp __jnasm_macro_local_label_8
__jnasm_macro_local_label_9:
	
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
	jmp __jnasm_macro_local_label_7
__jnasm_macro_local_label_11:
	push edi
	mov edi,[scr_ofs]
	shl edi,1
	add edi,scr_addr
	mov ah,0x08 
	mov [edi],ax
	inc dword [scr_ofs]
	pop edi
__jnasm_macro_local_label_7:
	cmp dword [scr_ofs],(scr_width*scr_height)
	jne __jnasm_macro_local_label_8
	
	push edi
	push esi
	push ecx
	mov edi,scr_addr
	mov esi,edi
	add esi,(scr_width*2)
	mov ecx,(scr_width*(scr_height-1))
	rep movsw
	
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
__jnasm_macro_local_label_8:
	call set_cursor32
	call kdb_send_char32
	ret


sys_print_eax32:
		push eax
__jnasm_macro_local_label_14:	
	mov eax,1
	lock xchg eax,dword [console_lock]
	test eax,eax
	jnz __jnasm_macro_local_label_14
	pop eax

	push eax
	push ebx
	mov ebx,eax
		mov eax,ebx
	shr eax,28
	and eax,0x0f
	mov al,[eax+hexchars]
	call sys_do_print_char32

		mov eax,ebx
	shr eax,24
	and eax,0x0f
	mov al,[eax+hexchars]
	call sys_do_print_char32

		mov eax,ebx
	shr eax,20
	and eax,0x0f
	mov al,[eax+hexchars]
	call sys_do_print_char32

		mov eax,ebx
	shr eax,16
	and eax,0x0f
	mov al,[eax+hexchars]
	call sys_do_print_char32

		mov eax,ebx
	shr eax,12
	and eax,0x0f
	mov al,[eax+hexchars]
	call sys_do_print_char32

		mov eax,ebx
	shr eax,8
	and eax,0x0f
	mov al,[eax+hexchars]
	call sys_do_print_char32

		mov eax,ebx
	shr eax,4
	and eax,0x0f
	mov al,[eax+hexchars]
	call sys_do_print_char32

		mov eax,ebx
	shr eax,0
	and eax,0x0f
	mov al,[eax+hexchars]
	call sys_do_print_char32

	mov al,' '
	call sys_do_print_char32
	pop ebx
	pop eax
		push eax
	xor eax,eax
	mov dword [console_lock],eax
	pop eax

	ret

 


	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	



sys_print_al32:
		push eax
__jnasm_macro_local_label_4:	
	mov eax,1
	lock xchg eax,dword [console_lock]
	test eax,eax
	jnz __jnasm_macro_local_label_4
	pop eax

	push eax
	push ebx
	mov ebx,eax
		mov eax,ebx
	shr eax,4
	and eax,0x0f
	mov al,[eax+hexchars]
	call sys_do_print_char32

		mov eax,ebx
	shr eax,0
	and eax,0x0f
	mov al,[eax+hexchars]
	call sys_do_print_char32

	mov al,' '
	call sys_do_print_char32
	pop ebx
	pop eax
		push eax
	xor eax,eax
	mov dword [console_lock],eax
	pop eax

	ret


sys_print_str32:
		push eax
__jnasm_macro_local_label_5:	
	mov eax,1
	lock xchg eax,dword [console_lock]
	test eax,eax
	jnz __jnasm_macro_local_label_5
	pop eax

	push esi
	mov esi,eax
__jnasm_macro_local_label_10:
	mov al,[esi]
	cmp al,0
	je __jnasm_macro_local_label_6
	inc esi
	call sys_do_print_char32
	jmp __jnasm_macro_local_label_10
__jnasm_macro_local_label_6:
	pop esi
		push eax
	xor eax,eax
	mov dword [console_lock],eax
	pop eax

	ret


	 
	 
	
	 
	
	 
	 
	
	 
	

	
 







  
  
	

	
		
  
  
  


	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	
	
	
	
	
	

	
	
	
	
	
	
	
	

  
    



	
	
	
	
	
	
	
	

	
	
	
	

	
	
	
	
	
	
	
	
	
	
	
	

	

	
	
	



    




	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	
	
	

	
	



	
kdb_init:
	push eax
	push ecx
	push edx
	push esi
	
	mov eax,0x400				
	movzx edx,word [eax+0x00]	
	test edx,edx				
	jz __jnasm_macro_local_label_15
	mov [kdb_port],edx
	
	mov edx,[kdb_port]
	add edx,4					
	mov eax,0x03				
	out dx,al
	
	
	mov ecx,MBI_CMDLINE_MAX
	mov esi,multiboot_cmdline
	mov eax,' kdb'
__jnasm_macro_local_label_17:
	cmp [esi],eax
	je __jnasm_macro_local_label_16
	inc esi
	loop __jnasm_macro_local_label_17
	jmp __jnasm_macro_local_label_15
	
__jnasm_macro_local_label_16:
	inc dword [kdb_enabled]
	
__jnasm_macro_local_label_15:	
	pop esi
	pop edx
	pop ecx
	pop eax
	ret  

	

kdb_send_char32:
	push ebx
	push edx
	test byte [kdb_enabled],0x1
	jz __jnasm_macro_local_label_18
	
	mov ebx,eax			
	mov edx,[kdb_port]	
	add edx,5
__jnasm_macro_local_label_20:
	inc edx				
	in al, dx	
		jmp __jnasm_macro_local_label_21
__jnasm_macro_local_label_21:
	jmp __jnasm_macro_local_label_22
__jnasm_macro_local_label_22:		

	test al,0x80		

	dec edx				
	in al,dx
		jmp __jnasm_macro_local_label_23
__jnasm_macro_local_label_23:
	jmp __jnasm_macro_local_label_24
__jnasm_macro_local_label_24:		

	test al,0x20		
	jz __jnasm_macro_local_label_20
	
	mov edx,[kdb_port]
	mov eax,ebx
	out dx,al           
		jmp __jnasm_macro_local_label_25
__jnasm_macro_local_label_25:
	jmp __jnasm_macro_local_label_26
__jnasm_macro_local_label_26:		

	jmp __jnasm_macro_local_label_18
	
__jnasm_macro_local_label_19:
	
__jnasm_macro_local_label_18:
	pop edx
	pop ebx
	ret


	


kdb_recv_char:
	push edx
	xor eax,eax
	test byte [kdb_enabled],0x1
	jz __jnasm_macro_local_label_28
	
	mov edx,[kdb_port]	
	add edx,5			
	in al, dx			
	test al,1			
	jz __jnasm_macro_local_label_28
		jmp __jnasm_macro_local_label_29
__jnasm_macro_local_label_29:
	jmp __jnasm_macro_local_label_30
__jnasm_macro_local_label_30:		

	sub edx,5			
	in al,dx
	jmp __jnasm_macro_local_label_27
	
__jnasm_macro_local_label_28:
	xor eax,eax
	dec eax	
	
__jnasm_macro_local_label_27:
	pop edx
	ret



	 
	 
	
	 
	
	
	 
	 
	
	 
	
	

		
 








    global Lsetup_idt
    
kernel_panic:
	 
	
	
	
	
		
	push eax
	mov eax,kernel_panic_msg
	call sys_print_str32
	pop eax
	

	jmp int_die
	
kernel_irq_panic:
	 
	
	
	
	
		
	push eax
	mov eax,kernel_irq_panic_msg
	call sys_print_str32
	pop eax
	

	jmp int_die
	
kernel_panic_msg: db 'Kernel panic!',0xd,0xa,0
kernel_irq_panic_msg: db 'Kernel panic in IRQ!',0xd,0xa,0





stub_int_div:
	push dword 0   
	push dword 0  
	push dword int_div  
	jmp inthandler

stub_int_debug:
	push dword 0   
	push dword 1  
	push dword int_debug  
	jmp inthandler

stub_int_nmi:
	push dword 0   
	push dword 2  
	push dword int_nmi  
	jmp inthandler

stub_int_bp:
	push dword 0   
	push dword 3  
	push dword int_bp  
	jmp inthandler

stub_int_of:
	push dword 0   
	push dword 4  
	push dword int_of  
	jmp inthandler

stub_int_bc:
	push dword 0   
	push dword 5  
	push dword int_bc  
	jmp inthandler

stub_int_inv_oc:
	push dword 0   
	push dword 6  
	push dword int_inv_oc  
	jmp inthandler

stub_int_dev_na:
	push dword 0   
	push dword 7  
	push dword int_dev_na  
	jmp inthandler

stub_int_df:
	push dword 8   
	push dword int_df   
	jmp inthandler

stub_int_copro_or:
	push dword 0   
	push dword 9  
	push dword int_copro_or  
	jmp inthandler

stub_int_inv_tss:
	push dword 10   
	push dword int_inv_tss   
	jmp inthandler

stub_int_snp:
	push dword 11   
	push dword int_snp   
	jmp inthandler

stub_int_sf:
	push dword 12   
	push dword int_sf   
	jmp inthandler

stub_int_gpf:
	push dword 13   
	push dword int_gpf   
	jmp inthandler

stub_int_pf:
	push dword 14   
	push dword int_pf   
	jmp inthandler

stub_int_copro_err:
	push dword 0   
	push dword 16  
	push dword int_copro_err  
	jmp inthandler

stub_int_alignment:
	push dword 17   
	push dword int_alignment   
	jmp inthandler

stub_int_mce:
	push dword 0   
	push dword 18  
	push dword int_mce  
	jmp inthandler

stub_int_xf:
	push dword 0   
	push dword 19  
	push dword int_xf  
	jmp inthandler

stub_int_stack_overflow:
	push dword 0   
	push dword 0x31  
	push dword int_stack_overflow  
	jmp inthandler






stub_irq0:
	push dword 0  
	push dword 0 
	push dword timer_handler 
	jmp irqhandler

stub_irq1:
	push dword 0  
	push dword 1 
	push dword def_irq_handler 
	jmp irqhandler

stub_irq2:
	push dword 0  
	push dword 2 
	push dword def_irq_handler 
	jmp irqhandler

stub_irq3:
	push dword 0  
	push dword 3 
	push dword def_irq_handler 
	jmp irqhandler

stub_irq4:
	push dword 0  
	push dword 4 
	push dword def_irq_handler 
	jmp irqhandler

stub_irq5:
	push dword 0  
	push dword 5 
	push dword def_irq_handler 
	jmp irqhandler

stub_irq6:
	push dword 0  
	push dword 6 
	push dword def_irq_handler 
	jmp irqhandler

stub_irq7:
	push dword 0  
	push dword 7 
	push dword def_irq_handler 
	jmp irqhandler

stub_irq8:
	push dword 0  
	push dword 8 
	push dword def_irq_handler 
	jmp irqhandler

stub_irq9:
	push dword 0  
	push dword 9 
	push dword def_irq_handler 
	jmp irqhandler

stub_irq10:
	push dword 0  
	push dword 10 
	push dword def_irq_handler 
	jmp irqhandler

stub_irq11:
	push dword 0  
	push dword 11 
	push dword def_irq_handler 
	jmp irqhandler

stub_irq12:
	push dword 0  
	push dword 12 
	push dword def_irq_handler 
	jmp irqhandler

stub_irq13:
	push dword 0  
	push dword 13 
	push dword def_irq_handler 
	jmp irqhandler

stub_irq14:
	push dword 0  
	push dword 14 
	push dword def_irq_handler 
	jmp irqhandler

stub_irq15:
	push dword 0  
	push dword 15 
	push dword def_irq_handler 
	jmp irqhandler





Lsetup_idt:
    
	in al,0x70
	or al,0x80
	out 0x70,al

		mov eax,stub_int_div
	mov edi,idtstart+((0)*8)
	mov ebx,0x8e00 | (0 << 13)
	call setup_idtentry

		mov eax,stub_int_debug
	mov edi,idtstart+((1)*8)
	mov ebx,0x8e00 | (0 << 13)
	call setup_idtentry

		mov eax,stub_int_nmi
	mov edi,idtstart+((2)*8)
	mov ebx,0x8e00 | (0 << 13)
	call setup_idtentry

		mov eax,stub_int_bp
	mov edi,idtstart+((3)*8)
	mov ebx,0x8e00 | (3 << 13)
	call setup_idtentry

		mov eax,stub_int_of
	mov edi,idtstart+((4)*8)
	mov ebx,0x8e00 | (0 << 13)
	call setup_idtentry

		mov eax,stub_int_bc
	mov edi,idtstart+((5)*8)
	mov ebx,0x8e00 | (3 << 13)
	call setup_idtentry

		mov eax,stub_int_inv_oc
	mov edi,idtstart+((6)*8)
	mov ebx,0x8e00 | (0 << 13)
	call setup_idtentry

		mov eax,stub_int_dev_na
	mov edi,idtstart+((7)*8)
	mov ebx,0x8e00 | (0 << 13)
	call setup_idtentry

		mov eax,stub_int_df
	mov edi,idtstart+((8)*8)
	mov ebx,0x8e00 | (0 << 13)
	call setup_idtentry

		mov eax,stub_int_copro_or
	mov edi,idtstart+((9)*8)
	mov ebx,0x8e00 | (0 << 13)
	call setup_idtentry

		mov eax,stub_int_inv_tss
	mov edi,idtstart+((10)*8)
	mov ebx,0x8e00 | (0 << 13)
	call setup_idtentry

		mov eax,stub_int_snp
	mov edi,idtstart+((11)*8)
	mov ebx,0x8e00 | (0 << 13)
	call setup_idtentry

		mov eax,stub_int_sf
	mov edi,idtstart+((12)*8)
	mov ebx,0x8e00 | (0 << 13)
	call setup_idtentry

		mov eax,stub_int_gpf
	mov edi,idtstart+((13)*8)
	mov ebx,0x8e00 | (0 << 13)
	call setup_idtentry

		mov eax,stub_int_pf
	mov edi,idtstart+((14)*8)
	mov ebx,0x8e00 | (0 << 13)
	call setup_idtentry

		mov eax,stub_int_copro_err
	mov edi,idtstart+((16)*8)
	mov ebx,0x8e00 | (0 << 13)
	call setup_idtentry

		mov eax,stub_int_alignment
	mov edi,idtstart+((17)*8)
	mov ebx,0x8e00 | (0 << 13)
	call setup_idtentry

		mov eax,stub_int_mce
	mov edi,idtstart+((18)*8)
	mov ebx,0x8e00 | (0 << 13)
	call setup_idtentry

		mov eax,stub_int_xf
	mov edi,idtstart+((19)*8)
	mov ebx,0x8e00 | (0 << 13)
	call setup_idtentry


		mov eax,stub_irq0
	mov edi,idtstart+((0x20)*8)
	mov ebx,0x8e00 | (0 << 13)
	call setup_idtentry

		mov eax,stub_irq1
	mov edi,idtstart+((0x21)*8)
	mov ebx,0x8e00 | (0 << 13)
	call setup_idtentry

		mov eax,stub_irq2
	mov edi,idtstart+((0x22)*8)
	mov ebx,0x8e00 | (0 << 13)
	call setup_idtentry

		mov eax,stub_irq3
	mov edi,idtstart+((0x23)*8)
	mov ebx,0x8e00 | (0 << 13)
	call setup_idtentry

		mov eax,stub_irq4
	mov edi,idtstart+((0x24)*8)
	mov ebx,0x8e00 | (0 << 13)
	call setup_idtentry

		mov eax,stub_irq5
	mov edi,idtstart+((0x25)*8)
	mov ebx,0x8e00 | (0 << 13)
	call setup_idtentry

		mov eax,stub_irq6
	mov edi,idtstart+((0x26)*8)
	mov ebx,0x8e00 | (0 << 13)
	call setup_idtentry

		mov eax,stub_irq7
	mov edi,idtstart+((0x27)*8)
	mov ebx,0x8e00 | (0 << 13)
	call setup_idtentry

		mov eax,stub_irq8
	mov edi,idtstart+((0x28)*8)
	mov ebx,0x8e00 | (0 << 13)
	call setup_idtentry

		mov eax,stub_irq9
	mov edi,idtstart+((0x29)*8)
	mov ebx,0x8e00 | (0 << 13)
	call setup_idtentry

		mov eax,stub_irq10
	mov edi,idtstart+((0x2A)*8)
	mov ebx,0x8e00 | (0 << 13)
	call setup_idtentry

		mov eax,stub_irq11
	mov edi,idtstart+((0x2B)*8)
	mov ebx,0x8e00 | (0 << 13)
	call setup_idtentry

		mov eax,stub_irq12
	mov edi,idtstart+((0x2C)*8)
	mov ebx,0x8e00 | (0 << 13)
	call setup_idtentry

		mov eax,stub_irq13
	mov edi,idtstart+((0x2D)*8)
	mov ebx,0x8e00 | (0 << 13)
	call setup_idtentry

		mov eax,stub_irq14
	mov edi,idtstart+((0x2E)*8)
	mov ebx,0x8e00 | (0 << 13)
	call setup_idtentry

		mov eax,stub_irq15
	mov edi,idtstart+((0x2F)*8)
	mov ebx,0x8e00 | (0 << 13)
	call setup_idtentry

	
		mov eax,stub_yieldPointHandler
	mov edi,idtstart+((0x30)*8)
	mov ebx,0x8e00 | (3 << 13)
	call setup_idtentry

		mov eax,stub_int_stack_overflow
	mov edi,idtstart+((0x31)*8)
	mov ebx,0x8e00 | (3 << 13)
	call setup_idtentry

		mov eax,stub_syscallHandler
	mov edi,idtstart+((0x32)*8)
	mov ebx,0x8e00 | (3 << 13)
	call setup_idtentry

		mov eax,stub_timesliceHandler
	mov edi,idtstart+((0x33)*8)
	mov ebx,0x8e00 | (3 << 13)
	call setup_idtentry


	lidt [idt]









	mov	al,0x11		
	out	0x20,al		
	call	delay
	mov	al,0x20		
	out	0x21,al
	call	delay
	mov	al,0x04		
	out	0x21,al
	call	delay
	mov	al,0x01		
	out	0x21,al
	call	delay

	mov	al,0x11		
	out	0xA0,al		
	call	delay
	mov	al,0x28		
	out	0xA1,al
	call	delay
	mov	al,0x02		
	out	0xA1,al
	call	delay
	mov	al,0x01		
	out	0xA1,al
	call	delay



	mov eax,0x36	
	out 0x43,al
	call delay
	mov eax,1193
	out 0x40,al		
	call delay
	mov al,ah
	out 0x40,al		
	call delay


	in al,0x70
	and al,0x7F
	out 0x70,al


	xor eax,eax
	out 0x21,al
	out 0xA1,al
	
	ret




delay:
	jmp d1
d1:
	jmp d2
d2:
	ret




	



int_debug:
	jmp int_die
    mov ebp,eax
     
	
	
	
	
		
	push eax
	mov eax,dbg_msg1
	call sys_print_str32
	pop eax
	

	 
	
	
	
	
		
	push eax
	mov eax,dword [ebp+OLD_EIP]
	call sys_print_eax32
	pop eax


	 
	
	
	
	
		
	push eax
	mov eax,dword [ebp+OLD_EAX]
	call sys_print_eax32
	pop eax


	 
	
	
	
	
		
	push eax
	mov eax,dword [ebp+OLD_EBX]
	call sys_print_eax32
	pop eax


	 
	
	
	
	
		
	push eax
	mov eax,dword [ebp+OLD_ECX]
	call sys_print_eax32
	pop eax


	 
	
	
	
	
		
	push eax
	mov eax,dword [ebp+OLD_EDX]
	call sys_print_eax32
	pop eax


	 
	
	
	
	
		
	push eax
	mov eax,dword [ebp+OLD_ESP]
	call sys_print_eax32
	pop eax


     
	
	
	
	
		
	push eax
	mov eax,dbg_msg2
	call sys_print_str32
	pop eax
	

	ret

dbg_msg1: db 'debug eip,eabcdx,sp=',0
dbg_msg2: db 0xd,0xa,0




int_bp:
    mov ebp,eax
     
	
	
	
	
		
	push eax
	mov eax,bp_msg1
	call sys_print_str32
	pop eax
	

	 
	
	
	
	
		
	push eax
	mov eax,dword [ebp+OLD_EIP]
	call sys_print_eax32
	pop eax


	 
	
	
	
	
		
	push eax
	mov eax,dword [ebp+OLD_EAX]
	call sys_print_eax32
	pop eax


     
	
	
	
	
		
	push eax
	mov eax,bp_msg2
	call sys_print_str32
	pop eax
	

    call sys_print_intregs
	ret

bp_msg1: db 'breakpoint eip,eax=',0
bp_msg2: db 0xd,0xa,0




int_df:
	cli
	 
	
	
	
	
		
	push eax
	mov eax,int_df_msg
	call sys_print_str32
	pop eax
	

	hlt
	
int_df_msg: db 'Real panic: Double fault! Halting...',0




int_div:
	cmp dword [ebp+OLD_CS], USER_CS
	jne int_die
		mov eax,VmThread_EX_DIV0
	mov ebx,dword [ebp+OLD_EIP]
	call int_system_exception

	ret




int_bc:
	cmp dword [ebp+OLD_CS], USER_CS
	jne int_die
		mov eax,VmThread_EX_INDEXOUTOFBOUNDS
	mov ebx,dword [ebp+OLD_EIP]
	call int_system_exception

	ret




int_copro_or:
	cmp dword [ebp+OLD_CS], USER_CS
	jne int_die
		mov eax,VmThread_EX_COPRO_OR
	mov ebx,dword [ebp+OLD_EIP]
	call int_system_exception

	ret




int_copro_err:
	cmp dword [ebp+OLD_CS], USER_CS
	jne int_die
		mov eax,VmThread_EX_COPRO_ERR
	mov ebx,dword [ebp+OLD_EIP]
	call int_system_exception

	ret




int_nmi:

int_of:

int_inv_oc:

int_inv_tss:

int_snp:

int_sf:
	jmp int_die	




int_alignment:
	jmp int_die
	



int_mce:
	jmp int_die
	



int_xf:
	jmp int_die
	
	
 








global sys_version


sys_version: db 'Version ','0.2.9-dev',0xd,0xa,0

 
	 	
	 		
	 			
	 			
	 	
	 				
	  				
	 			
	 				
	 			
	 			
	 			
	 		
	 	
	 			

	 	
	 		
	 			
	 			
	 	
	 				
	  				
	 			
	 				
	 			
	 			
	 			
	 		
	 	
	 			



  
	



















  

  
	
	
	
	
	
	
	
	
	
	

	
	
	
	
	

	
	
	
	

	
	
	
	
	
	
	
	
	



 
	global Q43org5jnode2vm6Unsafe23pushInt2e28I29V
Q43org5jnode2vm6Unsafe23pushInt2e28I29V:

	global Q43org5jnode2vm6Unsafe23pushLong2e28J29V
Q43org5jnode2vm6Unsafe23pushLong2e28J29V:

	global Q43org5jnode2vm6Unsafe23pushObject2e28Ljava2flang2fObject3b29V
Q43org5jnode2vm6Unsafe23pushObject2e28Ljava2flang2fObject3b29V:

	ret

	global Q43org5jnode2vm6Unsafe23getSuperClasses2e28Ljava2flang2fObject3b295bLorg2fjnode2fvm2fclassmgr2fVmType3b
Q43org5jnode2vm6Unsafe23getSuperClasses2e28Ljava2flang2fObject3b295bLorg2fjnode2fvm2fclassmgr2fVmType3b:

	mov eax,[esp+4] 
	mov eax,[eax+ObjectLayout_TIB_SLOT*4] 
	mov eax,[eax+(TIBLayout_SUPERCLASSES_INDEX+VmArray_DATA_OFFSET)*4] 
	ret 4


	global Q43org5jnode2vm6Unsafe23clear2e28Lorg2fvmmagic2funboxed2fAddress3bLorg2fvmmagic2funboxed2fExtent3b29V
Q43org5jnode2vm6Unsafe23clear2e28Lorg2fvmmagic2funboxed2fAddress3bLorg2fvmmagic2funboxed2fExtent3b29V:

	mov edx,[esp+(2*4)]		
	mov eax,[esp+(1*4)]		
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
	ret 4*2

	global Q43org5jnode2vm6Unsafe23copy2e28Lorg2fvmmagic2funboxed2fAddress3bLorg2fvmmagic2funboxed2fAddress3bLorg2fvmmagic2funboxed2fExtent3b29V
Q43org5jnode2vm6Unsafe23copy2e28Lorg2fvmmagic2funboxed2fAddress3bLorg2fvmmagic2funboxed2fAddress3bLorg2fvmmagic2funboxed2fExtent3b29V:

	push ebx
	mov ebx,esp
	push edi
	push esi
	push ecx

	
		mov esi,[ebx+(4*4)]		
	mov edi,[ebx+(3*4)]		
	mov ecx,[ebx+(2*4)]		
	
	
	cmp esi,edi
	jl copy_reverse
	
	
	cld
copy_bytes:
	test ecx,3			
	jz copy_dwords
	dec ecx
	movsb				
	jmp copy_bytes	
copy_dwords:
	shr ecx,2
	rep movsd
	jmp copy_done
	
copy_reverse:
	
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
	ret 4*3
	
	global Q43org5jnode2vm6Unsafe23inPortByte2e28I29I
Q43org5jnode2vm6Unsafe23inPortByte2e28I29I:

	mov edx,[esp+4] 
	in al,dx
	movzx eax,al
	ret 4
	
	global Q43org5jnode2vm6Unsafe23inPortWord2e28I29I
Q43org5jnode2vm6Unsafe23inPortWord2e28I29I:

	mov edx,[esp+4] 
	in ax,dx
	movzx eax,ax
	ret 4
	
	global Q43org5jnode2vm6Unsafe23inPortDword2e28I29I
Q43org5jnode2vm6Unsafe23inPortDword2e28I29I:

	mov edx,[esp+4] 
	in eax,dx
	ret 4
	
	global Q43org5jnode2vm6Unsafe23outPortByte2e28II29V
Q43org5jnode2vm6Unsafe23outPortByte2e28II29V:

	mov eax,[esp+(1*4)] 
	mov edx,[esp+(2*4)] 
	out dx,al
	ret 4*2

	global Q43org5jnode2vm6Unsafe23outPortWord2e28II29V
Q43org5jnode2vm6Unsafe23outPortWord2e28II29V:

	mov eax,[esp+(1*4)] 
	mov edx,[esp+(2*4)] 
	out dx,ax
	ret 4*2
	
	global Q43org5jnode2vm6Unsafe23outPortDword2e28II29V
Q43org5jnode2vm6Unsafe23outPortDword2e28II29V:

	mov eax,[esp+(1*4)] 
	mov edx,[esp+(2*4)] 
	out dx,eax
	ret 4*2

	global Q43org5jnode2vm6Unsafe23idle2e2829V
Q43org5jnode2vm6Unsafe23idle2e2829V:

	sti
	hlt
	nop
	nop
	ret

	global Q43org5jnode2vm6Unsafe23die2e2829V
Q43org5jnode2vm6Unsafe23die2e2829V:

	cli
	hlt
	nop
	nop
	ret


	global Q43org5jnode2vm6Unsafe23invokeVoid2e28Lorg2fjnode2fvm2fclassmgr2fVmMethod3b29V
Q43org5jnode2vm6Unsafe23invokeVoid2e28Lorg2fjnode2fvm2fclassmgr2fVmMethod3b29V:

	global Q43org5jnode2vm6Unsafe23invokeInt2e28Lorg2fjnode2fvm2fclassmgr2fVmMethod3b29I
Q43org5jnode2vm6Unsafe23invokeInt2e28Lorg2fjnode2fvm2fclassmgr2fVmMethod3b29I:

	global Q43org5jnode2vm6Unsafe23invokeLong2e28Lorg2fjnode2fvm2fclassmgr2fVmMethod3b29J
Q43org5jnode2vm6Unsafe23invokeLong2e28Lorg2fjnode2fvm2fclassmgr2fVmMethod3b29J:

	global Q43org5jnode2vm6Unsafe23invokeObject2e28Lorg2fjnode2fvm2fclassmgr2fVmMethod3b29Ljava2flang2fObject3b
Q43org5jnode2vm6Unsafe23invokeObject2e28Lorg2fjnode2fvm2fclassmgr2fVmMethod3b29Ljava2flang2fObject3b:

	pop eax 			
	xchg eax,[esp+0]	
	jmp [eax+VmMethod_NATIVECODE_OFS]



	global Q43org5jnode2vm6Unsafe23initThread2e28Lorg2fjnode2fvm2fscheduler2fVmThread3bLjava2flang2fObject3bI29V
Q43org5jnode2vm6Unsafe23initThread2e28Lorg2fjnode2fvm2fscheduler2fVmThread3bLjava2flang2fObject3bI29V:

	push ebx
	mov ecx,[esp+(4*4)]		
	mov edx,[esp+(3*4)]		
	mov ebx,[esp+(2*4)]		

	
	mov [edx+0], edx	
	add dword [edx+0],((VmThread_STACK_OVERFLOW_LIMIT_SLOTS * 4)+8)
	mov [edx+4], edx	
	add [edx+4], ebx

	push ebp
	pushf	
	cli
	add ebx,edx			
	mov edx,esp 		
	mov esp,ebx
	xor ebx,ebx
	xor ebp,ebp			
	push ebp 			
	push ebp		    
	push ebp		   	
	push ebp		    
	mov ebp,esp
	push ebx			
	push ebx			
	push ecx			
	push ebx			
	
	mov eax,VmThread_runThread
	mov [ecx+VmX86Thread_EAX_OFS],eax 		
	mov [ecx+VmX86Thread_EBP_OFS],ebp
	mov eax,[eax+VmMethod_NATIVECODE_OFS]
	mov [ecx+VmX86Thread_EIP_OFS],eax
	mov [ecx+VmX86Thread_ESP_OFS], esp		
	pushf
	pop eax
	or eax,F_IF
	mov [ecx+VmX86Thread_EFLAGS_OFS],eax	
	
	mov esp,edx			
	popf				
	pop ebp
	pop ebx
	ret 4*3
	
initThread_msg1: db 'New esp=',0


	global Q43org5jnode2vm6Unsafe23getMaxAddress2e2829Lorg2fvmmagic2funboxed2fAddress3b
Q43org5jnode2vm6Unsafe23getMaxAddress2e2829Lorg2fvmmagic2funboxed2fAddress3b:

	mov eax,0xFFC00000	
	ret
	

	global Q43org5jnode2vm6Unsafe23getMinAddress2e2829Lorg2fvmmagic2funboxed2fAddress3b
Q43org5jnode2vm6Unsafe23getMinAddress2e2829Lorg2fvmmagic2funboxed2fAddress3b:

	mov eax,free_paddr
	ret
	

	global Q43org5jnode2vm6Unsafe23getMemoryStart2e2829Lorg2fvmmagic2funboxed2fAddress3b
Q43org5jnode2vm6Unsafe23getMemoryStart2e2829Lorg2fvmmagic2funboxed2fAddress3b:

	mov eax,[free_mem_start]
	
	ret
	

	global Q43org5jnode2vm6Unsafe23getMemoryEnd2e2829Lorg2fvmmagic2funboxed2fAddress3b
Q43org5jnode2vm6Unsafe23getMemoryEnd2e2829Lorg2fvmmagic2funboxed2fAddress3b:

	mov eax,[mem_size]
	ret
	

	global Q43org5jnode2vm6Unsafe23getKernelStart2e2829Lorg2fvmmagic2funboxed2fAddress3b
Q43org5jnode2vm6Unsafe23getKernelStart2e2829Lorg2fvmmagic2funboxed2fAddress3b:

	mov eax,kernel_begin
	ret
	

	global Q43org5jnode2vm6Unsafe23getKernelEnd2e2829Lorg2fvmmagic2funboxed2fAddress3b
Q43org5jnode2vm6Unsafe23getKernelEnd2e2829Lorg2fvmmagic2funboxed2fAddress3b:

	mov eax,vm_start
	ret
	

	global Q43org5jnode2vm6Unsafe23getInitJarStart2e2829Lorg2fvmmagic2funboxed2fAddress3b
Q43org5jnode2vm6Unsafe23getInitJarStart2e2829Lorg2fvmmagic2funboxed2fAddress3b:

	mov eax,[initJar_start]
	ret
	

	global Q43org5jnode2vm6Unsafe23getInitJarEnd2e2829Lorg2fvmmagic2funboxed2fAddress3b
Q43org5jnode2vm6Unsafe23getInitJarEnd2e2829Lorg2fvmmagic2funboxed2fAddress3b:

	mov eax,[initJar_end]
	ret
	

	global Q43org5jnode2vm6Unsafe23getBootHeapStart2e2829Lorg2fvmmagic2funboxed2fAddress3b
Q43org5jnode2vm6Unsafe23getBootHeapStart2e2829Lorg2fvmmagic2funboxed2fAddress3b:

	mov eax,bootHeapStart
	ret
	

	global Q43org5jnode2vm6Unsafe23getBootHeapEnd2e2829Lorg2fvmmagic2funboxed2fAddress3b
Q43org5jnode2vm6Unsafe23getBootHeapEnd2e2829Lorg2fvmmagic2funboxed2fAddress3b:

	mov eax,bootHeapEnd
	ret
	




	global Q43org5jnode2vm6Unsafe23getCmdLine2e285bB29I
Q43org5jnode2vm6Unsafe23getCmdLine2e285bB29I:

	mov eax,[esp+4]		
	test eax,eax				
	jz after_copyCmdLine
	push ecx
	push esi
	push edi
	mov esi,multiboot_cmdline
	lea edi,[eax+(VmArray_DATA_OFFSET*4)]
	mov ecx,[eax+(VmArray_LENGTH_OFFSET*4)]
	cld
	rep	movsb
	pop edi
	pop esi
	pop ecx
after_copyCmdLine:
	mov eax,MBI_CMDLINE_MAX
	ret 4
	

	global Q43org5jnode2vm6Unsafe23yieldPoint2e2829V
Q43org5jnode2vm6Unsafe23yieldPoint2e2829V:

	
	cmp dword[fs:VmProcessor_THREADSWITCHINDICATOR_OFS],VmProcessor_TSI_SWITCH_REQUESTED
	jne noYieldPoint
	int 0x30
noYieldPoint:
	ret	


	global Q43org5jnode2vm6Unsafe23getJumpTable02e2829Lorg2fvmmagic2funboxed2fAddress3b
Q43org5jnode2vm6Unsafe23getJumpTable02e2829Lorg2fvmmagic2funboxed2fAddress3b:

	mov eax,vm_jumpTable
	ret
	

	global Q43org5jnode2vm6Unsafe23debug2e28Ljava2flang2fString3b29V
Q43org5jnode2vm6Unsafe23debug2e28Ljava2flang2fString3b29V:

	mov eax,[esp+4]
	call vm_print_string
	ret 4
	

	global Q43org5jnode2vm6Unsafe23debug2e28C29V
Q43org5jnode2vm6Unsafe23debug2e28C29V:

	mov eax,[esp+4]
	call sys_print_char32

	
	
	ret 4


	global Q43org5jnode2vm6Unsafe23debug2e28I29V
Q43org5jnode2vm6Unsafe23debug2e28I29V:

	mov eax,[esp+4]
	call sys_print_eax32

	
	
	ret 4


	global Q43org5jnode2vm6Unsafe23debug2e28J29V
Q43org5jnode2vm6Unsafe23debug2e28J29V:

	mov eax,[esp+8]		
	call sys_print_eax32
	mov eax,[esp+4]		
	call sys_print_eax32

	
	
	
	ret 4*2
		




	global Q43org5jnode2vm6Unsafe23debug2e28Lorg2fvmmagic2funboxed2fAddress3b29V
Q43org5jnode2vm6Unsafe23debug2e28Lorg2fvmmagic2funboxed2fAddress3b29V:

	global Q43org5jnode2vm6Unsafe23debug2e28Lorg2fvmmagic2funboxed2fWord3b29V
Q43org5jnode2vm6Unsafe23debug2e28Lorg2fvmmagic2funboxed2fWord3b29V:

	global Q43org5jnode2vm6Unsafe23debug2e28Lorg2fvmmagic2funboxed2fExtent3b29V
Q43org5jnode2vm6Unsafe23debug2e28Lorg2fvmmagic2funboxed2fExtent3b29V:

	global Q43org5jnode2vm6Unsafe23debug2e28Lorg2fvmmagic2funboxed2fOffset3b29V
Q43org5jnode2vm6Unsafe23debug2e28Lorg2fvmmagic2funboxed2fOffset3b29V:

	mov eax,[esp+4]
	call sys_print_eax32

	
	
	ret 4
		


	global Q43org5jnode2vm6Unsafe23setKdbEnabled2e28Z29Z
Q43org5jnode2vm6Unsafe23setKdbEnabled2e28Z29Z:

	mov eax,[esp+4]
	push edx
	mov edx,eax
	mov eax,[kdb_enabled]
	mov [kdb_enabled],edx
	pop edx
	ret 4
	

	global Q43org5jnode2vm6Unsafe23isKdbEnabled2e2829Z
Q43org5jnode2vm6Unsafe23isKdbEnabled2e2829Z:

	mov eax,[kdb_enabled]
	ret 
	

	global Q43org5jnode2vm6Unsafe23readKdbInput2e2829I
Q43org5jnode2vm6Unsafe23readKdbInput2e2829I:

	jmp kdb_recv_char


	global Q43org5jnode2vm6Unsafe23getCpuCycles2e2829J
Q43org5jnode2vm6Unsafe23getCpuCycles2e2829J:

	rdtsc			
	ret




	global Q43org5jnode2vm6Unsafe23callVbeFunction2e28Lorg2fvmmagic2funboxed2fAddress3bILorg2fvmmagic2funboxed2fAddress3b29I
Q43org5jnode2vm6Unsafe23callVbeFunction2e28Lorg2fvmmagic2funboxed2fAddress3bILorg2fvmmagic2funboxed2fAddress3b29I:

	push edi
	mov eax,[esp+(3*4)] 
	mov edi,[esp+(2*4)] 
	
	
	mov ebx, edi
	shr ebx, 16
	mov es, ebx


	
	
	and edi, 0x0000FFFF
	
	call dword far [esp+(4*4)] 
	pop edi
	ret 4*3

 










  
    
	
	
	
	
	
	 	    			 	

      
    
	

	
	



  
	
	
	
	
	

	  
	
	

	



  

	
	
	
	
	
	

	  				
	
	

	
	
	







	global Q43org5jnode2vm6Unsafe23andByte2e28Lorg2fvmmagic2funboxed2fAddress3bBI29V
Q43org5jnode2vm6Unsafe23andByte2e28Lorg2fvmmagic2funboxed2fAddress3bBI29V:

		mov eax,[esp+(3*4)]		
	mov edx,[esp+(2*4)] 	
	mov ecx,[esp+(1*4)]		
	test ecx,ecx
	jz __jnasm_macro_local_label_32						
__jnasm_macro_local_label_31:
	and byte [eax],dl
	add eax,1
	loop __jnasm_macro_local_label_31
__jnasm_macro_local_label_32:
	ret 4*3




	global Q43org5jnode2vm6Unsafe23andShort2e28Lorg2fvmmagic2funboxed2fAddress3bSI29V
Q43org5jnode2vm6Unsafe23andShort2e28Lorg2fvmmagic2funboxed2fAddress3bSI29V:

	global Q43org5jnode2vm6Unsafe23andChar2e28Lorg2fvmmagic2funboxed2fAddress3bCI29V
Q43org5jnode2vm6Unsafe23andChar2e28Lorg2fvmmagic2funboxed2fAddress3bCI29V:

		mov eax,[esp+(3*4)]		
	mov edx,[esp+(2*4)] 	
	mov ecx,[esp+(1*4)]		
	test ecx,ecx
	jz __jnasm_macro_local_label_34						
__jnasm_macro_local_label_33:
	and word [eax],dx
	add eax,2
	loop __jnasm_macro_local_label_33
__jnasm_macro_local_label_34:
	ret 4*3



	global Q43org5jnode2vm6Unsafe23andInt242e28Lorg2fvmmagic2funboxed2fAddress3bII29V
Q43org5jnode2vm6Unsafe23andInt242e28Lorg2fvmmagic2funboxed2fAddress3bII29V:

	    push edi
	mov edi,[esp+(4*4)]		
	mov eax,[esp+(3*4)] 	
	mov ecx,[esp+(2*4)]		
	test ecx,ecx
	jz __jnasm_macro_local_label_36						
	or eax,0xFF000000	    			 	
__jnasm_macro_local_label_35:
    and dword [edi],eax
    lea edi,[edi+3]
	loop __jnasm_macro_local_label_35
__jnasm_macro_local_label_36:
	pop edi
	ret 4*3



	global Q43org5jnode2vm6Unsafe23andInt2e28Lorg2fvmmagic2funboxed2fAddress3bII29V
Q43org5jnode2vm6Unsafe23andInt2e28Lorg2fvmmagic2funboxed2fAddress3bII29V:

		mov eax,[esp+(3*4)]		
	mov edx,[esp+(2*4)] 	
	mov ecx,[esp+(1*4)]		
	test ecx,ecx
	jz __jnasm_macro_local_label_38						
__jnasm_macro_local_label_37:
	and dword [eax],edx
	add eax,4
	loop __jnasm_macro_local_label_37
__jnasm_macro_local_label_38:
	ret 4*3



	global Q43org5jnode2vm6Unsafe23andLong2e28Lorg2fvmmagic2funboxed2fAddress3bJI29V
Q43org5jnode2vm6Unsafe23andLong2e28Lorg2fvmmagic2funboxed2fAddress3bJI29V:

		push edi
	mov edi,[esp+20]	
	mov edx,[esp+16] 	
	mov eax,[esp+12] 	
	mov ecx,[esp+8]		
	jecxz __jnasm_macro_local_label_40			
__jnasm_macro_local_label_39:
	and dword [edi],eax	
	and dword [edi+4],edx
	add edi,8
	loop __jnasm_macro_local_label_39
__jnasm_macro_local_label_40:
	pop edi
	ret 16

	
	
	
	
	
	

	  				
	
	

	
	
	

	





	global Q43org5jnode2vm6Unsafe23orByte2e28Lorg2fvmmagic2funboxed2fAddress3bBI29V
Q43org5jnode2vm6Unsafe23orByte2e28Lorg2fvmmagic2funboxed2fAddress3bBI29V:

		mov eax,[esp+(3*4)]		
	mov edx,[esp+(2*4)] 	
	mov ecx,[esp+(1*4)]		
	test ecx,ecx
	jz __jnasm_macro_local_label_42						
__jnasm_macro_local_label_41:
	or byte [eax],dl
	add eax,1
	loop __jnasm_macro_local_label_41
__jnasm_macro_local_label_42:
	ret 4*3




	global Q43org5jnode2vm6Unsafe23orShort2e28Lorg2fvmmagic2funboxed2fAddress3bSI29V
Q43org5jnode2vm6Unsafe23orShort2e28Lorg2fvmmagic2funboxed2fAddress3bSI29V:

	global Q43org5jnode2vm6Unsafe23orChar2e28Lorg2fvmmagic2funboxed2fAddress3bCI29V
Q43org5jnode2vm6Unsafe23orChar2e28Lorg2fvmmagic2funboxed2fAddress3bCI29V:

		mov eax,[esp+(3*4)]		
	mov edx,[esp+(2*4)] 	
	mov ecx,[esp+(1*4)]		
	test ecx,ecx
	jz __jnasm_macro_local_label_44						
__jnasm_macro_local_label_43:
	or word [eax],dx
	add eax,2
	loop __jnasm_macro_local_label_43
__jnasm_macro_local_label_44:
	ret 4*3



	global Q43org5jnode2vm6Unsafe23orInt242e28Lorg2fvmmagic2funboxed2fAddress3bII29V
Q43org5jnode2vm6Unsafe23orInt242e28Lorg2fvmmagic2funboxed2fAddress3bII29V:

	    push edi
	mov edi,[esp+(4*4)]		
	mov eax,[esp+(3*4)] 	
	mov ecx,[esp+(2*4)]		
	test ecx,ecx
	jz __jnasm_macro_local_label_46						
	and eax,0x00FFFFFF	    			 	
__jnasm_macro_local_label_45:
    or dword [edi],eax
    lea edi,[edi+3]
	loop __jnasm_macro_local_label_45
__jnasm_macro_local_label_46:
	pop edi
	ret 4*3



	global Q43org5jnode2vm6Unsafe23orInt2e28Lorg2fvmmagic2funboxed2fAddress3bII29V
Q43org5jnode2vm6Unsafe23orInt2e28Lorg2fvmmagic2funboxed2fAddress3bII29V:

		mov eax,[esp+(3*4)]		
	mov edx,[esp+(2*4)] 	
	mov ecx,[esp+(1*4)]		
	test ecx,ecx
	jz __jnasm_macro_local_label_48						
__jnasm_macro_local_label_47:
	or dword [eax],edx
	add eax,4
	loop __jnasm_macro_local_label_47
__jnasm_macro_local_label_48:
	ret 4*3



	global Q43org5jnode2vm6Unsafe23orLong2e28Lorg2fvmmagic2funboxed2fAddress3bJI29V
Q43org5jnode2vm6Unsafe23orLong2e28Lorg2fvmmagic2funboxed2fAddress3bJI29V:

		push edi
	mov edi,[esp+20]	
	mov edx,[esp+16] 	
	mov eax,[esp+12] 	
	mov ecx,[esp+8]		
	jecxz __jnasm_macro_local_label_50			
__jnasm_macro_local_label_49:
	or dword [edi],eax	
	or dword [edi+4],edx
	add edi,8
	loop __jnasm_macro_local_label_49
__jnasm_macro_local_label_50:
	pop edi
	ret 16

	
	
	
	
	
	

	  				
	
	

	
	
	

	





	global Q43org5jnode2vm6Unsafe23xorByte2e28Lorg2fvmmagic2funboxed2fAddress3bBI29V
Q43org5jnode2vm6Unsafe23xorByte2e28Lorg2fvmmagic2funboxed2fAddress3bBI29V:

		mov eax,[esp+(3*4)]		
	mov edx,[esp+(2*4)] 	
	mov ecx,[esp+(1*4)]		
	test ecx,ecx
	jz __jnasm_macro_local_label_52						
__jnasm_macro_local_label_51:
	xor byte [eax],dl
	add eax,1
	loop __jnasm_macro_local_label_51
__jnasm_macro_local_label_52:
	ret 4*3




	global Q43org5jnode2vm6Unsafe23xorShort2e28Lorg2fvmmagic2funboxed2fAddress3bSI29V
Q43org5jnode2vm6Unsafe23xorShort2e28Lorg2fvmmagic2funboxed2fAddress3bSI29V:

	global Q43org5jnode2vm6Unsafe23xorChar2e28Lorg2fvmmagic2funboxed2fAddress3bCI29V
Q43org5jnode2vm6Unsafe23xorChar2e28Lorg2fvmmagic2funboxed2fAddress3bCI29V:

		mov eax,[esp+(3*4)]		
	mov edx,[esp+(2*4)] 	
	mov ecx,[esp+(1*4)]		
	test ecx,ecx
	jz __jnasm_macro_local_label_54						
__jnasm_macro_local_label_53:
	xor word [eax],dx
	add eax,2
	loop __jnasm_macro_local_label_53
__jnasm_macro_local_label_54:
	ret 4*3



	global Q43org5jnode2vm6Unsafe23xorInt242e28Lorg2fvmmagic2funboxed2fAddress3bII29V
Q43org5jnode2vm6Unsafe23xorInt242e28Lorg2fvmmagic2funboxed2fAddress3bII29V:

	    push edi
	mov edi,[esp+(4*4)]		
	mov eax,[esp+(3*4)] 	
	mov ecx,[esp+(2*4)]		
	test ecx,ecx
	jz __jnasm_macro_local_label_56						
	and eax,0x00FFFFFF	    			 	
__jnasm_macro_local_label_55:
    xor dword [edi],eax
    lea edi,[edi+3]
	loop __jnasm_macro_local_label_55
__jnasm_macro_local_label_56:
	pop edi
	ret 4*3



	global Q43org5jnode2vm6Unsafe23xorInt2e28Lorg2fvmmagic2funboxed2fAddress3bII29V
Q43org5jnode2vm6Unsafe23xorInt2e28Lorg2fvmmagic2funboxed2fAddress3bII29V:

		mov eax,[esp+(3*4)]		
	mov edx,[esp+(2*4)] 	
	mov ecx,[esp+(1*4)]		
	test ecx,ecx
	jz __jnasm_macro_local_label_58						
__jnasm_macro_local_label_57:
	xor dword [eax],edx
	add eax,4
	loop __jnasm_macro_local_label_57
__jnasm_macro_local_label_58:
	ret 4*3



	global Q43org5jnode2vm6Unsafe23xorLong2e28Lorg2fvmmagic2funboxed2fAddress3bJI29V
Q43org5jnode2vm6Unsafe23xorLong2e28Lorg2fvmmagic2funboxed2fAddress3bJI29V:

		push edi
	mov edi,[esp+20]	
	mov edx,[esp+16] 	
	mov eax,[esp+12] 	
	mov ecx,[esp+8]		
	jecxz __jnasm_macro_local_label_60			
__jnasm_macro_local_label_59:
	xor dword [edi],eax	
	xor dword [edi+4],edx
	add edi,8
	loop __jnasm_macro_local_label_59
__jnasm_macro_local_label_60:
	pop edi
	ret 16

	
	
	
	
	
	

	  				
	
	

	
	
	

	
	
 










	global Q43org5jnode2vm6Unsafe23setBytes2e28Lorg2fvmmagic2funboxed2fAddress3bBI29V
Q43org5jnode2vm6Unsafe23setBytes2e28Lorg2fvmmagic2funboxed2fAddress3bBI29V:

	push edi
	mov edi,[esp+(4*4)]		
	mov eax,[esp+(3*4)] 	
	mov ecx,[esp+(2*4)]		
	rep stosb
	pop edi
	ret 4*3



	global Q43org5jnode2vm6Unsafe23setShorts2e28Lorg2fvmmagic2funboxed2fAddress3bSI29V
Q43org5jnode2vm6Unsafe23setShorts2e28Lorg2fvmmagic2funboxed2fAddress3bSI29V:

	global Q43org5jnode2vm6Unsafe23setChars2e28Lorg2fvmmagic2funboxed2fAddress3bCI29V
Q43org5jnode2vm6Unsafe23setChars2e28Lorg2fvmmagic2funboxed2fAddress3bCI29V:

	push edi
	mov edi,[esp+(4*4)]		
	mov eax,[esp+(3*4)] 	
	mov ecx,[esp+(2*4)]		
	rep stosw
	pop edi
	ret 4*3



	global Q43org5jnode2vm6Unsafe23setInts2e28Lorg2fvmmagic2funboxed2fAddress3bII29V
Q43org5jnode2vm6Unsafe23setInts2e28Lorg2fvmmagic2funboxed2fAddress3bII29V:

	global Q43org5jnode2vm6Unsafe23setFloats2e28Lorg2fvmmagic2funboxed2fAddress3bFI29V
Q43org5jnode2vm6Unsafe23setFloats2e28Lorg2fvmmagic2funboxed2fAddress3bFI29V:

	push edi
	mov edi,[esp+(4*4)]		
	mov eax,[esp+(3*4)] 	
	mov ecx,[esp+(2*4)]		
	rep stosd
	pop edi
	ret 4*3


	global Q43org5jnode2vm6Unsafe23setObjects2e28Lorg2fvmmagic2funboxed2fAddress3bLjava2flang2fObject3bI29V
Q43org5jnode2vm6Unsafe23setObjects2e28Lorg2fvmmagic2funboxed2fAddress3bLjava2flang2fObject3bI29V:

	push edi
	mov edi,[esp+(4*4)]		
	mov eax,[esp+(3*4)]	 	
	mov ecx,[esp+(2*4)]		
	rep stosd

	
	
	pop edi
	ret 4*3


	global Q43org5jnode2vm6Unsafe23setInts242e28Lorg2fvmmagic2funboxed2fAddress3bII29V
Q43org5jnode2vm6Unsafe23setInts242e28Lorg2fvmmagic2funboxed2fAddress3bII29V:

	push edi
	mov edi,[esp+(4*4)]		
	mov eax,[esp+(3*4)] 	
	mov ecx,[esp+(2*4)]		
	and eax,0xFFFFFF				
set24_loop:
	mov edx,[edi]
	and edx,0xFF000000
	or edx,eax
	mov [edi],edx
	lea edi,[edi+3]
	loop set24_loop
	pop edi
	ret 4*3



	global Q43org5jnode2vm6Unsafe23setLongs2e28Lorg2fvmmagic2funboxed2fAddress3bJI29V
Q43org5jnode2vm6Unsafe23setLongs2e28Lorg2fvmmagic2funboxed2fAddress3bJI29V:

	global Q43org5jnode2vm6Unsafe23setDoubles2e28Lorg2fvmmagic2funboxed2fAddress3bDI29V
Q43org5jnode2vm6Unsafe23setDoubles2e28Lorg2fvmmagic2funboxed2fAddress3bDI29V:

	push edi
	mov edi,[esp+20]	
	mov edx,[esp+16] 	
	mov eax,[esp+12] 	
	mov ecx,[esp+8]		
	test ecx,0xFFFFFFFF	
	jz set64_end
set64_loop:
	stosd				
	xchg eax,edx		
	stosd				
	xchg eax,edx		
	loop set64_loop
set64_end:	
	pop edi
	ret 16

	
	
	
	
	
	
	
	
		align 4096

	
 















	global Q53org5jnode2vm3x869UnsafeX8623getCPUID2e28Lorg2fvmmagic2funboxed2fWord3b5bI29I
Q53org5jnode2vm3x869UnsafeX8623getCPUID2e28Lorg2fvmmagic2funboxed2fWord3b5bI29I:

	push edi
	mov edi,[esp+(2*4)]		
	mov eax,[esp+(3*4)]		
	push ebx
	push ecx
	push edx
	
	test edi,edi		
	je cpuid_invalid_arg
	mov ebx,4 			
	cmp ebx,[edi+VmArray_LENGTH_OFFSET*4]
	ja cpuid_invalid_arg
	
	lea edi,[edi+VmArray_DATA_OFFSET*4]		
	
	cpuid
	mov [edi+0],eax		
	mov [edi+4],ebx		
	mov [edi+8],ecx		
	mov [edi+12],edx	
	
	mov eax,1			
	jmp cpuid_ret
	
cpuid_invalid_arg:	
	xor eax,eax 		
	
cpuid_ret:
	pop edx
	pop ecx
	pop ebx
	pop edi
	ret 4

	
 









	global Q53org5jnode2vm3x869UnsafeX8623getGDT2e285bI29I
Q53org5jnode2vm3x869UnsafeX8623getGDT2e285bI29I:

	mov eax,[esp+4]		
	test eax,eax
	jz getGDT_ret

	push esi
	push edi
	push ecx
	cld
	
	mov ecx,[eax+VmArray_LENGTH_OFFSET*4]
	lea edi,[eax+VmArray_DATA_OFFSET*4]
	mov esi,gdtstart
	rep movsd
	
	pop ecx
	pop edi
	pop esi
	
getGDT_ret:
	
	mov eax,gdtend-gdtstart
	shr eax,2
	ret 4


	global Q53org5jnode2vm3x869UnsafeX8623getTSS2e285bI29I
Q53org5jnode2vm3x869UnsafeX8623getTSS2e285bI29I:

	mov eax,[esp+4]		
	test eax,eax		
	jz getTSS_ret

	push esi	
	push edi
	push ecx
	cld
	
	mov ecx,[eax+VmArray_LENGTH_OFFSET*4]
	lea edi,[eax+VmArray_DATA_OFFSET*4]
	mov esi,tss
	rep movsd
	
	pop ecx
	pop edi
	pop esi
	
getTSS_ret:
	
	mov eax,tss_e-tss
	shr eax,2
	ret 4
	

	global Q53org5jnode2vm3x869UnsafeX8623getAPBootCodeSize2e2829I
Q53org5jnode2vm3x869UnsafeX8623getAPBootCodeSize2e2829I:

	mov eax,ap_boot_end-ap_boot
	ret
	

	global Q53org5jnode2vm3x869UnsafeX8623getCR32e2829Lorg2fvmmagic2funboxed2fAddress3b
Q53org5jnode2vm3x869UnsafeX8623getCR32e2829Lorg2fvmmagic2funboxed2fAddress3b:

	mov eax,pd_paddr

	

	ret


	global Q53org5jnode2vm3x869UnsafeX8623getMultibootMMap2e2829Lorg2fvmmagic2funboxed2fAddress3b
Q53org5jnode2vm3x869UnsafeX8623getMultibootMMap2e2829Lorg2fvmmagic2funboxed2fAddress3b:

	mov eax,multiboot_mmap+4
	ret


	global Q53org5jnode2vm3x869UnsafeX8623getMultibootMMapLength2e2829I
Q53org5jnode2vm3x869UnsafeX8623getMultibootMMapLength2e2829I:

	mov eax,[multiboot_mmap]
	ret


	global Q53org5jnode2vm3x869UnsafeX8623getVbeControlInfos2e2829Lorg2fvmmagic2funboxed2fAddress3b
Q53org5jnode2vm3x869UnsafeX8623getVbeControlInfos2e2829Lorg2fvmmagic2funboxed2fAddress3b:

	mov eax,vbe_control_info
	ret


	global Q53org5jnode2vm3x869UnsafeX8623getVbeModeInfos2e2829Lorg2fvmmagic2funboxed2fAddress3b
Q53org5jnode2vm3x869UnsafeX8623getVbeModeInfos2e2829Lorg2fvmmagic2funboxed2fAddress3b:

	mov eax,vbe_mode_info
	ret


	global Q53org5jnode2vm3x869UnsafeX8623setupBootCode2e28Lorg2fvmmagic2funboxed2fAddress3b5bI5bI29V
Q53org5jnode2vm3x869UnsafeX8623setupBootCode2e28Lorg2fvmmagic2funboxed2fAddress3b5bI5bI29V:

	push ebx
	
	mov eax,[esp+(4*4)]		
	mov edx,[esp+(3*4)]		
	mov ebx,[esp+(2*4)]		

	push esi
	push edi
	push ecx
	cld

	
	mov ecx,ap_boot_end-ap_boot		
	mov edi,eax						
	mov esi,ap_boot					
	rep movsb						
	
	
	lea edi,[eax+(ap_boot16_jmp-ap_boot)+2]	
	lea ecx,[eax+(ap_boot32-ap_boot)]
	mov [edi],ecx
	
	
	lea edi,[eax+(ap_gdt_ptr-ap_boot)]
	lea ecx,[edx+VmArray_DATA_OFFSET*4]
	mov [edi],ecx

	
	lea edi,[eax+(ap_boot32_lgdt-ap_boot)+3] 
	lea ecx,[eax+(ap_gdtbase-ap_boot)]
	mov [edi],ecx

	
	lea edi,[eax+(ap_boot32_ltss-ap_boot)+1]	
	lea ecx,[ebx+VmArray_DATA_OFFSET*4]
	mov [edi],ecx
	
	
		push eax
	mov eax,SC_DISABLE_PAGING
	int SYSCALL_INT
	pop eax

	mov ecx,eax					
	and ecx,0xf
	mov word [0x467],cx			
	mov ecx,eax					
	shr ecx,4
	mov word [0x469],cx			
		push eax
	mov eax,SC_ENABLE_PAGING
	int SYSCALL_INT
	pop eax

	
		push eax
	mov eax,0x0F
	out 0x70,al
	mov eax,0x0A
	out 0x71,al
	pop eax


	pop ecx
	pop edi
	pop esi
	pop ebx

	ret 4*3


	global Q53org5jnode2vm3x869UnsafeX8623syncMSRs2e2829V
Q53org5jnode2vm3x869UnsafeX8623syncMSRs2e2829V:

		push eax
	mov eax,SC_SYNC_MSRS
	int SYSCALL_INT
	pop eax

	ret


	global Q53org5jnode2vm3x869UnsafeX8623saveMSRs2e2829V
Q53org5jnode2vm3x869UnsafeX8623saveMSRs2e2829V:

		push eax
	mov eax,SC_SAVE_MSRS
	int SYSCALL_INT
	pop eax

	ret


	global Q53org5jnode2vm3x869UnsafeX8623restoreMSRs2e2829V
Q53org5jnode2vm3x869UnsafeX8623restoreMSRs2e2829V:

		push eax
	mov eax,SC_RESTORE_MSRS
	int SYSCALL_INT
	pop eax

	ret






	global Q53org5jnode2vm3x869UnsafeX8623readMSR2e28Lorg2fvmmagic2funboxed2fWord3b29J
Q53org5jnode2vm3x869UnsafeX8623readMSR2e28Lorg2fvmmagic2funboxed2fWord3b29J:

	push ecx
	mov ecx,[esp+(2*4)] 	
	rdmsr
	pop ecx
	ret
		





	global Q53org5jnode2vm3x869UnsafeX8623writeMSR2e28Lorg2fvmmagic2funboxed2fWord3bJ29V
Q53org5jnode2vm3x869UnsafeX8623writeMSR2e28Lorg2fvmmagic2funboxed2fWord3bJ29V:

	push ecx
	mov ecx,[esp+(2*4)+8]	
	mov edx,[esp+(2*4)+4] 	
	mov eax,[esp+(2*4)+0] 	
	push ebx
	mov ebx,eax
		push eax
	mov eax,SC_WRITE_MSR
	int SYSCALL_INT
	pop eax

	pop ebx
	pop ecx
	ret
		
 








V256_T8:	dd 0x01000100
			dd 0x01000100


	global Q53org5jnode2vm3x869UnsafeX8623setARGB32bppMMX2e28Lorg2fvmmagic2funboxed2fAddress3bLorg2fvmmagic2funboxed2fAddress3bI29V
Q53org5jnode2vm3x869UnsafeX8623setARGB32bppMMX2e28Lorg2fvmmagic2funboxed2fAddress3bLorg2fvmmagic2funboxed2fAddress3bI29V:

	push ecx

	
	mov eax,[esp+(4*4)]		
	mov edx,[esp+(3*4)]		
	mov ecx,[esp+(2*4)]		
	pxor mm1,mm1					
	movq mm5,qword [V256_T8]		
	
setARGB32bppMMX_Loop:	
	movd mm2,dword [eax]			
	movd mm3,dword [edx]			
	
	punpcklbw mm2,mm1		 		
	punpcklbw mm3,mm1				
	
	pshufw mm4,mm2,0xFF				
	movq mm6,mm3					

	pcmpgtw mm6,mm2					
	movq mm7,mm5					
	
	pand mm7,mm6					
	paddw mm2,mm7					
	
	psubw mm2,mm3					
	pmullw mm2,mm4					
	
	psrlw mm2,8						
	paddw mm2,mm3					
	
	pand mm4,mm6					

	psubw mm2,mm4					
	packuswb mm2,mm2				
	
	movd dword [edx],mm2
	lea edx,[edx+4]
	lea eax,[eax+4]
	dec ecx
	jnz setARGB32bppMMX_Loop
	
	
	emms

	pop ecx
	ret 4*3


 
















	global vm_athrow
vm_athrow:

	 
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		

		
		
	
	
	global vm_athrow_notrace
vm_athrow_notrace:

	 
		
		
	
		
	
	test ebp,ebp
	jz vm_athrow_unhandled	
	
vm_athrow_notrace_pop_eip:
	
	
	pop edx				
	
	
	
	
	
	
	lea edx,[edx-1]

	push eax			
	push edx			
		
	
	push eax			
	push ebp			
	push edx			
	mov eax, vm_findThrowableHandler
		call [eax+VmMethod_NATIVECODE_OFS]

	
	mov ebx,eax
	
	pop edx				
	pop eax				
		
vm_athrow_deliver_compiled:
	test ebx,ebx
	jz vm_athrow_notrace_pop_eip	
	
	jmp ebx
	
vm_athrow_unhandled:
	cli
	 
	
	
	
	
		
	push eax
	mov eax,vm_athrow_msg4
	call sys_print_str32
	pop eax
	

	mov ebx,eax
	mov eax,[ebx+ObjectLayout_TIB_SLOT*4]
	mov eax,[eax+VmArray_DATA_OFFSET*4]
	mov eax,[eax+VmType_NAME_OFS]
	call vm_print_string
	mov eax,[ebx+Throwable_DETAILMESSAGE_OFS] 
	call vm_print_string
	cli
	hlt
	ret
	



vm_print_string:
	push eax
	test eax,eax
	jz vm_print_string_null
	mov eax,[eax] 
	call vm_print_chararray
	jmp vm_print_string_ret
	
vm_print_string_null:
	 
	
	
	
	
		
	push eax
	mov eax,vm_print_chararray_msg1
	call sys_print_str32
	pop eax
	

vm_print_string_ret:
	pop eax
	ret




vm_print_chararray:
		push eax
__jnasm_macro_local_label_61:	
	mov eax,1
	lock xchg eax,dword [console_lock]
	test eax,eax
	jnz __jnasm_macro_local_label_61
	pop eax

    push eax
    push ecx
    push esi
    
	test eax,eax
	je vm_print_chararray_null
	mov ecx,[eax+VmArray_LENGTH_OFFSET*4]
	lea esi,[eax+VmArray_DATA_OFFSET*4]
	cld
	
vm_print_chararray_loop:
	test ecx,ecx
	jz vm_print_chararray_ret
	lodsw
	push esi
	call sys_do_print_char32

	
	
	pop esi
	dec ecx
	jmp vm_print_chararray_loop	
	
vm_print_chararray_null:
	 
	
	
	
	
		
	push eax
	mov eax,vm_print_chararray_msg1
	call sys_print_str32
	pop eax
	

	
vm_print_chararray_ret:
	pop esi
	pop ecx
	pop eax
		push eax
	xor eax,eax
	mov dword [console_lock],eax
	pop eax

	ret
	



vmint_print_stack:
	mov ecx,MAX_STACK_TRACE_LENGTH

vmint_print_stack_loop:
	test ebp,ebp 					
	jz vmint_print_stack_ret
	dec ecx
	jz vmint_print_stack_ret
	
	
	 
	
	
	
	
		
	push eax
	mov eax,[ebp+VmX86StackReader_METHOD_ID_OFFSET]
	call sys_print_eax32
	pop eax



    
     
	
	
	
	
		
	push eax
	mov eax,vmint_print_stack_msg1
	call sys_print_str32
	pop eax
	


	
	mov ebp,[ebp+VmX86StackReader_PREVIOUS_OFFSET] 
	jmp vmint_print_stack_loop
	
vmint_print_stack_ret:
	ret
	




vm_athrow_msg1: db 'athrow of ',0
vm_athrow_msg2: db ': ',0
vm_athrow_msg3: db 'at ',0
vm_athrow_msg4: db 'Unhandled exception ... halt',0

vm_print_string_msg1: db 'NULL String!',0
vm_print_chararray_msg1: db 'NULL char array!',0

double_colon_msg: db '::',0
eol_msg: db 0xd,0xa,0

vmint_print_stack_msg1: db '   ',0


 









	global vm_invoke_abstract
vm_invoke_abstract:

	push dword VmThread_EX_ABSTRACTMETHOD 
	push eax 
	mov eax,SoftByteCodes_systemException
		call [eax+VmMethod_NATIVECODE_OFS]

	jmp vm_athrow
	ret





vm_invoke_msg1: db '{inv:',0
vm_invoke_msg3: db ' (cnt:',0
vm_invoke_msg4: db ')} ',0

vm_invoke_abstract_msg1: db 'Abstract method called: ',0

vm_invoke_compile_msg1: db '@#@# compile class ',0
vm_invoke_compile_msg2: db ' #@#@ ',0

vm_invoke_init_msg1: db '{clinit:',0
vm_invoke_init_msg2: db '} ',0



 








	global currentTimeMillisStaticsIdx
	
currentTimeMillisStaticsIdx	dd 0
	






stub_yieldPointHandler:
	push dword 0		
	push dword 0		
	push dword 0		
		push eax
	push ecx
	push edx
	push ebx
	push ebp
	push esi
	push edi
    push ds
    push es
    push fs
    push gs
    mov eax,KERNEL_DS
    mov ds,ax
    mov es,ax
    mov gs,ax
    
     
		
	
    cld
	test dword [int_die_halted],0xffffffff
	jnz near int_die_halt

	mov ebp,esp
	call yieldPointHandler
	    mov eax,esp
    and dword [esp+OLD_EFLAGS],~F_NT 
    and byte [gdt_tss+5],~0x02       
    pop gs
    pop fs
    pop es
    pop ds
    pop edi
    pop esi
    pop ebp
    pop ebx
    pop edx
    pop ecx
    pop eax
    add esp,12 
    iret

	






stub_timesliceHandler:
	push dword 0		
	push dword 0		
	push dword 0		
		push eax
	push ecx
	push edx
	push ebx
	push ebp
	push esi
	push edi
    push ds
    push es
    push fs
    push gs
    mov eax,KERNEL_DS
    mov ds,ax
    mov es,ax
    mov gs,ax
    
     
		
	
    cld
	test dword [int_die_halted],0xffffffff
	jnz near int_die_halt

	mov ebp,esp
	call timesliceHandler
	    mov eax,esp
    and dword [esp+OLD_EFLAGS],~F_NT 
    and byte [gdt_tss+5],~0x02       
    pop gs
    pop fs
    pop es
    pop ds
    pop edi
    pop esi
    pop ebp
    pop ebx
    pop edx
    pop ecx
    pop eax
    add esp,12 
    iret

	






  
	
	




  
	
	





  
	
	
	
	
	
	
	
	
	

	
	
	
	
	
	
	
	
	
	
	





  
	
	
	
	
	
	
	
	
	

	
	
	
	
	
	
	
	
	
	


yieldPointHandler_kernelCode:
	 
	
	
	
	
		
	push eax
	mov eax,yp_kernel_msg
	call sys_print_str32
	pop eax
	

	jmp int_die
	
yieldPointHandler:
	cmp dword [ebp+OLD_CS],USER_CS
	jne yieldPointHandler_kernelCode
	
	or dword[fs:VmProcessor_THREADSWITCHINDICATOR_OFS],VmProcessor_TSI_SWITCH_ACTIVE
	and dword[fs:VmProcessor_THREADSWITCHINDICATOR_OFS],~VmProcessor_TSI_SWITCH_NEEDED
	mov dword[fs:VmX86Processor_DEADLOCKCOUNTER_OFS], 0
	
	
	
yieldPointHandler_reschedule:
	
	mov edi,dword[fs:VmProcessor_CURRENTTHREAD_OFS]
		mov ecx,[ebp+OLD_EBP]
	mov [edi+VmX86Thread_EBP_OFS],ecx


		mov ecx,[ebp+OLD_EAX]
	mov [edi+VmX86Thread_EAX_OFS],ecx

		mov ecx,[ebp+OLD_EBX]
	mov [edi+VmX86Thread_EBX_OFS],ecx

		mov ecx,[ebp+OLD_ECX]
	mov [edi+VmX86Thread_ECX_OFS],ecx

		mov ecx,[ebp+OLD_EDX]
	mov [edi+VmX86Thread_EDX_OFS],ecx

		mov ecx,[ebp+OLD_EDI]
	mov [edi+VmX86Thread_EDI_OFS],ecx

		mov ecx,[ebp+OLD_ESI]
	mov [edi+VmX86Thread_ESI_OFS],ecx

		mov ecx,[ebp+OLD_EBP]
	mov [edi+VmX86Thread_EBP_OFS],ecx

		mov ecx,[ebp+OLD_ESP]
	mov [edi+VmX86Thread_ESP_OFS],ecx

		mov ecx,[ebp+OLD_EIP]
	mov [edi+VmX86Thread_EIP_OFS],ecx

		mov ecx,[ebp+OLD_EFLAGS]
	mov [edi+VmX86Thread_EFLAGS_OFS],ecx

 
	
	
	
	
	
	
	
	
	

	
yieldPointHandler_SaveMSRs:
		mov esi,[edi+VmX86Thread_READWRITEMSRS_OFS]
	test esi,esi
	jnz __jnasm_macro_local_label_62			
	jmp __jnasm_macro_local_label_64			
__jnasm_macro_local_label_62:	
	mov ebx,[esi+VmArray_LENGTH_OFFSET*4]
	test ebx,ebx
	jz __jnasm_macro_local_label_64
	lea esi,[esi+VmArray_DATA_OFFSET*4]
__jnasm_macro_local_label_63:
	mov edx,[esi]
	mov ecx,[edx+MSR_ID_OFS]
	rdmsr
	mov ecx,[esi]
	mov [ecx+MSR_VALUE_OFS+0],eax	
	mov [ecx+MSR_VALUE_OFS+4],edx	
	
	lea esi,[esi+4]
	dec ebx
	jnz __jnasm_macro_local_label_63		
__jnasm_macro_local_label_64:	

	
	
yieldPointHandler_fxSave:
	
	test dword [edi+VmX86Thread_FXFLAGS_OFS],VmX86Thread_FXF_USED
	jz yieldPointHandler_saveEnd	
	
	inc dword[fs:VmX86Processor_FXSAVECOUNTER_OFS]
	
	and dword [edi+VmX86Thread_FXFLAGS_OFS],~VmX86Thread_FXF_USED
	
yieldPointHandler_loadFxStatePtr:
	mov ebx, [edi+VmX86Thread_FXSTATEPTR_OFS]
	test ebx,ebx
	jz near yieldPointHandler_fxSaveInit
	
	test dword [cpu_features],FEAT_FXSR
	jz yieldPointHandler_fpuSave
	fxsave [ebx]
	jmp yieldPointHandler_saveEnd
yieldPointHandler_fpuSave:
	fnsave [ebx]
yieldPointHandler_saveEnd:

	
	push ebp
	xor ebp,ebp						
	mov eax,dword[fs:VmProcessor_KERNELSTACKEND_OFS]
	mov dword[fs:VmProcessor_STACKEND_OFS],eax				
	mov eax,VmProcessor_reschedule	
	push dword[fs:VmProcessor_ME_OFS]			
		call [eax+VmMethod_NATIVECODE_OFS]

	pop ebp

	
yieldPointHandler_restore:
	mov edi,dword[fs:VmProcessor_NEXTTHREAD_OFS]
	
	cmp edi,dword[fs:VmProcessor_CURRENTTHREAD_OFS]
	je near yieldPointHandler_done
	
	test edi,edi
	jz near yieldPointHandler_done
		mov ecx,[edi+VmX86Thread_EAX_OFS]
	mov [ebp+OLD_EAX],ecx

		mov ecx,[edi+VmX86Thread_EBX_OFS]
	mov [ebp+OLD_EBX],ecx

		mov ecx,[edi+VmX86Thread_ECX_OFS]
	mov [ebp+OLD_ECX],ecx

		mov ecx,[edi+VmX86Thread_EDX_OFS]
	mov [ebp+OLD_EDX],ecx

		mov ecx,[edi+VmX86Thread_EDI_OFS]
	mov [ebp+OLD_EDI],ecx

		mov ecx,[edi+VmX86Thread_ESI_OFS]
	mov [ebp+OLD_ESI],ecx

		mov ecx,[edi+VmX86Thread_EBP_OFS]
	mov [ebp+OLD_EBP],ecx

		mov ecx,[edi+VmX86Thread_ESP_OFS]
	mov [ebp+OLD_ESP],ecx

		mov ecx,[edi+VmX86Thread_EIP_OFS]
	mov [ebp+OLD_EIP],ecx

		mov ecx,[edi+VmX86Thread_EFLAGS_OFS]
	mov [ebp+OLD_EFLAGS],ecx

 
	
	
	
	
	
	
	
	
	
	
	
	
	mov eax,cr0
	or eax,CR0_TS
	mov cr0,eax
	
	
yieldPointHandler_RestoreMSRs:
		mov esi,[edi+VmX86Thread_READWRITEMSRS_OFS]
	test esi,esi
	jnz __jnasm_macro_local_label_66		
	jmp __jnasm_macro_local_label_67			
__jnasm_macro_local_label_66:	
	mov ebx,[esi+VmArray_LENGTH_OFFSET*4]
	test ebx,ebx
	jz __jnasm_macro_local_label_67
	lea esi,[esi+VmArray_DATA_OFFSET*4]
__jnasm_macro_local_label_65:
	mov edx,[esi]
	mov ecx,[edx+MSR_ID_OFS]		
	mov eax,[edx+MSR_VALUE_OFS+0]	
	mov edx,[edx+MSR_VALUE_OFS+4]	
	wrmsr
	
	lea esi,[esi+4]
	dec ebx
	jnz __jnasm_macro_local_label_65		
__jnasm_macro_local_label_67:	

		mov esi,[edi+VmX86Thread_WRITEONLYMSRS_OFS]
	test esi,esi
	jnz __jnasm_macro_local_label_69		
	jmp __jnasm_macro_local_label_70			
__jnasm_macro_local_label_69:	
	mov ebx,[esi+VmArray_LENGTH_OFFSET*4]
	test ebx,ebx
	jz __jnasm_macro_local_label_70
	lea esi,[esi+VmArray_DATA_OFFSET*4]
__jnasm_macro_local_label_68:
	mov edx,[esi]
	mov ecx,[edx+MSR_ID_OFS]		
	mov eax,[edx+MSR_VALUE_OFS+0]	
	mov edx,[edx+MSR_VALUE_OFS+4]	
	wrmsr
	
	lea esi,[esi+4]
	dec ebx
	jnz __jnasm_macro_local_label_68		
__jnasm_macro_local_label_70:	

	
	
yieldPointHandler_fixOldStackOverflow:
	mov cl,[edi+VmThread_STACKOVERFLOW_S1_OFS]
	test cl,cl
	jnz yieldPointHandler_fixStackOverflow
yieldPointHandler_afterStackOverflow:
	
	mov dword[fs:VmProcessor_CURRENTTHREAD_OFS],edi
	
	mov ebx,[edi+VmThread_ISOLATEDSTATICS_OFS]
	mov dword[fs:VmProcessor_ISOLATEDSTATICS_OFS],ebx
	
	mov ebx,[ebx+VmStatics_STATICS_OFS]
	mov dword[fs:VmProcessor_ISOLATEDSTATICSTABLE_OFS],ebx
	
	mov ebx,[edi+VmThread_STACKEND_OFS]
	mov dword[fs:VmProcessor_STACKEND_OFS],ebx
yieldPointHandler_done:
	and dword[fs:VmProcessor_THREADSWITCHINDICATOR_OFS],~VmProcessor_TSI_SWITCH_ACTIVE
	ret



yieldPointHandler_fixStackOverflow:
	
	mov ecx,[edi+VmThread_STACKEND_OFS]
	add ecx,(VmThread_STACK_OVERFLOW_LIMIT_SLOTS * 4)
	
	cmp [edi+VmX86Thread_ESP_OFS],ecx
	jle yieldPointHandler_afterStackOverflow		
	
	mov [edi+VmThread_STACKEND_OFS],ecx
	mov byte [edi+VmThread_STACKOVERFLOW_S1_OFS],0
	jmp yieldPointHandler_afterStackOverflow



fixFxStatePtr:
	mov ebx,[edi+VmX86Thread_FXSTATE_OFS]
	add ebx,(VmArray_DATA_OFFSET*4) + 15
	and ebx,~0xF
	mov [edi+VmX86Thread_FXSTATEPTR_OFS],ebx
	ret	

yieldPointHandler_fxSaveInit:
	call fixFxStatePtr
	jmp yieldPointHandler_loadFxStatePtr

	







int_dev_na:
	
	inc dword[fs:VmX86Processor_DEVICENACOUNTER_OFS]
	mov edi,dword[fs:VmProcessor_CURRENTTHREAD_OFS]
	
	or dword [edi+VmX86Thread_FXFLAGS_OFS],VmX86Thread_FXF_USED
	
	clts
	
	mov ebx, [edi+VmX86Thread_FXSTATEPTR_OFS]
	test ebx,ebx
	jz int_dev_na_ret		
	
	inc dword[fs:VmX86Processor_FXRESTORECOUNTER_OFS]
	test dword [cpu_features],FEAT_FXSR
	jz int_dev_na_fpuRestore
	fxrstor [ebx]
	ret
int_dev_na_fpuRestore:
	frstor [ebx]
int_dev_na_ret:
	ret





timer_handler:
	mov edi,dword[fs:VmProcessor_STATICSTABLE_OFS]
	mov eax,[currentTimeMillisStaticsIdx]
	lea edi,[edi+eax*4+(VmArray_DATA_OFFSET*4)]
	add dword [edi+0],1
	adc dword [edi+4],0
	test dword [edi+0],0x07

	
	
	
	jnz timer_ret
	
	or dword[fs:VmProcessor_THREADSWITCHINDICATOR_OFS], VmProcessor_TSI_SWITCH_NEEDED
	add dword[fs:VmX86Processor_DEADLOCKCOUNTER_OFS], 1
	test dword[fs:VmX86Processor_DEADLOCKCOUNTER_OFS], 0x4000
	jnz timer_deadlock
	
	
	test dword[fs:VmX86Processor_SENDTIMESLICEINTERRUPT_OFS], 1
	jz timer_ret
	
	push ebp
	xor ebp,ebp						
	mov eax,dword[fs:VmProcessor_KERNELSTACKEND_OFS]
	mov dword[fs:VmProcessor_STACKEND_OFS],eax				
	mov eax,VmX86Processor_broadcastTimeSliceInterrupt 
	mov byte [0xb8000+79*2],'1'
	push dword[fs:VmProcessor_ME_OFS]			
		call [eax+VmMethod_NATIVECODE_OFS]

	mov byte [0xb8000+79*2],'2'
	pop ebp
timer_ret:
	mov al,0x60 
	out 0x20,al
	ret
	
timer_deadlock:
	mov eax,dword [jnodeFinished]
	test eax,eax
	jnz timer_ret
	 
	
	
	
	
		
	push eax
	mov eax,deadLock_msg
	call sys_print_str32
	pop eax
	

	jmp int_die
	



timesliceHandler:
	
	inc byte [0xb8000+78*2]
	or dword[fs:VmProcessor_THREADSWITCHINDICATOR_OFS], VmProcessor_TSI_SWITCH_NEEDED
	add dword[fs:VmX86Processor_DEADLOCKCOUNTER_OFS], 1
	test dword[fs:VmX86Processor_DEADLOCKCOUNTER_OFS], 0x4000
	jnz timer_deadlock
	
	mov eax,dword[fs:VmX86Processor_LOCALAPICEOIADDRESS_OFS]
	mov dword [eax],0
	ret
	



def_irq_handler:
	cmp dword [ebp+OLD_CS],USER_CS
	jne def_irq_kernel
	
	mov eax,[ebp+INTNO]
	mov edi,dword[fs:VmX86Processor_IRQCOUNT_OFS]
	inc dword [edi+eax*4+(VmArray_DATA_OFFSET*4)]
	
	or dword[fs:VmProcessor_THREADSWITCHINDICATOR_OFS], VmProcessor_TSI_SWITCH_NEEDED
	
	ret
	
def_irq_kernel:
	 
	
	
	
	
		
	push eax
	mov eax,irq_kernel_msg
	call sys_print_str32
	pop eax
	

	ret
	







int_system_exception:
	test dword[fs:VmProcessor_THREADSWITCHINDICATOR_OFS],VmProcessor_TSI_SYSTEM_READY
	jz near int_die
	
	
	mov edi,dword[fs:VmProcessor_CURRENTTHREAD_OFS]
		mov ecx,[ebp+OLD_EAX]
	mov [edi+VmX86Thread_EXEAX_OFS],ecx

		mov ecx,[ebp+OLD_EBX]
	mov [edi+VmX86Thread_EXEBX_OFS],ecx

		mov ecx,[ebp+OLD_ECX]
	mov [edi+VmX86Thread_EXECX_OFS],ecx

		mov ecx,[ebp+OLD_EDX]
	mov [edi+VmX86Thread_EXEDX_OFS],ecx

		mov ecx,[ebp+OLD_EDI]
	mov [edi+VmX86Thread_EXEDI_OFS],ecx

		mov ecx,[ebp+OLD_ESI]
	mov [edi+VmX86Thread_EXESI_OFS],ecx

		mov ecx,[ebp+OLD_EBP]
	mov [edi+VmX86Thread_EXEBP_OFS],ecx

		mov ecx,[ebp+OLD_ESP]
	mov [edi+VmX86Thread_EXESP_OFS],ecx

		mov ecx,[ebp+OLD_EIP]
	mov [edi+VmX86Thread_EXEIP_OFS],ecx

		mov ecx,[ebp+OLD_EFLAGS]
	mov [edi+VmX86Thread_EXEFLAGS_OFS],ecx

	mov ecx,cr2
	mov [edi+VmX86Thread_EXCR2_OFS],ecx
	
	
	
	
	mov edi,[ebp+OLD_ESP]
	lea edi,[edi-4]
	mov [ebp+OLD_EAX],eax		
	mov [ebp+OLD_EBX],ebx		
	mov eax,[ebp+OLD_EIP]
	mov [edi+0],eax
	mov [ebp+OLD_ESP],edi
	mov dword [ebp+OLD_EIP],doSystemException
	ret
	
doSystemException:	
	push eax 
	push ebx 
	mov eax,SoftByteCodes_systemException
		call [eax+VmMethod_NATIVECODE_OFS]

	jmp vm_athrow
	



int_stack_overflow:
	cmp dword [ebp+OLD_CS],USER_CS
	jne doFatal_stack_overflow
	mov eax,dword[fs:VmProcessor_CURRENTTHREAD_OFS]
	mov cl,[eax+VmThread_STACKOVERFLOW_S1_OFS]
	test cl,cl
	jz int_stack_first_overflow
	jmp doFatal_stack_overflow
		
int_stack_first_overflow:
	inc byte [eax+VmThread_STACKOVERFLOW_S1_OFS]
	
	mov edx,[eax+VmThread_STACKEND_OFS]
	sub edx,(VmThread_STACK_OVERFLOW_LIMIT_SLOTS * 4)
	mov [eax+VmThread_STACKEND_OFS],edx
	mov dword[fs:VmProcessor_STACKEND_OFS],edx
	mov eax,VmThread_EX_STACKOVERFLOW
	mov dword [ebp+OLD_EIP],doSystemException
	jmp int_system_exception
	
doFatal_stack_overflow:
	 
	
	
	
	
		
	push eax
	mov eax,fatal_so_msg
	call sys_print_str32
	pop eax
	

	call vmint_print_stack
	jmp int_die
	cli
	hlt
	
yp_kernel_msg:	db 'YieldPoint in kernel mode??? Probably a bug',0
irq_kernel_msg:	db 'IRQ in kernel mode??? Probably a bug',0
fatal_so_msg:		db 'Fatal stack overflow: ',0
deadLock_msg:		db 'Very likely deadlock detected: ',0


 













	align 4
	
	global vm_jumpTable
vm_jumpTable:

	dd vm_athrow
	dd vm_athrow_notrace
	dd vm_invoke_abstract
	
	
	
 








stub_syscallHandler:
	push dword 0		
	push dword 0		
	push dword 0		
		push eax
	push ecx
	push edx
	push ebx
	push ebp
	push esi
	push edi
    push ds
    push es
    push fs
    push gs
    mov eax,KERNEL_DS
    mov ds,ax
    mov es,ax
    mov gs,ax
    
     
		
	
    cld
	test dword [int_die_halted],0xffffffff
	jnz near int_die_halt

	mov ebp,esp
	mov eax,dword [ebp+OLD_EAX]
	cmp eax,SC_MAX
	ja stub_syscallHandler_ret
	call dword [eax*4 + syscalls]
stub_syscallHandler_ret:	
	    mov eax,esp
    and dword [esp+OLD_EFLAGS],~F_NT 
    and byte [gdt_tss+5],~0x02       
    pop gs
    pop fs
    pop es
    pop ds
    pop edi
    pop esi
    pop ebp
    pop ebx
    pop edx
    pop ecx
    pop eax
    add esp,12 
    iret


syscalls:
	dd	disable_paging
	dd	enable_paging
	dd	sc_SyncMSRs
	dd	sc_SaveMSRs
	dd	sc_RestoreMSRs
	dd	sc_WriteMSR

sc_SyncMSRs:
	mov edi,dword[fs:VmProcessor_CURRENTTHREAD_OFS]
		mov esi,[edi+VmX86Thread_READWRITEMSRS_OFS]
	test esi,esi
	jnz __jnasm_macro_local_label_71			
	jmp __jnasm_macro_local_label_73			
__jnasm_macro_local_label_71:	
	mov ebx,[esi+VmArray_LENGTH_OFFSET*4]
	test ebx,ebx
	jz __jnasm_macro_local_label_73
	lea esi,[esi+VmArray_DATA_OFFSET*4]
__jnasm_macro_local_label_72:
	mov edx,[esi]
	mov ecx,[edx+MSR_ID_OFS]
	rdmsr
	mov ecx,[esi]
	mov [ecx+MSR_VALUE_OFS+0],eax	
	mov [ecx+MSR_VALUE_OFS+4],edx	
	
	lea esi,[esi+4]
	dec ebx
	jnz __jnasm_macro_local_label_72		
__jnasm_macro_local_label_73:	

		mov esi,[edi+VmX86Thread_READWRITEMSRS_OFS]
	test esi,esi
	jnz __jnasm_macro_local_label_75		
	jmp __jnasm_macro_local_label_76			
__jnasm_macro_local_label_75:	
	mov ebx,[esi+VmArray_LENGTH_OFFSET*4]
	test ebx,ebx
	jz __jnasm_macro_local_label_76
	lea esi,[esi+VmArray_DATA_OFFSET*4]
__jnasm_macro_local_label_74:
	mov edx,[esi]
	mov ecx,[edx+MSR_ID_OFS]		
	mov eax,[edx+MSR_VALUE_OFS+0]	
	mov edx,[edx+MSR_VALUE_OFS+4]	
	wrmsr
	
	lea esi,[esi+4]
	dec ebx
	jnz __jnasm_macro_local_label_74		
__jnasm_macro_local_label_76:	

		mov esi,[edi+VmX86Thread_WRITEONLYMSRS_OFS]
	test esi,esi
	jnz __jnasm_macro_local_label_78		
	jmp __jnasm_macro_local_label_79			
__jnasm_macro_local_label_78:	
	mov ebx,[esi+VmArray_LENGTH_OFFSET*4]
	test ebx,ebx
	jz __jnasm_macro_local_label_79
	lea esi,[esi+VmArray_DATA_OFFSET*4]
__jnasm_macro_local_label_77:
	mov edx,[esi]
	mov ecx,[edx+MSR_ID_OFS]		
	mov eax,[edx+MSR_VALUE_OFS+0]	
	mov edx,[edx+MSR_VALUE_OFS+4]	
	wrmsr
	
	lea esi,[esi+4]
	dec ebx
	jnz __jnasm_macro_local_label_77		
__jnasm_macro_local_label_79:	

	ret
	
sc_SaveMSRs:
	mov edi,dword[fs:VmProcessor_CURRENTTHREAD_OFS]
		mov esi,[edi+VmX86Thread_READWRITEMSRS_OFS]
	test esi,esi
	jnz __jnasm_macro_local_label_80			
	jmp __jnasm_macro_local_label_82			
__jnasm_macro_local_label_80:	
	mov ebx,[esi+VmArray_LENGTH_OFFSET*4]
	test ebx,ebx
	jz __jnasm_macro_local_label_82
	lea esi,[esi+VmArray_DATA_OFFSET*4]
__jnasm_macro_local_label_81:
	mov edx,[esi]
	mov ecx,[edx+MSR_ID_OFS]
	rdmsr
	mov ecx,[esi]
	mov [ecx+MSR_VALUE_OFS+0],eax	
	mov [ecx+MSR_VALUE_OFS+4],edx	
	
	lea esi,[esi+4]
	dec ebx
	jnz __jnasm_macro_local_label_81		
__jnasm_macro_local_label_82:	

	ret
	
sc_RestoreMSRs:
	mov edi,dword[fs:VmProcessor_CURRENTTHREAD_OFS]
		mov esi,[edi+VmX86Thread_READWRITEMSRS_OFS]
	test esi,esi
	jnz __jnasm_macro_local_label_84		
	jmp __jnasm_macro_local_label_85			
__jnasm_macro_local_label_84:	
	mov ebx,[esi+VmArray_LENGTH_OFFSET*4]
	test ebx,ebx
	jz __jnasm_macro_local_label_85
	lea esi,[esi+VmArray_DATA_OFFSET*4]
__jnasm_macro_local_label_83:
	mov edx,[esi]
	mov ecx,[edx+MSR_ID_OFS]		
	mov eax,[edx+MSR_VALUE_OFS+0]	
	mov edx,[edx+MSR_VALUE_OFS+4]	
	wrmsr
	
	lea esi,[esi+4]
	dec ebx
	jnz __jnasm_macro_local_label_83		
__jnasm_macro_local_label_85:	

		mov esi,[edi+VmX86Thread_WRITEONLYMSRS_OFS]
	test esi,esi
	jnz __jnasm_macro_local_label_87		
	jmp __jnasm_macro_local_label_88			
__jnasm_macro_local_label_87:	
	mov ebx,[esi+VmArray_LENGTH_OFFSET*4]
	test ebx,ebx
	jz __jnasm_macro_local_label_88
	lea esi,[esi+VmArray_DATA_OFFSET*4]
__jnasm_macro_local_label_86:
	mov edx,[esi]
	mov ecx,[edx+MSR_ID_OFS]		
	mov eax,[edx+MSR_VALUE_OFS+0]	
	mov edx,[edx+MSR_VALUE_OFS+4]	
	wrmsr
	
	lea esi,[esi+4]
	dec ebx
	jnz __jnasm_macro_local_label_86		
__jnasm_macro_local_label_88:	

	ret	

sc_WriteMSR:
	mov ecx,dword [ebp+OLD_ECX]
	mov eax,dword [ebp+OLD_EBX]
	mov edx,dword [ebp+OLD_EDX]
	wrmsr
	ret
	
  














	align 4
ap_boot:
							
	db 0x8C,0xC8			
	db 0x8E,0xD8			
	
	
	db 0x0f,0x01,0x1e	
	dw ap_idtbase48-ap_boot
	db 0x0f,0x01,0x16	
	dw ap_gdtbase48-ap_boot	
	
	
	db 0xB8,0x01,0x00		
	db 0x0F,0x01,0xF0		
	db 0xE9,0x00,0x00		

	
ap_boot16_jmp:	
	db 0x66,0xEA			
	dd ap_boot32
	dw KERNEL_CS

	bits 32
	
ap_boot32:	
	mov eax,KERNEL_DS
	mov ds,ax
	
	
ap_boot32_lgdt:
	lgdt [ap_gdtbase]
		
ap_boot32_ltss:
	mov ebx,0					
		
	
	jmp dword KERNEL_CS:ap_boot_in_kernel_space

	align 4
ap_gdtbase48:		
	dw (gdtend-gdtstart)-1
	dd gdtstart

	align 4
ap_idtbase48:
	dw 0
	dd 0

	align 4
ap_gdtbase:
	dw (gdtend-gdtstart)-1
ap_gdt_ptr:
	dd 0
	
	bits 32
ap_boot_end:	
	




 
ap_boot_in_kernel_space:
	mov eax,KERNEL_DS
	mov ss,ax
	mov ds,ax
	mov es,ax
	mov gs,ax
	mov eax,CURPROC_FS
	mov fs,ax
	
	
	
	mov esp,[ebx+0x04]			
	push ebx					
	
	
	 
	
	
	
	
		
	push eax
	mov eax,ap_boot_msg
	call sys_print_str32
	pop eax
	

	
	
	lidt [idt]
		jmp __jnasm_macro_local_label_89
__jnasm_macro_local_label_89:

	
	
	call enable_paging
	
	
	pop ebx						


	
	
	
	
	 



	
    mov eax,TSS_DS
    ltr ax
    
    pushf
    pop eax
    or eax,F_IOPL1 | F_IOPL2
    push eax
    popf
    	jmp __jnasm_macro_local_label_90
__jnasm_macro_local_label_90:

    
	
	mov ecx,[ebx+0x38]			

	mov eax,ap_boot_go_user_cs
	push dword USER_DS			
	push ecx					
	pushf						
	push dword USER_CS			
	push eax					
	pushf
	pop eax
	and eax,~F_NT
	push eax
	popf
	iret

	
		

ap_boot_go_user_cs:
	mov eax,USER_DS
	mov ss,ax
	mov esp,ecx
	mov ds,ax
	mov es,ax
	mov gs,ax
	mov eax,CURPROC_FS
	mov fs,ax
	

    
	
	
	 
	
	
	
	
		
	push eax
	mov eax,ap_user_msg
	call sys_print_str32
	pop eax
	

	
	
	sti
	
	
	xor ebp,ebp		
	push ebp		
	push ebp		
	push ebp		
	push ebp		
	mov ebp,esp
	mov eax,VmX86Processor_applicationProcessorMain
		call [eax+VmMethod_NATIVECODE_OFS]

	
	
ap_halt:	
	hlt
	jmp ap_halt
	
ap_test:
	inc byte [0xb8000+0]	
	jmp ap_test
	
ap_boot_msg: 	 db 'AP-boot',0xd,0xa,0
ap_user_msg: 	 db 'AP-usermode',0xd,0xa,0
	
	
 
		align 4096
kernel_end:

scr_ofs:		dd 0
hexchars: 		db '0123456789ABCDEF' 
console_lock: dd 0

die_lock: dd 0

jnodeFinished:	dd 0
kdb_enabled		dd 0
kdb_port		dd 0x3f8
newline_msg:	db 0xa,0
  
		align 4096
	global vm_start
vm_start:

vmCurProcessor: dd 0
VmThread_runThread: dd 0
bootHeapStart: dd 0
bootHeapEnd: dd 0
freeMemoryStart: dd 0
vm_findThrowableHandler: dd 0
VmMethod_Class: dd 0
VmSystem_initialized: dd 0
SoftByteCodes_systemException: dd 0
VmProcessor_reschedule: dd 0
VmX86Processor_broadcastTimeSliceInterrupt: dd 0
VmX86Processor_applicationProcessorMain: dd 0
Luser_esp: dd 0
