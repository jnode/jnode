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
F_NT        equ 0x00004000
F_03        equ 0x00008000
F_RF        equ 0x00010000
F_VM        equ 0x00020000

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

CR4_VME		equ 0x00000001
CR4_PVI		equ 0x00000002
CR4_TSD		equ 0x00000004
CR4_DE		equ 0x00000008
CR4_PSE		equ 0x00000010
CR4_MCE		equ 0x00000040

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
; Multiboot Info struct
; ----------------------

MBI_FLAGS		equ 0x00
MBI_MEMLOWER	equ 0x04
MBI_MEMUPPER	equ 0x08
MBI_BOOTDEVICE	equ 0x0C
MBI_CMDLINE		equ 0x10
MBI_MODSCOUNT	equ 0x14
MBI_MODSADDR	equ 0x18

MBI_SIZE		equ 0x20
MBI_CMDLINE_MAX	equ 0x400

MBMOD_START		equ 0x00
MBMOD_END		equ 0x04
MBMOD_CMDLINE	equ 0x08
MBMOD_PAD		equ 0x10


