; -----------------------------------------------
; $Id: i386.h,v 1.6 2003/11/24 07:49:11 epr Exp $
;
; Intel 386+ constants
;
; Author       : E.Prangsma 
; -----------------------------------------------

; ------------------
; Flags
; ------------------

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
F_NT        equ 0x00004000	; Nested task
F_03        equ 0x00008000
F_RF        equ 0x00010000	; Resume flag
F_VM        equ 0x00020000	; Virtual 8086 mode
F_AC		equ 0x00040000	; Alignment check
F_VIF		equ	0x00080000	; Virtual interrupt flag
F_VIP		equ	0x00100000	; Virtual interrupt pending
F_ID		equ	0x00200000	; ID flag

; ------------------
; CR0 flags
; ------------------

CR0_PE		equ 0x00000001	; Protection enabled
CR0_MP		equ 0x00000002	; Coprocessor present
CR0_EM		equ 0x00000004	; Do not monitor coprocessor
CR0_TS		equ 0x00000008	; No task switch
CR0_ET		equ 0x00000010	; Indicates the presence of a 80387
CR0_PG		equ 0x80000000	; Paging enabled

; ------------------
; CR4 flags
; ------------------

CR4_VME			equ 1 << 0	; Virtual Mode Extensions
CR4_PVI			equ 1 << 1	; Protected mode virtual interrupts
CR4_TSD			equ 1 << 2	; Timestamp disable
CR4_DE			equ 1 << 3	; Debugging extensions
CR4_PSE			equ 1 << 4	; Page size extensions
CR4_PAE			equ 1 << 5	; Physical address extension
CR4_MCE			equ 1 << 6	; Machine check enable
CR4_PGE			equ 1 << 7	; Page global enable
CR4_PCE			equ 1 << 8	; Performance monitoring counter enable
CR4_OSFXSR		equ 1 << 9	; Operating system FXSAVE/FXRSTOR support
CR4_OSXMMEXCPT	equ 1 << 10	; Operating system unmasked exception support

; ------------------
; MXCSR flags
; SSE control flags
; ------------------

MXCSR_IE		equ 1 << 0	; Invalid operation exception
MXCSR_DE		equ 1 << 1	; Denormalized operand exception
MXCSR_ZE		equ 1 << 2	; Zero-divide exception
MXCSR_OE		equ 1 << 3	; Overflow exception
MXCSR_UE		equ 1 << 4	; Underflow exception
MXCSR_PE		equ 1 << 5	; Precision exception
MXCSR_DAZ		equ 1 << 6	; Denormals all zero's
MXCSR_IM		equ 1 << 7	; Invalid operation exception mask
MXCSR_DM		equ 1 << 8	; Denormalized operand exception mask
MXCSR_ZM		equ 1 << 9	; Zero-divide exception mask
MXCSR_OM		equ 1 << 10	; Overflow exception mask
MXCSR_UM		equ 1 << 11	; Underflow exception mask
MXCSR_PM		equ 1 << 12	; Precision exception mask
MXCSR_RC1		equ 1 << 13	; Floating point rounding control bit 1
MXCSR_RC2		equ 1 << 14	; Floating point rounding control bit 2
MXCSR_FZ		equ 1 << 15	; Flush to zero for masked underflow

; ------------------
; Page flags
; ------------------

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

; ----------------------
; CPUID feature flags
; ----------------------

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

; ----------------------
; Multiboot Info struct
; ----------------------

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
MBMMAP_BASEADDR	equ 0		; 64-bit base address
MBMMAP_LENGTH	equ 8		; 64-bit length
MBMMAP_TYPE		equ 16		; 32-bit type
MBMMAP_ESIZE	equ 20

VBECTRLINFO	equ 0
VBEMODEINFO	equ 4
VBE_MODE	equ 8
VBE_INT_SEG	equ 10
VBE_INT_OFF	equ 12
VBE_INT_LEN	equ 14
VBE_ESIZE	equ 16

VBECTRLINFO_SIZE	equ 512
VBEMODEINFO_SIZE	equ 256

MBF_MEM			equ (1 << 0)
MBF_BOOTDEVICE	equ (1 << 1)
MBF_CMDLINE		equ (1 << 2)
MBF_MODS		equ (1 << 3)
MBF_MMAP		equ (1 << 6)
MBF_VBE			equ (1 << 11)

%include "i386_bits.h"
