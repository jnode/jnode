; -----------------------------------------------
; $Id: rmconfig.h,v 1.4 2003/11/24 07:48:26 epr Exp $
;
; JNode Boot Real Mode Configuration parameters
;
; Author       : E.Prangsma 
; -----------------------------------------------

configbase equ 0c000h

    absolute configbase

rmc_bootdevice     resb 1  ; Boot device (DL passed by the BIOS)
                   resb 3
rmc_basememsize    resd 1  ; Base memory size (in bytes)
rmc_extmemsize     resd 1  ; Extended memory size (in bytes)
rmc_classes_addr   resd 1  ; Start address of the classes file
rmc_classes_size   resd 1  ; Size (in bytes) of the classes file
rmc_memstart       resd 1  ; Start of free memory after the kernel & classes
rmc_bootversion    resd 1  ; Version of the boot sector

