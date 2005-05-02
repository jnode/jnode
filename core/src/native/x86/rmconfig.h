; -----------------------------------------------
; $Id: rmconfig.h,v 1.4 2003/11/24 07:48:26 epr Exp $
;
; JNode Boot Real Mode Configuration parameters
;
; Author       : E.Prangsma 
; -----------------------------------------------

configbase equ 0c000h

%define rmc_bootdevice     byte[configbase+0]	; Boot device (DL passed by the BIOS)
;                   resb 3
%define rmc_basememsize    dword[configbase+4]	; Base memory size (in bytes)
%define rmc_extmemsize     dword[configbase+8]	; Extended memory size (in bytes)
%define rmc_classes_addr   dword[configbase+12]	; Start address of the classes file
%define rmc_classes_size   dword[configbase+16]	; Size (in bytes) of the classes file
%define rmc_memstart       dword[configbase+20]	; Start of free memory after the kernel & classes
%define rmc_bootversion    dword[configbase+24]	; Version of the boot sector

